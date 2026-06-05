package com.androidperformance.test;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements DefaultTraceInterface {
    private static final int STATIC_VALUE = staticInitializer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TextView(this));

        testSleep();
        traceExceptionDemo(false);
        overloadedDemo(1);
        overloadedDemo("trace");
        synchronizedDemo(2);
        defaultInterfaceMethod();
        new ConcreteFixture().concreteBaseMethod();
        testLongMethodNameXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXGGGGGGGGG();
    }

    private static int staticInitializer() {
        return 1;
    }

    private void testSleep() {
        Thread.yield();
    }

    private int traceExceptionDemo(boolean shouldThrow) {
        if (shouldThrow) {
            throw new IllegalStateException("trace demo exception");
        }
        return 42;
    }

    private int overloadedDemo(int value) {
        return value + STATIC_VALUE;
    }

    private String overloadedDemo(String value) {
        return value + STATIC_VALUE;
    }

    private synchronized int synchronizedDemo(int value) {
        return value + 1;
    }

    private int testLongMethodNameXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXGGGGGGGGG() {
        return 7;
    }

    private native void nativeMethod();

    abstract static class AbstractFixture {
        int concreteBaseMethod() {
            return 7;
        }

        abstract int abstractMethod();
    }

    static class ConcreteFixture extends AbstractFixture {
        @Override
        int abstractMethod() {
            return 9;
        }
    }
}

interface DefaultTraceInterface {
    default int defaultInterfaceMethod() {
        return 3;
    }
}
