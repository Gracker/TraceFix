#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

LOW_AGP_VERSION="${LOW_AGP_VERSION:-7.4.2}"
HIGH_AGP_VERSION="${HIGH_AGP_VERSION:-8.3.2}"
REMOTE_PLUGIN_VERSION="${REMOTE_PLUGIN_VERSION:-1.0}"
REMOTE_REPO_URL="${REMOTE_REPO_URL:-file://${ROOT_DIR}/build/tracefix-remote-repo}"

run_check() {
    local label="$1"
    local agp_version="$2"
    local module="$3"
    local plugin_source="$4"
    local class_name="$5"
    local method_name="$6"

    echo "=== ${label}: AGP ${agp_version} / ${plugin_source} plugin / module ${module} ==="
    if [[ "${plugin_source}" == "local" ]]; then
        ./gradlew \
            -PTRACEFIX_AGP_VERSION="${agp_version}" \
            :android-systrace-plugin:publishToMavenLocal \
            :"${module}":clean \
            :"${module}":assembleDebug \
            --no-daemon
    elif [[ "${plugin_source}" == "remote" ]]; then
        ./gradlew \
            -PTRACEFIX_AGP_VERSION="${agp_version}" \
            :android-systrace-plugin:publishTraceFixLocalPublicationToTracefixRemoteRepository \
            --no-daemon

        ./gradlew \
            -PTRACEFIX_AGP_VERSION="${agp_version}" \
            -PTRACEFIX_ENABLE_REMOTE_DEMOS=true \
            -PTRACEFIX_VERSION_REMOTE="${REMOTE_PLUGIN_VERSION}" \
            -PTRACEFIX_REMOTE_REPO_URL="${REMOTE_REPO_URL}" \
            :"${module}":clean \
            :"${module}":assembleDebug \
            --no-daemon
    else
        echo "ERROR: unknown plugin source '${plugin_source}'"
        exit 1
    fi

    local class_file
    class_file="$(find "${module}/build/intermediates" -type f -name "${class_name}" | grep -E 'transformDebugClassesWithAsm|asm_instrumented_project_classes' | head -n 1 || true)"
    if [[ -z "${class_file}" ]]; then
        echo "ERROR: cannot find transformed class ${class_name} for module ${module}"
        exit 1
    fi

    local javap_output
    javap_output="$(mktemp)"
    javap -c -p "${class_file}" > "${javap_output}"

    if ! grep -q 'android/os/Trace.beginSection' "${javap_output}"; then
        echo "ERROR: Trace.beginSection not found in ${class_file}"
        exit 1
    fi
    if ! grep -q 'android/os/Trace.endSection' "${javap_output}"; then
        echo "ERROR: Trace.endSection not found in ${class_file}"
        exit 1
    fi
    if ! grep -q "${method_name}" "${javap_output}"; then
        echo "ERROR: ${method_name} not found in ${class_file}"
        exit 1
    fi
    if ! grep -q 'athrow' "${javap_output}"; then
        echo "ERROR: exceptional path (athrow) not found in ${class_file}"
        exit 1
    fi

    rm -f "${javap_output}"
    echo "PASS: ${label}"
}

run_check "Low-version local demo" "${LOW_AGP_VERSION}" "android-systrace-sample-local-debug" "local" "MainActivity.class" "traceExceptionDemo"
run_check "Low-version remote demo" "${LOW_AGP_VERSION}" "android-systrace-sample-low-remote-debug" "remote" "MainActivity.class" "traceExceptionDemo"
run_check "High-version local demo" "${HIGH_AGP_VERSION}" "android-systrace-sample-high-local-debug" "local" "MainActivity.class" "traceExceptionDemo"
run_check "High-version remote demo" "${HIGH_AGP_VERSION}" "android-systrace-sample-remote-debug" "remote" "ScrollingActivity.class" "traceExceptionDemo"

echo "All compatibility demos passed."
