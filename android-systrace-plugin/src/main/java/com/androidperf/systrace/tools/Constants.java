package com.androidperf.systrace.tools;

public class Constants {
    public static final String[] UN_TRACE_CLASS = {"R.class", "R$", "Manifest", "BuildConfig"};
    public final static int MAX_SECTION_NAME_LEN = 127;
    public final static String DEFAULT_TRACE_METHOD_BEAT_CLASS = "androidx/core/os/TraceCompat";
    public final static String DEFAULT_TRACE_METHOD_BEAT_BEGIN = "beginSection";
    public final static String DEFAULT_TRACE_METHOD_BEAT_END = "endSection";
}
