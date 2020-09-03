package com.androidperf.systrace.tools;

public class TraceBuildConfig {

    /**
     * whether it need to trace by class filename
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

}
