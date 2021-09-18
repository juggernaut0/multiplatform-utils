package multiplatform

import asynclite.async
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertTrue

class FetchTest {
    @Test
    fun nullHeaders() = async {
        var called = false

        val fetcher = { _: dynamic, init: RequestInit ->
            called = true
            assertTrue { init.headers === undefined }
            assertTrue { init.body === undefined }
            Promise.resolve(Response(""))
        }

        fetch(fetcher,"GET", "something", null, null)
        assertTrue(called)
    }
}
