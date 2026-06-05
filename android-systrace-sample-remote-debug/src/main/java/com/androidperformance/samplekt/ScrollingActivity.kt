package com.androidperformance.samplekt

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class ScrollingActivity : Activity(), KotlinDefaultTraceInterface {
    companion object {
        private val staticValue = staticInitializer()

        @JvmStatic
        private fun staticInitializer(): Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this))

        testSleep()
        traceExceptionDemo(false)
        overloadedDemo(1)
        overloadedDemo("trace")
        synchronizedDemo(2)
        defaultInterfaceMethod()
        defaultArgDemo()
        lambdaDemo()
        ConcreteFixture().concreteBaseMethod()
    }

    private fun testSleep() {
        Thread.yield()
    }

    private fun traceExceptionDemo(shouldThrow: Boolean): Int {
        if (shouldThrow) {
            throw IllegalStateException("trace demo exception")
        }
        return 42
    }

    private fun overloadedDemo(value: Int): Int = value + staticValue

    private fun overloadedDemo(value: String): String = value + staticValue

    @Synchronized
    private fun synchronizedDemo(value: Int): Int = value + 1

    private fun defaultArgDemo(value: Int = 1): Int = value + 1

    private fun lambdaDemo(): Int {
        val mapper = { value: Int -> value + 1 }
        return mapper(1)
    }

    abstract class AbstractFixture {
        fun concreteBaseMethod(): Int = 7
        abstract fun abstractMethod(): Int
    }

    class ConcreteFixture : AbstractFixture() {
        override fun abstractMethod(): Int = 9
    }
}

interface KotlinDefaultTraceInterface {
    fun defaultInterfaceMethod(): Int = 3
}
