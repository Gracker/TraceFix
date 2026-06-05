package com.androidperf.systrace;

import com.androidperf.systrace.tools.Constants;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class TraceFixMethodTracer extends ClassVisitor {
    private final int asmApi;
    private final boolean traceConstructors;
    private final boolean traceClassInitializers;
    private final boolean traceSyntheticMethods;
    private final boolean traceBridgeMethods;
    private String className;

    public TraceFixMethodTracer(int api, ClassVisitor cv) {
        this(api, cv, true, true, false, false);
    }

    public TraceFixMethodTracer(
            int api,
            ClassVisitor cv,
            boolean traceConstructors,
            boolean traceClassInitializers,
            boolean traceSyntheticMethods,
            boolean traceBridgeMethods) {
        super(api, cv);
        this.asmApi = api;
        this.traceConstructors = traceConstructors;
        this.traceClassInitializers = traceClassInitializers;
        this.traceSyntheticMethods = traceSyntheticMethods;
        this.traceBridgeMethods = traceBridgeMethods;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name.replace("/", ".");
    }

    @Override
    public MethodVisitor visitMethod(
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        if (!shouldTraceMethod(access, name)) {
            return methodVisitor;
        }
        return new AdviceAdapter(asmApi, methodVisitor, access, name, desc) {
            private final Label traceStart = new Label();
            private final Label traceEnd = new Label();
            private boolean traceOpened = false;

            @Override
            protected void onMethodEnter() {
                mv.visitLdcInsn(buildSectionName(className, name, desc));
                mv.visitMethodInsn(INVOKESTATIC, Constants.DEFAULT_TRACE_METHOD_BEAT_CLASS,
                        Constants.DEFAULT_TRACE_METHOD_BEAT_BEGIN,
                        "(Ljava/lang/String;)V", false);
                traceOpened = true;
                visitLabel(traceStart);
            }

            @Override
            protected void onMethodExit(int opcode) {
                if (!traceOpened || opcode == ATHROW) {
                    return;
                }
                mv.visitMethodInsn(INVOKESTATIC, Constants.DEFAULT_TRACE_METHOD_BEAT_CLASS,
                        Constants.DEFAULT_TRACE_METHOD_BEAT_END,
                        "()V", false);
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                if (traceOpened) {
                    Label handler = new Label();
                    visitLabel(traceEnd);
                    visitTryCatchBlock(traceStart, traceEnd, handler, null);
                    visitLabel(handler);
                    visitMethodInsn(INVOKESTATIC, Constants.DEFAULT_TRACE_METHOD_BEAT_CLASS,
                            Constants.DEFAULT_TRACE_METHOD_BEAT_END,
                            "()V", false);
                    throwException();
                }
                super.visitMaxs(maxStack, maxLocals);
            }
        };
    }

    private boolean shouldTraceMethod(int access, String name) {
        if ((access & Opcodes.ACC_ABSTRACT) != 0 || (access & Opcodes.ACC_NATIVE) != 0) {
            return false;
        }
        if (!traceSyntheticMethods && (access & Opcodes.ACC_SYNTHETIC) != 0) {
            return false;
        }
        if (!traceBridgeMethods && (access & Opcodes.ACC_BRIDGE) != 0) {
            return false;
        }
        if (!traceConstructors && "<init>".equals(name)) {
            return false;
        }
        return traceClassInitializers || !"<clinit>".equals(name);
    }

    static String buildSectionName(String className, String methodName, String desc) {
        String safeClassName = className == null ? "" : className;
        String safeMethodName = methodName == null ? "" : methodName;
        String safeDesc = desc == null ? "" : desc;
        String readableName = safeClassName + "." + safeMethodName;
        String identity = readableName + safeDesc;
        String hashSuffix = "#" + Integer.toUnsignedString(identity.hashCode(), 36);
        int maxReadableLength = Constants.MAX_SECTION_NAME_LEN - hashSuffix.length();
        if (readableName.length() <= maxReadableLength) {
            return readableName + hashSuffix;
        }
        String methodSuffix = "." + safeMethodName;
        if (methodSuffix.length() < maxReadableLength) {
            int classNameLength = maxReadableLength - methodSuffix.length();
            return safePrefix(safeClassName, classNameLength) + methodSuffix + hashSuffix;
        }
        return safePrefix(safeMethodName, maxReadableLength) + hashSuffix;
    }

    private static String safePrefix(String value, int maxLength) {
        if (maxLength <= 0) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        int endIndex = maxLength;
        if (endIndex > 0 && Character.isHighSurrogate(value.charAt(endIndex - 1))) {
            endIndex--;
        }
        return value.substring(0, endIndex);
    }
}
