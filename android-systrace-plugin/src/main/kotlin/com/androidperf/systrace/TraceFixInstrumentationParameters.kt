package com.androidperf.systrace

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

abstract class TraceFixInstrumentationParameters : InstrumentationParameters {
    @get:Input
    abstract val includedClassPrefixes: ListProperty<String>

    @get:Input
    abstract val excludedClassPrefixes: ListProperty<String>

    @get:Input
    abstract val traceConstructors: Property<Boolean>

    @get:Input
    abstract val traceClassInitializers: Property<Boolean>

    @get:Input
    abstract val traceSyntheticMethods: Property<Boolean>

    @get:Input
    abstract val traceBridgeMethods: Property<Boolean>
}
