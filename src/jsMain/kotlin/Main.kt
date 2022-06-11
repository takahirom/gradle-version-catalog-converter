import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

class Lib(val group: String, val name: String, val version: String) {
    fun String.capitarize(upper: Boolean = false): String {
        val str = this
        return buildString {
            var first = upper
            str.forEach {
                if (it == '.'|| it == '-') {
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

    fun groupName() = group.capitarize()
    fun nameName() = groupName() + name.capitarize(true)

}

@NoLiveLiterals
fun main() {
    var text: String by mutableStateOf(
        """implementation 'androidx.core:core-ktx:1.7.0'
        |implementation "androidx.compose.ui:ui:${'$'}compose_version"""".trimMargin()

    )
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
                    appendLine(libs[0].groupName() + " = " + libs[0].version)
                }
            appendLine("[libraries]")
            libs
                .forEach { lib: Lib ->
                    // groovy-core = { module = "org.codehaus.groovy:groovy", version.ref = "groovy" }
                    appendLine(lib.nameName() + " = { module = \"${lib.group}:${lib.name}\", version.ref = \"${lib.groupName()}\" }")
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
            Span({ style { padding(15.px) } }) {
                Pre {
                    Text("$catalog")
                }
            }
        }
    }
}

