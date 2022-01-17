package multiplatform.api

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlEncodeTest {
    @Test
    fun testRealPlus() {
        assertEquals("a%2Bb", "a+b".urlEncode())
    }

    @Test
    fun testSpace() {
        assertEquals("a%20b", "a b".urlEncode())
    }
}
