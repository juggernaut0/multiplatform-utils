package multiplatform.graphql

import graphql.Scalars
import graphql.schema.*
import kotlinx.coroutines.future.future
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.properties.Properties

inline fun schema(builder: SchemaBuilder.() -> Unit): GraphQLSchema = SchemaBuilder().also(builder).build()

@OptIn(ExperimentalSerializationApi::class)
class SchemaBuilder {
    private val builder = GraphQLSchema.newSchema()
    internal val codeRegistry = GraphQLCodeRegistry.newCodeRegistry()

    fun query(queryFields: QueryBuilder.() -> Unit) {
        QueryBuilder(this).also(queryFields).build().also { builder.query(it) }
    }

    inline fun <T> type(ser: KSerializer<T>, extraFields: FieldBuilder<T>.() -> Unit) {
        FieldBuilder(ser, this).also(extraFields).build().also { addType(it) }
    }

    fun addType(objectType: GraphQLType) {
        builder.additionalType(objectType)
    }

    fun build(): GraphQLSchema {
        builder.codeRegistry(codeRegistry.build())
        return builder.build()
    }
}

@OptIn(ExperimentalSerializationApi::class)
class QueryBuilder(private val schemaBuilder: SchemaBuilder) {
    private val builder = GraphQLObjectType.newObject().name("Query")

    fun <R> field(name: String, ser: KSerializer<R>, resolver: suspend () -> R) {
        val field = GraphQLFieldDefinition.newFieldDefinition()
        field.name(name)
        field.type(ser.descriptor.toGraphQLOutputType())
        builder.field(field)
        val dataFetcher = DataFetcher {
            it.coroutineScope.future { resolver() }
        }
        schemaBuilder.codeRegistry.dataFetcher(FieldCoordinates.coordinates("Query", name), dataFetcher)
    }

    fun <R, P> field(name: String, ser: KSerializer<R>, paramsSer: KSerializer<P>, resolver: suspend (P) -> R) {
        val field = GraphQLFieldDefinition.newFieldDefinition()
        field.name(name)
        field.type(ser.descriptor.toGraphQLOutputType())
        field.argumentsFromDescriptor(paramsSer.descriptor)
        builder.field(field)
        val dataFetcher = DataFetcher {
            val args = Properties.decodeFromMap(paramsSer, it.arguments)
            it.coroutineScope.future { resolver(args) }
        }
        schemaBuilder.codeRegistry.dataFetcher(FieldCoordinates.coordinates("Query", name), dataFetcher)
    }

    fun build(): GraphQLObjectType {
        return builder.build()
    }
}

@OptIn(ExperimentalSerializationApi::class)
class FieldBuilder<T>(private val parentSer: KSerializer<T>, private val schemaBuilder: SchemaBuilder) {
    private val myName: String
    private val builder = GraphQLObjectType.newObject()

    init {
        val descriptor = parentSer.descriptor
        myName = descriptor.serialName.split('.').last()
        builder.name(myName)
        for (i in 0 until descriptor.elementsCount) {
            val name = descriptor.getElementName(i)
            val desc = descriptor.getElementDescriptor(i)
            builder.field(fieldFromSer(name, desc))
        }
    }

    private fun fieldFromSer(name: String, descriptor: SerialDescriptor): GraphQLFieldDefinition {
        val builder = GraphQLFieldDefinition.newFieldDefinition()
        builder.name(name)
        builder.type(descriptor.toGraphQLOutputType())
        val dataFetcher = DataFetcher {
            val parent: T = it.getSource()
            var field: Any? = null
            val extractorEncoder = FieldExtractorEncoder(name) { v -> field = v } }
            parentSer.serialize(extractorEncoder, parent)
            field
        }
        schemaBuilder.codeRegistry.dataFetcher(FieldCoordinates.coordinates(myName, name), dataFetcher)
        return builder.build()
    }

    fun <R> field(name: String, ser: KSerializer<R>, resolver: suspend T.() -> R) {
        val field = GraphQLFieldDefinition.newFieldDefinition()
        field.name(name)
        field.type(ser.descriptor.toGraphQLOutputType())
        builder.field(field)
        val dataFetcher = DataFetcher {
            val parent: T = it.getSource()
            it.coroutineScope.future { parent.resolver() }
        }
        schemaBuilder.codeRegistry.dataFetcher(FieldCoordinates.coordinates(myName, name), dataFetcher)
    }

    fun <R, P> field(name: String, ser: KSerializer<R>, paramsSer: KSerializer<P>, resolver: suspend T.(P) -> R) {
        val field = GraphQLFieldDefinition.newFieldDefinition()
        field.name(name)
        field.type(ser.descriptor.toGraphQLOutputType())
        field.argumentsFromDescriptor(paramsSer.descriptor)
        builder.field(field)
        val dataFetcher = DataFetcher {
            val parent: T = it.getSource()
            val args = Properties.decodeFromMap(paramsSer, it.arguments)
            it.coroutineScope.future { parent.resolver(args) }
        }
        schemaBuilder.codeRegistry.dataFetcher(FieldCoordinates.coordinates(myName, name), dataFetcher)
    }

    fun build(): GraphQLObjectType {
        return builder.build()
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun GraphQLFieldDefinition.Builder.argumentsFromDescriptor(descriptor: SerialDescriptor) {
    for (i in 0 until descriptor.elementsCount) {
        val paramName = descriptor.getElementName(i)
        val paramDesc = descriptor.getElementDescriptor(i)
        val argument = graphql.schema.GraphQLArgument.newArgument()
            .name(paramName)
            .type(paramDesc.toGraphQLInputType())
        argument(argument)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun SerialDescriptor.toGraphQLType(): GraphQLType {
    val kind = kind
    val baseType = if (kind is PrimitiveKind) {
        when (kind) {
            PrimitiveKind.BOOLEAN -> Scalars.GraphQLBoolean
            PrimitiveKind.BYTE -> Scalars.GraphQLByte
            PrimitiveKind.CHAR -> Scalars.GraphQLChar
            PrimitiveKind.SHORT -> Scalars.GraphQLShort
            PrimitiveKind.INT -> Scalars.GraphQLInt
            PrimitiveKind.LONG -> Scalars.GraphQLLong
            PrimitiveKind.FLOAT -> Scalars.GraphQLFloat
            PrimitiveKind.DOUBLE -> Scalars.GraphQLFloat
            PrimitiveKind.STRING -> Scalars.GraphQLString
        }
    } else {
        GraphQLTypeReference(serialName.split('.').last())
    }
    return baseType.let { if (isNullable) it else GraphQLNonNull(it) }
}

// TODO figure out where these break
private fun SerialDescriptor.toGraphQLOutputType(): GraphQLOutputType = toGraphQLType() as GraphQLOutputType
private fun SerialDescriptor.toGraphQLInputType(): GraphQLInputType = toGraphQLType() as GraphQLInputType
