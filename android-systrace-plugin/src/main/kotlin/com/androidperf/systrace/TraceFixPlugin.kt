package com.androidperf.systrace

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.androidperf.systrace.tools.LogTools
import com.androidperf.systrace.tools.TraceBuildConfig
import groovy.lang.Closure
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutionException

class TraceFixPlugin : Transform(),
    Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.findByType(AppExtension::class.java)!!.registerTransform(this)
        mTraceBuildConfig = TraceBuildConfig()
    }

    override fun getName(): String {
        return "TraceFix"
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_JARS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    @kotlin.jvm.Throws(
        TransformException::class,
        InterruptedException::class,
        IOException::class
    )
    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        LogTools.i(TAG, "prepare transform ")
        val start = System.currentTimeMillis()
        try {
            doTransform(transformInvocation)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        LogTools.i(TAG, "[Transform] cost time: %dms ", System.currentTimeMillis() - start)
    }

    @kotlin.jvm.Throws(ExecutionException::class, InterruptedException::class)
    private fun doTransform(transformInvocation: TransformInvocation) {
        val isIncremental = transformInvocation.isIncremental && this.isIncremental
        DefaultGroovyMethods.each(
            transformInvocation.inputs,
            object : Closure<Collection<JarInput?>?>(this, this) {
                fun doCall(it: TransformInput? = null): Collection<JarInput> {
                    DefaultGroovyMethods.each(
                        it!!.directoryInputs,
                        object : Closure<Any?>(this@TraceFixPlugin, this@TraceFixPlugin) {
                            fun doCall(it: DirectoryInput? = null) {
                                if (it!!.file.isDirectory) {
                                    ResourceGroovyMethods.eachFileRecurse(
                                        it.file,
                                        object : Closure<Any?>(
                                            this@TraceFixPlugin,
                                            this@TraceFixPlugin
                                        ) {
                                            fun doCall(it: File? = null) {
                                                val fileName = it!!.name
                                                if (mTraceBuildConfig!!.isNeedTraceClass(fileName)) {
                                                    handleFile(it)
                                                }
                                            }
                                        })
                                }
                                val dest = transformInvocation.outputProvider.getContentLocation(
                                    it.name, it.contentTypes, it.scopes, Format.DIRECTORY
                                )
                                FileUtils.copyDirectory(it.file, dest)
                            }
                        })
                    return DefaultGroovyMethods.each(
                        it.jarInputs,
                        object : Closure<Any?>(this@TraceFixPlugin, this@TraceFixPlugin) {
                            fun doCall(jarInput: Any) {
                                var jarName = (jarInput as JarInput).name
                                val md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                                if (jarName.endsWith(".jar")) {
                                    jarName = jarName.substring(0, jarName.length - 4)
                                }
                                val dest = transformInvocation.outputProvider.getContentLocation(
                                    jarName + md5Name,
                                    jarInput.contentTypes,
                                    jarInput.scopes,
                                    Format.JAR
                                )
                                FileUtils.copyFile(jarInput.file, dest)
                            }
                        })
                }
            })
    }

    private var mTraceBuildConfig: TraceBuildConfig? = null

    companion object {
        private fun handleFile(file: File?) {
            LogTools.i(TAG, "handleFile start")
            val cr = ClassReader(ResourceGroovyMethods.getBytes(file))
            val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
            val classVisitor = TraceFixMethodTracer(Opcodes.ASM5, cw)
            cr.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            val bytes: ByteArray? = cw.toByteArray()
            val fos = FileOutputStream(file!!.parentFile.absolutePath + File.separator + file.name)
            fos.write(bytes)
            fos.close()
            LogTools.i(TAG, "handleFile end")
        }

        private const val TAG = "TraceFixPlugin"
    }
}