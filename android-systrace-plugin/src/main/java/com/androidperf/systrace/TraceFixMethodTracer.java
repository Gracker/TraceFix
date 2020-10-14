package com.androidperf.systrace;

import com.androidperf.systrace.method.TraceMethod;
import com.androidperf.systrace.tools.Constants;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TraceFixMethodTracer extends ClassVisitor {
    private final HashMap<String, TraceMethod> mCollectedMethodMap;
    private final HashMap<String, TraceMethod> mCollectedIgnoreMethodMap;
    private final HashMap<String, TraceMethod> mCollectedBlackMethodMap;
    private final HashMap<String, String> mCollectedClassExtendMap;
    private final boolean isActivityOrSubClass = false;
    private String mClassName;
    private boolean isABSClass = false;
    private String[] mInterfaces;
    private boolean hasWindowFocusMethod = false;

    public TraceFixMethodTracer(int api, ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
        this.mCollectedMethodMap = new HashMap<>();
        this.mCollectedClassExtendMap = new HashMap<>();
        this.mCollectedIgnoreMethodMap = new HashMap<>();
        this.mCollectedBlackMethodMap = new HashMap<>();
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        System.out.println("LifecycleClassVisitor : visit -----> started : " + name);
        super.visit(version, access, name, signature, superName, interfaces);
        this.mClassName = name.replace("/", ".");
        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
            this.isABSClass = true;
        }
        mCollectedClassExtendMap.put(mClassName, superName);
        mInterfaces = interfaces;
//        this.isActivityOrSubClass = isActivityOrSubClass(mClassName, mCollectedClassExtendMap);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("LifecycleClassVisitor : visitMethod -----> " + name);
        if (isABSClass) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            if (!hasWindowFocusMethod) {
                hasWindowFocusMethod = TraceMethod.isWindowFocusChangeMethod(name, desc);
            }
            MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
            methodVisitor = new AdviceAdapter(Opcodes.ASM5, methodVisitor, access, name, desc) {
                @Override
                protected void onMethodEnter() {
                    String sectionName = mClassName + "." + name;
                    System.out.println("LifecycleClassVisitor : onMethodEnter : " + sectionName);
                    int length = sectionName.length();
                    if (length > Constants.MAX_SECTION_NAME_LEN) {
                        int parmIndex = sectionName.indexOf('(');
                        sectionName = sectionName.substring(0, parmIndex);
                        length = sectionName.length();
                        if (length > Constants.MAX_SECTION_NAME_LEN) {
                            sectionName = sectionName.substring(length - Constants.MAX_SECTION_NAME_LEN);
                        }
                    }
                    mv.visitLdcInsn(sectionName);
                    mv.visitMethodInsn(INVOKESTATIC, Constants.DEFAULT_TRACE_METHOD_BEAT_CLASS,
                            Constants.DEFAULT_TRACE_METHOD_BEAT_BEGIN,
                            "(Ljava/lang/String;)V", false);
                }

                @Override
                protected void onMethodExit(int opcode) {
                    mv.visitMethodInsn(INVOKESTATIC, Constants.DEFAULT_TRACE_METHOD_BEAT_CLASS,
                            Constants.DEFAULT_TRACE_METHOD_BEAT_END,
                            "()V", false);
                }
            };
            return methodVisitor;
        }
    }

    @Override
    public void visitEnd() {
//        if (!hasWindowFocusMethod && isActivityOrSubClass && isNeedTrace) {
//            insertWindowFocusChangeMethod(cv, className);
//        }
        super.visitEnd();
    }

    private void insertWindowFocusChangeMethod(ClassVisitor cv, String classname) {
        MethodVisitor methodVisitor = cv.visitMethod(Opcodes.ACC_PUBLIC, Constants.WINDOW_FOCUS_METHOD,
                Constants.WINDOW_FOCUS_METHOD_ARGS, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 1);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Constants.ACTIVITY_CLASS,
                Constants.WINDOW_FOCUS_METHOD,
                Constants.WINDOW_FOCUS_METHOD_ARGS,
                false);
        //traceWindowFocusChangeMethod(methodVisitor, classname);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }

    private boolean isActivityOrSubClass(String className, ConcurrentHashMap<String, String> mCollectedClassExtendMap) {
        className = className.replace(".", "/");
        boolean isActivity = className.equals(Constants.ACTIVITY_CLASS)
                || className.equals(Constants.V4_ACTIVITY_CLASS)
                || className.equals(Constants.V7_ACTIVITY_CLASS);
        if (isActivity) {
            return true;
        } else {
            if (!mCollectedClassExtendMap.containsKey(className)) {
                return false;
            } else {
                return isActivityOrSubClass(mCollectedClassExtendMap.get(className), mCollectedClassExtendMap);
            }
        }
    }


}
