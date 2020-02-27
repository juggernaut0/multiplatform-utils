package multiplatform.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.UnitSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class PathTemplateTest {
    @Serializable
    class Params(
            val a: String? = null,
            val b: String? = null,
            val c: String? = null,
            val d: String? = null,
            val e: String? = null,
            val f: String? = null,
            val g: String? = null,
            val h: String? = null
    )

    @Test
    fun nothing() {
        val path = pathOf(UnitSerializer, "/a/b/c")
        val applied = path.applyParams(Unit)
        assertEquals("/a/b/c", applied)
    }

    @Test
    fun simple() {
        val path = pathOf(Params.serializer(), "/a/{b}/c")
        val applied = path.applyParams(
            Params(
                b = "bpar",
                c = "fail"
            )
        )
        assertEquals("/a/bpar/c", applied)
    }

    @Test
    fun query() {
        val path = pathOf(Params.serializer(), "/a/{b}/c?d={d}&{e}&f=f&g&h={h}")
        val applied = path.applyParams(
            Params(
                b = "bpar",
                d = "dpar",
                e = "epar",
                g = "fail",
                h = null
            )
        )
        assertEquals("/a/bpar/c?d=dpar&epar&f=f&g", applied)
    }

    @Test
    fun missingQuery() {
        val path = pathOf(Params.serializer(), "/a?b={b}")
        val applied = path.applyParams(Params(b = null))
        assertEquals("/a", applied)
    }

    @Test
    fun fragment() {
        val path = pathOf(Params.serializer(), "/a/{b}/c#{d}")
        val applied = path.applyParams(
            Params(
                b = "bpar",
                d = "dpar"
            )
        )
        assertEquals("/a/bpar/c#dpar", applied)
    }

    @Test
    fun missingFragment() {
        val path = pathOf(Params.serializer(), "/a#{b}")
        val applied = path.applyParams(Params(b = null))
        assertEquals("/a", applied)
    }

    @Test
    fun queryAndFragment() {
        val path = pathOf(Params.serializer(), "/{a}?b={b}#{c}")
        val applied = path.applyParams(
            Params(
                a = "apar",
                b = "bpar",
                c = "cpar"
            )
        )
        assertEquals("/apar?b=bpar#cpar", applied)
    }

    @Test
    fun missingQueryAndFragment() {
        val path = pathOf(Params.serializer(), "/{a}?b={b}#{c}")
        val applied = path.applyParams(
            Params(
                a = "apar",
                b = null,
                c = "cpar"
            )
        )
        assertEquals("/apar#cpar", applied)
    }

    @Test
    fun queryAndMissingFragment() {
        val path = pathOf(Params.serializer(), "/{a}?b={b}#{c}")
        val applied = path.applyParams(
            Params(
                a = "apar",
                b = "bpar",
                c = null
            )
        )
        assertEquals("/apar?b=bpar", applied)
    }

    @Test
    fun missingQueryAndMissingFragment() {
        val path = pathOf(Params.serializer(), "/{a}?b={b}#{c}")
        val applied = path.applyParams(
            Params(
                a = "apar",
                b = null,
                c = null
            )
        )
        assertEquals("/apar", applied)
    }

    @Test
    fun urlEscaped() {
        val path  = pathOf(Params.serializer(), "/api/{a}/b")
        val applied = path.applyParams(
            Params(
                a = "x/y"
            )
        )
        assertEquals("/api/x%2Fy/b", applied)
    }
}
