package de.fayard

import com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentFilter
import com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentSelectionWithCurrent
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import org.gradle.api.Transformer
import java.io.File

object PluginConfig {

    /**
     * The name of the extension for configuring the runtime behavior of the plugin.
     *
     * @see org.gradle.plugins.site.SitePluginExtension
     */
    const val EXTENSION_NAME = "buildSrcVersions"

    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }

    const val DEFAULT_LIBS = "Libs"
    const val DEFAULT_VERSIONS = "Versions"
    const val DEFAULT_INDENT = "  "
    const val BENMANES_REPORT_PATH = "build/dependencyUpdates/report.json"

    /** Documentation **/
    fun issue(number: Int) : String = "$buildSrcVersionsUrl/issues/$number"
    val buildSrcVersionsUrl = "https://github.com/jmfayard/buildSrcVersions"
    val issue47UpdatePlugin = "See issue #47: how to update buildSrcVersions itself ${issue(47)}"
    val issue53PluginConfiguration = issue(53)
    val issue54VersionOnlyMode = issue(54)
    val issue19UpdateGradle = issue(19)


    /**
     * We don't want to use meaningless generic libs like Libs.core
     *
     * Found many inspiration for bad libs here https://developer.android.com/jetpack/androidx/migrate
     * **/
    val MEANING_LESS_NAMES: List<String> = listOf(
        "common", "core", "core-testing", "testing", "runtime", "extensions",
        "compiler", "migration", "db", "rules", "runner", "monitor", "loader",
        "media", "print", "io", "media", "collection", "gradle", "android"
    )

    val INITIAL_GITIGNORE = """
.gradle/
build/
"""

    val GRADLE_KDOC = """
See issue 19: How to update Gradle itself?
$issue19UpdateGradle
"""

    val KDOC_LIBS = """
    Generated by $buildSrcVersionsUrl

    Update this file with
      `$ ./gradlew buildSrcVersions`
    """.trimIndent()

    val KDOC_VERSIONS = """
    Generated by $buildSrcVersionsUrl

    Find which updates are available by running
        `$ ./gradlew buildSrcVersions`
    This will only update the comments.

    YOU are responsible for updating manually the dependency version.
    """.trimIndent()


    const val INITIAL_BUILD_GRADLE_KTS = """
plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}
        """



    val moshi = Moshi.Builder().build()

    inline fun <reified T: Any> moshiAdapter(clazz: Class<T> = T::class.java): Lazy<JsonAdapter<T>> = lazy { moshi.adapter(clazz) }

    val dependencyGraphAdapter: JsonAdapter<DependencyGraph> by moshiAdapter()

    internal val extensionAdapter: JsonAdapter<BuildSrcVersionsExtensionImpl> by moshiAdapter()

    fun readGraphFromJsonFile(jsonInput: File): DependencyGraph {
        return dependencyGraphAdapter.fromJson(jsonInput.source().buffer())!!
    }

    val VERSIONS_ONLY_START = "<buildSrcVersions>"
    val VERSIONS_ONLY_END = "</buildSrcVersions>"
    val VERSIONS_ONLY_INTRO = listOf(
        VERSIONS_ONLY_START,
        "Generated by ./gradle buildSrcVersions",
        "See $issue54VersionOnlyMode"
    )

    const val GRADLE_CURRENT_VERSION = "gradleCurrentVersion"
    const val GRADLE_LATEST_VERSION = "gradleLatestVersion"
}
