package multiplatform.graphql

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind

@OptIn(ExperimentalSerializationApi::class)
internal object GraphQLQueryBuilder {
    fun buildQuery(descriptor: SerialDescriptor): String {
        require(descriptor.kind == StructureKind.CLASS) // TODO allow other kinds as root?

        return buildString {
            val vars = descriptor.annotations.filterIsInstance<GraphQLVariable>()
            if (vars.isNotEmpty()) {
                append("query")
                append(vars.joinToString(separator = ",", prefix = "(", postfix = ")") {
                    "\$${it.name}:${it.type}${if (it.default.isNotBlank()) "=${it.default}" else ""}"
                })
            }
            outputClass(descriptor)
        }
    }

    fun StringBuilder.output(descriptor: SerialDescriptor) {
        when (descriptor.kind) {
            is PrimitiveKind -> return
            StructureKind.CLASS -> outputClass(descriptor)
            StructureKind.LIST -> outputList(descriptor)
            else -> throw IllegalArgumentException("Unsupported kind: ${descriptor.kind}")
        }
    }

    fun StringBuilder.outputClass(descriptor: SerialDescriptor) {
        append('{')
        for (i in 0 until descriptor.elementsCount) {
            if (i != 0) {
                append(' ')
            }
            append(descriptor.getElementName(i))
            val args = descriptor.getElementAnnotations(i).filterIsInstance<GraphQLArgument>()
            if (args.isNotEmpty()) {
                append(args.joinToString(separator = ",", prefix = "(", postfix = ")") { "${it.name}:${it.value}" })
            }
            output(descriptor.getElementDescriptor(i))
        }
        append('}')
    }

    fun StringBuilder.outputList(descriptor: SerialDescriptor) {
        val inner = descriptor.getElementDescriptor(0)
        output(inner)
    }
}
