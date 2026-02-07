package com.androidperf.systrace

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.androidperf.systrace.tools.Constants
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

abstract class TraceFixAsmClassVisitorFactory :
    AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return TraceFixMethodTracer(Opcodes.ASM9, nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val normalizedClassName = classData.className.replace('/', '.')
        val simpleClassName = normalizedClassName.substringAfterLast('.', normalizedClassName)
        if (simpleClassName.isEmpty()) {
            return false
        }
        val classFileName = "$simpleClassName.class"
        return Constants.UN_TRACE_CLASS.none { classFileName.contains(it) }
    }
}
