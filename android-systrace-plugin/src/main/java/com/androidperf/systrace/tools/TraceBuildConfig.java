package com.androidperf.systrace.tools;

public class TraceBuildConfig {

    /**
     * whether it need to trace by class filename
     *
     * @param fileName fileName
     * @return isNeed
     */
    public boolean isNeedTraceClass(String fileName) {
        boolean isNeed = true;
        if (fileName.endsWith(".class")) {
            for (String unTraceCls : Constants.UN_TRACE_CLASS) {
                if (fileName.contains(unTraceCls)) {
                    isNeed = false;
                    break;
                }
            }
        } else {
            isNeed = false;
        }
        return isNeed;
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
        return isNeedTraceClass(simpleClassName + ".class");
    }

}
