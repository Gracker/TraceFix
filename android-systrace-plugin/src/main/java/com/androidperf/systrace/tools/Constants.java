package com.androidperf.systrace.tools;

public class Constants {
    public final static int MAX_SECTION_NAME_LEN = 127;
    public final static String DEFAULT_TRACE_METHOD_BEAT_CLASS = "android/os/Trace";
    public final static String DEFAULT_TRACE_METHOD_BEAT_BEGIN = "beginSection";
    public final static String DEFAULT_TRACE_METHOD_BEAT_END = "endSection";
    public final static String WINDOW_FOCUS_METHOD = "onWindowFocusChanged";
    public final static String WINDOW_FOCUS_METHOD_ARGS = "(Z)V";
}
