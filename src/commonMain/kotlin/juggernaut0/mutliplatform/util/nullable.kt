package juggernaut0.mutliplatform.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.makeNullable

val <T : Any> KSerializer<T>.nullable: KSerializer<T?> get() = makeNullable(this)
