package com.androidperf.systrace;

import com.androidperf.systrace.method.TraceMethod;
import com.androidperf.systrace.tools.Constants;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.HashMap;

public class SystraceTagAdd extends ClassVisitor {
    private String mClassName;
    private boolean isABSClass = false;
    private String[] mInterfaces;

    private final HashMap<String, TraceMethod> mCollectedMethodMap;
    private final HashMap<String, TraceMethod> mCollectedIgnoreMethodMap;
    private final HashMap<String, TraceMethod> mCollectedBlackMethodMap;
    private final HashMap<String, String> mCollectedClassExtendMap;


    public SystraceTagAdd(int api, ClassVisitor cv) {
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
        this.mClassName = name;
        if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
            this.isABSClass = true;
        }
        mCollectedClassExtendMap.put(mClassName, superName);
        mInterfaces = interfaces;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("LifecycleClassVisitor : visitMethod : " + name);
        if (isABSClass) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
            methodVisitor = new AdviceAdapter(Opcodes.ASM5, methodVisitor, access, name, desc) {
                @Override
                protected void onMethodEnter() {
                    String sectionName = name;
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
}
