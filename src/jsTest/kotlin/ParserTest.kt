import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun parserTest() {
        assertEquals(
            "implementation \"androidx.compose.ui:ui:${'$'}compose_version\""
                .parseLibraries()[0],
            Lib.Library("androidx.compose.ui", "ui", "${'$'}compose_version")
        )
    }
}
