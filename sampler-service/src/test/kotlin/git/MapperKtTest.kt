package git

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MapperKtTest {

    @Test
    fun testBase64toUtf8_1() {
        assertEquals(
            "a290bGluLmNvZGUuc3R5bGU9b2ZmaWNpYWw=\n".base64toUtf8(),
            "kotlin.code.style=official"
        )
    }

    @Test
    fun testBase64toUtf8_2() {
        assertEquals(
            ("aW1wb3J0IG9yZy5lY2xpcHNlLmVnaXQuZ2l0aHViLmNvcmUuUmVwb3NpdG9y\n" +
                    "eUlkCmltcG9ydCBvcmcuZWNsaXBzZS5lZ2l0LmdpdGh1Yi5jb3JlLmNsaWVu\n" +
                    "dC5HaXRIdWJDbGllbnQKaW1wb3J0IG9yZy5lY2xpcHNlLmVnaXQuZ2l0aHVi\n").base64toUtf8(),
            "import org.eclipse.egit.github.core.RepositoryId\n" +
                    "import org.eclipse.egit.github.core.client.GitHubClient\n" +
                    "import org.eclipse.egit.github"
        )
    }
}