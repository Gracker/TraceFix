package com.androidperf.systraceplug

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.androidperf.systrace.SystraceTagAdd
import com.androidperf.systrace.tools.LogTools
import com.androidperf.systrace.tools.TraceBuildConfig
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class TraceFixPlugin extends Transform implements Plugin<Project> {
    private static final String TAG = "TraceFixPlugin"
    private TraceBuildConfig mTraceBuildConfig;
    @Override
    void apply(Project project) {
        project.extensions.findByType(AppExtension.class).registerTransform(this)
        mTraceBuildConfig = new TraceBuildConfig()
    }

    @Override
    String getName() {
        return "TraceFix"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        LogTools.i(TAG, "prepare transform ")
        transformInvocation.inputs.each {
            it.directoryInputs.each {
                if (it.file.isDirectory()) {
                    it.file.eachFileRecurse {
                        def fileName = it.name
                        if (mTraceBuildConfig.isNeedTraceClass(fileName)) {
                            handleFile(it)
                        }
                    }
                }
                def dest = transformInvocation.outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(it.file, dest)
            }
            it.jarInputs.each { jarInput ->
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }

    private static void handleFile(File file) {
        def cr = new ClassReader(file.bytes)
        def cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        def classVisitor = new SystraceTagAdd(Opcodes.ASM5, cw)
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        def bytes = cw.toByteArray()
        FileOutputStream fos = new FileOutputStream(file.getParentFile().getAbsolutePath() + File.separator + file.name)
        fos.write(bytes)
        fos.close()
    }


}