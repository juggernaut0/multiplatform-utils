package multiplatform

actual typealias UUID = java.util.UUID

internal actual fun fromString(str: String) = UUID.fromString(str)
