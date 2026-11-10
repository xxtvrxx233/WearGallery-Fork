import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

private const val TARGET_APK_FOLDER = "uni-outs/apk"
private const val TARGET_MAPPING_FOLDER = "uni-outs/mapping"

/**
 * Copies build outputs (APK / mapping) into the root project build directory
 * so that CI can pick them up from a single place.
 */
class CopyOutsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("copyOuts", CopyOutsExtension::class.java)
        val rootBuildDir = project.rootProject.layout.buildDirectory

        project.plugins.withId("com.android.application") {
            val copyApk = project.tasks.register("copyApk", Copy::class.java) {
                dependsOn("assembleRelease")
                onlyIf { ext.copyApk }
                from(project.layout.buildDirectory.dir("outputs/apk/release"))
                into(rootBuildDir.dir(TARGET_APK_FOLDER))
                include("*.apk")
            }
            project.tasks.register("copyMapping", Copy::class.java) {
                dependsOn(copyApk)
                onlyIf { ext.copyApk }
                from(project.layout.buildDirectory.file("outputs/mapping/release/mapping.txt"))
                into(rootBuildDir.dir("$TARGET_MAPPING_FOLDER/${project.name}"))
                include("mapping.txt")
            }
        }

    }
}
