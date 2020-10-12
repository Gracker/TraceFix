package com.androidperformance.test;

import androidx.core.os.TraceCompat;

public class Test {
    public void test() {
        TraceCompat.beginSection(new Throwable().getStackTrace()[0].getMethodName());
    }

    public void test2() {
        TraceCompat.endSection();
    }
}
