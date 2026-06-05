package com.androidperf.systrace.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TraceBuildConfig {
    private final List<String> includedClassPrefixes;
    private final List<String> excludedClassPrefixes;

    public TraceBuildConfig() {
        this(Collections.emptyList(), Collections.emptyList());
    }

    public TraceBuildConfig(List<String> includedClassPrefixes, List<String> excludedClassPrefixes) {
        this.includedClassPrefixes = normalizePrefixes(includedClassPrefixes);
        this.excludedClassPrefixes = normalizePrefixes(excludedClassPrefixes);
    }

    /**
     * whether it need to trace by class filename
     *
     * @param fileName fileName
     * @return isNeed
     */
    public boolean isNeedTraceClass(String fileName) {
        if (fileName == null || !fileName.endsWith(".class")) {
            return false;
        }
        String simpleName = fileName.substring(0, fileName.length() - ".class".length());
        return !isGeneratedSimpleClassName(simpleName);
    }

    /**
     * whether it need to trace by class name in AGP instrumentation callback
     *
     * @param className fully-qualified class name
     * @return isNeed
     */
    public boolean isNeedTraceClassName(String className) {
        if (className == null || className.length() == 0) {
            return false;
        }
        String normalizedClassName = className.replace('/', '.');
        int separatorIndex = normalizedClassName.lastIndexOf('.');
        String simpleClassName = separatorIndex >= 0
                ? normalizedClassName.substring(separatorIndex + 1)
                : normalizedClassName;
        if (!isNeedTraceClass(simpleClassName + ".class")) {
            return false;
        }
        if (!includedClassPrefixes.isEmpty() && !startsWithAny(normalizedClassName, includedClassPrefixes)) {
            return false;
        }
        return !startsWithAny(normalizedClassName, excludedClassPrefixes);
    }

    public static boolean isGeneratedSimpleClassName(String simpleClassName) {
        return "R".equals(simpleClassName)
                || simpleClassName.startsWith("R$")
                || "Manifest".equals(simpleClassName)
                || simpleClassName.startsWith("Manifest$")
                || "BuildConfig".equals(simpleClassName);
    }

    private static List<String> normalizePrefixes(List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalizedPrefixes = new ArrayList<>();
        for (String prefix : prefixes) {
            if (prefix != null && prefix.trim().length() > 0) {
                normalizedPrefixes.add(prefix.trim().replace('/', '.'));
            }
        }
        return normalizedPrefixes;
    }

    private static boolean startsWithAny(String className, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
