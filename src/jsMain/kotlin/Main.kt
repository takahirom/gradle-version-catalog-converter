import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.background
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

class Lib(val group: String, val name: String, val version: String) {
    private fun String.capitarize(upper: Boolean = false): String {
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

    private fun String.hyphenation(): String {
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

    fun groupName(useHyphen: Boolean) = if (useHyphen) group.hyphenation() else group.capitarize()
    fun nameName(useHyphen: Boolean) = if (useHyphen) {
        group.hyphenation() + "-" + name.hyphenation()
    } else {
        groupName(false) + name.capitarize(true)
    }

}

@NoLiveLiterals
fun main() {
    var text: String by mutableStateOf(
        """object AndroidX {
  val coreKtx = "androidx.core:core-ktx:1.7.0"
}
implementation 'androidx.activity:activity-compose:1.3.1'
testImplementation 'junit:junit:4.13.2'
androidTestImplementation 'androidx.test.ext:junit:1.1.3'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
implementation "androidx.compose.ui:ui:${'$'}compose_version"""""

    )
    var useHyphenForVersion: Boolean by mutableStateOf<Boolean>(false)
    var useHyphenForLibraries: Boolean by mutableStateOf<Boolean>(false)
    val catalog: String by derivedStateOf {
        buildString {
            val libs = text
                .lines()
                .mapNotNull { line ->
                    try {
                        if (line.contains("'")) {
                            val (group, name, version) = line.trimStart { it != '\'' }.trimEnd { it != '\'' }
                                .drop(1).dropLast(1)
                                .split(":")
                            Lib(group, name, version)
                        } else if (line.contains("\"")) {
                            val (group, name, version) = line.trimStart { it != '"' }.trimEnd { it != '"' }
                                .drop(1).dropLast(1)
                                .split(":")

                            Lib(group, name, version)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            appendLine("[versions]")
            libs.groupBy { it.group }
                .forEach { (_, libs: List<Lib>) ->
                    appendLine(libs[0].groupName(useHyphenForVersion) + " = \"" + libs[0].version + "\"")
                }
            appendLine("[libraries]")
            libs
                .forEach { lib: Lib ->
                    // groovy-core = { module = "org.codehaus.groovy:groovy", version.ref = "groovy" }
                    appendLine(
                        lib.nameName(useHyphenForLibraries) + " = { module = \"${lib.group}:${lib.name}\", version.ref = \"${
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
            Text("Use \"-\" as version name:")
            CheckboxInput {
                checked(useHyphenForVersion)
                onChange { useHyphenForVersion = it.value }
            }
            Br()
            Text("Use \"-\" as library name:")
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

