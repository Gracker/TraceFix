package com.androidperf.systrace

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.androidperf.systrace.tools.TraceBuildConfig
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

abstract class TraceFixAsmClassVisitorFactory :
    AsmClassVisitorFactory<TraceFixInstrumentationParameters> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val traceFixParameters = parameters.get()
        return TraceFixMethodTracer(
            Opcodes.ASM9,
            nextClassVisitor,
            traceFixParameters.traceConstructors.get(),
            traceFixParameters.traceClassInitializers.get(),
            traceFixParameters.traceSyntheticMethods.get(),
            traceFixParameters.traceBridgeMethods.get()
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val traceFixParameters = parameters.get()
        val buildConfig = TraceBuildConfig(
            traceFixParameters.includedClassPrefixes.get(),
            traceFixParameters.excludedClassPrefixes.get()
        )
        return buildConfig.isNeedTraceClassName(classData.className)
    }
}
