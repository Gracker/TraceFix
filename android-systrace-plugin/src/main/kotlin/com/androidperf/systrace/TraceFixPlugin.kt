package com.androidperf.systrace

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.androidperf.systrace.tools.LogTools
import org.gradle.api.Plugin
import org.gradle.api.Project

class TraceFixPlugin : Plugin<Project> {
    private var registered = false

    override fun apply(project: Project) {
        registerWhenAndroidPluginPresent(project, "com.android.application")
        registerWhenAndroidPluginPresent(project, "com.android.library")
        registerWhenAndroidPluginPresent(project, "com.android.dynamic-feature")
    }

    private fun registerWhenAndroidPluginPresent(project: Project, pluginId: String) {
        project.pluginManager.withPlugin(pluginId) {
            if (registered) {
                return@withPlugin
            }
            val androidComponents = findAndroidComponents(project)
            if (androidComponents == null) {
                LogTools.w(TAG, "AndroidComponentsExtension not found in project: %s", project.path)
                return@withPlugin
            }
            registerOfficialInstrumentation(project, androidComponents)
            registered = true
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun findAndroidComponents(project: Project): AndroidComponentsExtension<*, *, Variant>? {
        return project.extensions.findByType(AndroidComponentsExtension::class.java)
            as AndroidComponentsExtension<*, *, Variant>?
    }

    private fun registerOfficialInstrumentation(
        project: Project,
        androidComponents: AndroidComponentsExtension<*, *, Variant>
    ) {
        LogTools.i(TAG, "register official class instrumentation for project: %s", project.path)
        androidComponents.onVariants(androidComponents.selector().all()) { variant ->
            variant.instrumentation.transformClassesWith(
                TraceFixAsmClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ) {}
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )
            LogTools.i(TAG, "instrumentation registered for variant: %s", variant.name)
        }
    }

    companion object {
        private const val TAG = "TraceFixPlugin"
    }
}
