package multiplatform.graphql

import graphql.Scalars
import graphql.schema.*
import kotlinx.coroutines.future.future
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.properties.Properties

inline fun schema(builder: SchemaBuilder.() -> Unit): GraphQLSchema = SchemaBuilder().also(builder).build()

@OptIn(ExperimentalSerializationApi::class)
class SchemaBuilder {
    private val builder = GraphQLSchema.newSchema()
    internal val codeRegistry = GraphQLCodeRegistry.newCodeRegistry()

    fun query(queryFields: QueryBuilder.() -> Unit) {
        QueryBuilder(this).also(queryFields).build().also { builder.query(it) }
    }

    fun type(ser: KSerializer<*>) {
        FieldBuilder(ser, this).build().also { addType(it) }
    }

    inline fun <T> type(ser: KSerializer<T>, extraFields: FieldBuilder<T>.() -> Unit) {
        FieldBuilder(ser, this).also(extraFields).build().also { addType(it) }
    }

    /**
     * kotlinx.serialization does not expose base class fields so you must specify them explicitly. The types will be
     * inferred from the subclasses. If no common fields are specified, they will be inferred from the subclasses.
     */
    fun <T: Any> `interface`(ser: KSerializer<T>, vararg commonFields: String) {
        val (ifType, subtypes) = InterfaceBuilder(ser, commonFields.toList(), this).build()
        addType(ifType)
        subtypes.forEach { addType(it) }
    }

    inline fun <T: Any> `interface`(ser: KSerializer<T>, vararg commonFields: String, subtypeBuilder: InterfaceBuilder<T>.() -> Unit) {
        val (ifType, subtypes) = InterfaceBuilder(ser, commonFields.toList(), this).also(subtypeBuilder).build()
        addType(ifType)
        subtypes.forEach { addType(it) }
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
class FieldBuilder<T>(descriptor: SerialDescriptor, private val schemaBuilder: SchemaBuilder) {
    private val myName: String = descriptor.serialName.split('.').last()
    internal val builder = GraphQLObjectType.newObject()

    constructor(parentSer: KSerializer<T>, schemaBuilder: SchemaBuilder) : this(parentSer.descriptor, schemaBuilder)

    init {
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

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class InterfaceBuilder<T: Any>(ser: KSerializer<T>, commonFields: List<String>, private val schemaBuilder: SchemaBuilder) {
    private val subtypeExtras: MutableMap<SerialDescriptor, FieldBuilder<*>.() -> Unit> = mutableMapOf()
    private val builder = GraphQLInterfaceType.newInterface()
    private val baseDesc: SerialDescriptor
    private val ser: SealedClassSerializer<T>

    init {
        val name = ser.descriptor.serialName.split('.').last()
        require(ser is SealedClassSerializer<T>) { "$name must be a sealed class in order to create an interface" }
        this.ser = ser
        baseDesc = ser.descriptor.getElementDescriptor(1)
        require(baseDesc.elementsCount > 0) { "$name must have at least one implementation in order to create an interface" }

        val ifTypeBuilder = builder.name(name)

        val ifFields = if (commonFields.isEmpty()) {
            // infer common fields from subtypes
            (0 until baseDesc.elementsCount)
                .map { baseDesc.getElementDescriptor(it) }
                .map { desc ->
                    (0 until desc.elementsCount).map { i -> desc.getElementName(i) to desc.getElementDescriptor(i) }
                }
                .reduce { a, b -> a.filter { it in b } }
        } else {
            val desc = baseDesc.getElementDescriptor(0)
            commonFields.map { fieldName ->
                val i = desc.getElementIndex(fieldName)
                    .takeIf { it >= 0 } ?: error("$fieldName is not a field in $name")
                fieldName to desc.getElementDescriptor(i)
            }
        }
        for ((fieldName, desc) in ifFields) {
            val field = GraphQLFieldDefinition.newFieldDefinition().name(fieldName).type(desc.toGraphQLOutputType())
            ifTypeBuilder.field(field)
        }


    }

    fun <U : T> subtype(ser: KSerializer<U>, fields: FieldBuilder<U>.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        subtypeExtras[ser.descriptor] = fields as FieldBuilder<*>.() -> Unit
    }

    fun build(): Pair<GraphQLInterfaceType, Collection<GraphQLObjectType>> {
        val ifType = builder.build()
        val subtypes = buildSubtypes(ifType)
        val typeResolver = TypeResolver { env ->
            val obj: T = env.getObject()
            val actualSer = ser.findPolymorphicSerializerOrNull(NullEncoder, obj)
                ?: error("Could not find actual serializer for $obj")
            subtypes[actualSer.descriptor]
        }
        schemaBuilder.codeRegistry.typeResolver(ifType, typeResolver)
        return Pair(ifType, subtypes.values)
    }

    private fun buildSubtypes(ifType: GraphQLInterfaceType): Map<SerialDescriptor, GraphQLObjectType> {
        val subtypes = mutableMapOf<SerialDescriptor, GraphQLObjectType>()
        for (i in 0 until baseDesc.elementsCount) {
            val elemDescriptor = baseDesc.getElementDescriptor(i)
            FieldBuilder<T>(elemDescriptor, schemaBuilder)
                .apply { builder.withInterface(ifType) }
                .apply { subtypeExtras[elemDescriptor]?.invoke(this) }
                .build()
                .also { subtypes[elemDescriptor] = it }
        }
        return subtypes
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun GraphQLFieldDefinition.Builder.argumentsFromDescriptor(descriptor: SerialDescriptor) {
    for (i in 0 until descriptor.elementsCount) {
        val paramName = descriptor.getElementName(i)
        val paramDesc = descriptor.getElementDescriptor(i)
        val argument = GraphQLArgument.newArgument()
            .name(paramName)
            .type(paramDesc.toGraphQLInputType())
        argument(argument)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun SerialDescriptor.toGraphQLType(): GraphQLType {
    val baseType = when (val kind = kind) {
        is PrimitiveKind -> {
            when (kind) {
                PrimitiveKind.BOOLEAN -> Scalars.GraphQLBoolean
                PrimitiveKind.INT -> Scalars.GraphQLInt
                PrimitiveKind.FLOAT -> Scalars.GraphQLFloat
                PrimitiveKind.DOUBLE -> Scalars.GraphQLFloat
                PrimitiveKind.STRING -> Scalars.GraphQLString
                else -> throw IllegalArgumentException("Unsupported primitive kind $kind in graphql schema")
            }
        }
        is StructureKind.LIST -> {
            val inner = getElementDescriptor(0).toGraphQLType()
            GraphQLList(inner)
        }
        else -> {
            GraphQLTypeReference(serialName.split('.').last().trimEnd('?'))
        }
    }
    return if (isNullable) baseType else GraphQLNonNull(baseType)
}

// TODO figure out where these break
private fun SerialDescriptor.toGraphQLOutputType(): GraphQLOutputType = toGraphQLType() as GraphQLOutputType
private fun SerialDescriptor.toGraphQLInputType(): GraphQLInputType = toGraphQLType() as GraphQLInputType