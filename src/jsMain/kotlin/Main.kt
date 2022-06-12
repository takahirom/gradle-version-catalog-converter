import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

sealed interface Lib {
    class Library(
        val group: String,
        val name: String,
        override val version: String,
    ) : Lib {
        override fun groupName(useHyphen: Boolean) = if (useHyphen) group.hyphenation() else group.capitarize()
        fun nameName(useHyphen: Boolean) = if (useHyphen) {
            group.hyphenation() + "-" + name.hyphenation()
        } else {
            groupName(false) + name.capitarize(true)
        }
    }

    class Plugin(
        val id: String,
        override val version: String,
    ) : Lib {
        override fun groupName(useHyphen: Boolean) = if (useHyphen) id.hyphenation() else id.capitarize()

        fun idName(useHyphen: Boolean) = groupName(useHyphen)
    }

    fun groupName(useHyphen: Boolean): String

    val version: String

    fun String.capitarize(upper: Boolean = false): String {
        val str = this
        return buildString {
            var first = upper
            str.forEach {
                if (it == '.' || it == '-') {
                    first = true
                } else {
                    if (first) {
                        append(it.uppercase())
                        first = false
                    } else {
                        append(it)
                    }
                }
            }
        }
    }

    fun String.hyphenation(): String {
        val str = this
        return buildString {
            str.forEach {
                if (it == '.' || it == '-') {
                    append("-")
                } else {
                    append(it)
                }
            }
        }
    }

}

@NoLiveLiterals
fun main() {
    var text: String by mutableStateOf(
        """object AndroidX {
  val coreKtx = "androidx.core:core-ktx:1.7.0"
}
  id "org.jetbrains.kotlin.android" version "1.6.10" apply false
implementation 'androidx.activity:activity-compose:1.3.1'
testImplementation 'junit:junit:4.13.2'
androidTestImplementation 'androidx.test.ext:junit:1.1.3'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
implementation "androidx.compose.ui:ui:${'$'}compose_version"""""

    )
    var useHyphenForVersion: Boolean by mutableStateOf<Boolean>(false)
    var useHyphenForLibraries: Boolean by mutableStateOf<Boolean>(true)
    val catalog: String by derivedStateOf {
        buildString {
            val libs = text
                .lines()
                .mapNotNull { line ->
                    try {
                        if (line.contains("id") && line.contains("version")) {
                            val notQuote = "[^'\"]+"
                            val quote = "['\"]"
                            val match =
                                Regex("$notQuote$quote($notQuote)$quote$notQuote$quote($notQuote)$quote$notQuote").find(
                                    line
                                )!!
                            val (id, version) = match.destructured
                            Lib.Plugin(id, version)
                        } else if (line.contains("'")) {
                            val (group, name, version) = line.trimStart { it != '\'' }.trimEnd { it != '\'' }
                                .drop(1).dropLast(1)
                                .split(":")
                            Lib.Library(group, name, version)
                        } else if (line.contains("\"")) {
                            val (group, name, version) = line.trimStart { it != '"' }.trimEnd { it != '"' }
                                .drop(1).dropLast(1)
                                .split(":")

                            Lib.Library(group, name, version)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            appendLine("[versions]")
            libs.groupBy { it.groupName(useHyphenForVersion) }
                .forEach { (versionName, libs: List<Lib>) ->
                    appendLine(versionName + " = \"" + libs[0].version + "\"")
                }
            appendLine()
            appendLine("[libraries]")
            libs
                .filterIsInstance<Lib.Library>()
                .forEach { lib: Lib.Library ->
                    // groovy-core = { module = "org.codehaus.groovy:groovy", version.ref = "groovy" }
                    appendLine(
                        lib.nameName(useHyphenForLibraries) + " = { module = \"${lib.group}:${lib.name}\", version.ref = \"${
                            lib.groupName(
                                useHyphenForVersion
                            )
                        }\" }"
                    )
                }
            appendLine()
            appendLine("[plugins]")
            libs
                .filterIsInstance<Lib.Plugin>()
                .forEach { lib: Lib.Plugin ->
                    // groovy-core = { module = "org.codehaus.groovy:groovy", version.ref = "groovy" }
                    appendLine(
                        lib.idName(useHyphenForLibraries) + " = { id = \"${lib.id}\", version.ref = \"${
                            lib.groupName(
                                useHyphenForVersion
                            )
                        }\" }"
                    )
                }
        }
    }
    renderComposable(rootElementId = "root") {
        Div({ style { padding(25.px) } }) {
            TextArea {
                value(text)
                onInput {
                    text = it.value
                }
            }
            Br()
            Text("Use \"-\" for version name:")
            CheckboxInput {
                checked(useHyphenForVersion)
                onChange { useHyphenForVersion = it.value }
            }
            Br()
            Text("Use \"-\" for library name:")
            CheckboxInput {
                checked(useHyphenForLibraries)
                onChange { useHyphenForLibraries = it.value }
            }
            Br()
            Span({
                style {
                    padding(15.px)
                }
            }) {
                Pre({
                    style {
                        padding(15.px)
                        background("#CCCCCC")
                    }
                }) {
                    Text("$catalog")
                }
            }
        }
    }
}

