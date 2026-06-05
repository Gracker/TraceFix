#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

AGP_VERSION="${AGP_VERSION:-9.1.1}"
REMOTE_PLUGIN_VERSION="${REMOTE_PLUGIN_VERSION:-0.2.0-local}"
REMOTE_REPO_URL="${REMOTE_REPO_URL:-file://${ROOT_DIR}/build/tracefix-remote-repo}"

run_gradle() {
    ./gradlew \
        -PTRACEFIX_AGP_VERSION="${AGP_VERSION}" \
        -PTRACEFIX_AGP_API_VERSION="${AGP_VERSION}" \
        "$@" \
        --no-daemon
}

find_transformed_class() {
    local module="$1"
    local variant="$2"
    local class_name="$3"
    local variant_cap
    variant_cap="$(printf '%s%s' "$(printf '%s' "${variant:0:1}" | tr '[:lower:]' '[:upper:]')" "${variant:1}")"

    find "${module}/build/intermediates" -type f -name "${class_name}" \
        | grep -E "/${variant}/(transform${variant_cap}ClassesWithAsm|asm_instrumented_project_classes|instrumented_project_classes)" \
        | head -n 1 || true
}

method_block() {
    local class_file="$1"
    local method_marker="$2"
    javap -c -p "${class_file}" | awk -v marker="${method_marker}" '
        /^[[:space:]][^0-9][^;{]*\([^)]*\);$/ {
            in_method = index($0, marker) > 0
        }
        in_method { print }
        in_method && /^$/ { exit }
    '
}

assert_contains() {
    local text="$1"
    local expected="$2"
    local message="$3"
    if ! grep -q "${expected}" <<< "${text}"; then
        echo "ERROR: ${message}"
        echo "${text}"
        exit 1
    fi
}

assert_not_contains() {
    local text="$1"
    local unexpected="$2"
    local message="$3"
    if grep -q "${unexpected}" <<< "${text}"; then
        echo "ERROR: ${message}"
        echo "${text}"
        exit 1
    fi
}

assert_method_traced() {
    local class_file="$1"
    local method_marker="$2"
    local block
    block="$(method_block "${class_file}" "${method_marker}")"
    assert_contains "${block}" "android/os/Trace.beginSection" "${method_marker} missing Trace.beginSection in ${class_file}"
    assert_contains "${block}" "android/os/Trace.endSection" "${method_marker} missing Trace.endSection in ${class_file}"
}

assert_method_not_traced() {
    local class_file="$1"
    local method_marker="$2"
    local block
    block="$(method_block "${class_file}" "${method_marker}")"
    assert_not_contains "${block}" "android/os/Trace.beginSection" "${method_marker} should not be traced in ${class_file}"
    assert_not_contains "${block}" "android/os/Trace.endSection" "${method_marker} should not be traced in ${class_file}"
}

verify_java_fixture() {
    local module="$1"
    local variant="$2"
    local main_class
    local abstract_class
    local interface_class

    main_class="$(find_transformed_class "${module}" "${variant}" "MainActivity.class")"
    abstract_class="$(find_transformed_class "${module}" "${variant}" 'MainActivity$AbstractFixture.class')"
    interface_class="$(find_transformed_class "${module}" "${variant}" "DefaultTraceInterface.class")"

    if [[ -z "${main_class}" || -z "${abstract_class}" || -z "${interface_class}" ]]; then
        echo "ERROR: cannot find transformed Java fixture classes for ${module}/${variant}"
        exit 1
    fi

    assert_method_traced "${main_class}" "traceExceptionDemo(boolean)"
    assert_contains "$(method_block "${main_class}" "traceExceptionDemo(boolean)")" "athrow" "traceExceptionDemo missing exceptional path in ${main_class}"
    assert_method_traced "${main_class}" "overloadedDemo(int)"
    assert_method_traced "${main_class}" "overloadedDemo(java.lang.String)"
    assert_method_traced "${main_class}" "synchronizedDemo(int)"
    assert_method_traced "${main_class}" "testLongMethodName"
    assert_method_not_traced "${main_class}" "nativeMethod()"
    assert_method_traced "${abstract_class}" "concreteBaseMethod()"
    assert_method_not_traced "${abstract_class}" "abstractMethod()"
    assert_method_traced "${interface_class}" "defaultInterfaceMethod()"
}

verify_kotlin_fixture() {
    local module="$1"
    local variant="$2"
    local class_file

    class_file="$(find_transformed_class "${module}" "${variant}" "ScrollingActivity.class")"
    if [[ -z "${class_file}" ]]; then
        echo "ERROR: cannot find transformed Kotlin fixture for ${module}/${variant}"
        exit 1
    fi

    assert_method_traced "${class_file}" "traceExceptionDemo(boolean)"
    assert_contains "$(method_block "${class_file}" "traceExceptionDemo(boolean)")" "athrow" "traceExceptionDemo missing exceptional path in ${class_file}"
    assert_method_traced "${class_file}" "overloadedDemo(int)"
    assert_method_traced "${class_file}" "overloadedDemo(java.lang.String)"
    assert_method_traced "${class_file}" "synchronizedDemo(int)"
    assert_method_traced "${class_file}" "defaultArgDemo(int)"
    assert_method_not_traced "${class_file}" 'defaultArgDemo$default'
}

run_check() {
    local label="$1"
    local module="$2"
    local plugin_source="$3"
    local verifier="$4"

    echo "=== ${label}: AGP ${AGP_VERSION} / ${plugin_source} plugin / module ${module} ==="
    if [[ "${plugin_source}" == "local" ]]; then
        run_gradle \
            :android-systrace-plugin:publishToMavenLocal

        run_gradle \
            -PTRACEFIX_ENABLE_DEMOS=true \
            :"${module}":clean \
            :"${module}":assembleDebug \
            :"${module}":assembleRelease
    elif [[ "${plugin_source}" == "remote" ]]; then
        run_gradle \
            :android-systrace-plugin:publishTraceFixLocalPublicationToTracefixRemoteRepository

        run_gradle \
            -PTRACEFIX_ENABLE_REMOTE_DEMOS=true \
            -PTRACEFIX_VERSION_REMOTE="${REMOTE_PLUGIN_VERSION}" \
            -PTRACEFIX_REMOTE_REPO_URL="${REMOTE_REPO_URL}" \
            :"${module}":clean \
            :"${module}":assembleDebug \
            :"${module}":assembleRelease
    else
        echo "ERROR: unknown plugin source '${plugin_source}'"
        exit 1
    fi

    "${verifier}" "${module}" "debug"
    "${verifier}" "${module}" "release"
    echo "PASS: ${label}"
}

run_check "Android 17 local Java fixture" "android-systrace-sample-high-local-debug" "local" "verify_java_fixture"
run_check "Android 17 remote Kotlin fixture" "android-systrace-sample-remote-debug" "remote" "verify_kotlin_fixture"

echo "All Android 17 compatibility demos passed."
