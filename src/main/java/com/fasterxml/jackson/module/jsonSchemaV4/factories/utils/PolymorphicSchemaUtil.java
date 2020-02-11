package com.fasterxml.jackson.module.jsonSchemaV4.factories.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.types.*;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by zoliszel on 16/06/2015.
 */
public class PolymorphicSchemaUtil {

    public static class NamedJavaType {
        private final String name;

        private final JavaType javaType;

        public NamedJavaType(String name, JavaType javaType) {
            this.name = name;
            this.javaType = javaType;
        }

        public JavaType getJavaType() {
            return javaType;
        }

        public String getName() {
            return name;
        }

        public boolean hasName() {
            return name != null;
        }

        public Class<?> getRawClass() {
            return javaType.getRawClass();
        }
    }

    private static final Set<String> ALLOWED_NON_NUMERIC_VALUES = new HashSet<String>(Arrays.asList("INF", "Infinity", "-INF", "-Infinity", "NaN"));

    public static final String POLYMORPHIC_TYPE_NAME_SUFFIX = "_1";

    public static final String NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE = "___NUMBER___";

    public static final String NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE = "__ALLOWED_NON_NUMERIC_VALUES___";

    public static final String NUMBER_WITH_NON_NUMERIC_VALUES = "___NUMBER_WITH_NON_NUMERIC_VALUES___";

    private static final String DEFINITION_PREFIX = "#/definitions/";

    private static JsonSchema getNumberSchemaWithAllowedString() {
        NumberSchema numberSchema = new NumberSchema();
        numberSchema.setId(SchemaGenerationContext.javaTypeToUrn(NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE));

        StringSchema allowedStringValues = new StringSchema();
        allowedStringValues.setId(SchemaGenerationContext.javaTypeToUrn(NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE));
        allowedStringValues.setEnums(ALLOWED_NON_NUMERIC_VALUES);

        Map<String, JsonSchema> typesToWrap = new HashMap<String, JsonSchema>();
        typesToWrap.put(NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE, numberSchema);
        typesToWrap.put(NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE, allowedStringValues);
        PolymorphicObjectSchema wrappedSchema = constructPolymorphicSchema(typesToWrap, PolymorphicObjectSchema.Type.ONE_OF);
        wrappedSchema.setId(SchemaGenerationContext.javaTypeToUrn(NUMBER_WITH_NON_NUMERIC_VALUES));

        ReferenceSchema referenceSchema = new ReferenceSchema(DEFINITION_PREFIX + NUMBER_WITH_NON_NUMERIC_VALUES, wrappedSchema.getType());
        referenceSchema.setDefinitions(new HashMap<String, JsonSchema>());
        referenceSchema.getDefinitions().put(NUMBER_WITH_NON_NUMERIC_VALUES, wrappedSchema);
        return referenceSchema;
    }

    public static PolymorphicObjectSchema constructPolymorphicSchema(Map<String, JsonSchema> typesToWrap, PolymorphicObjectSchema.Type type) {

        PolymorphicObjectSchema wrappedSchema = new PolymorphicObjectSchema();
        wrappedSchema.setDefinitions(new HashMap<String, JsonSchema>());
        List<ReferenceSchema> referenceSchemas = new ArrayList<ReferenceSchema>();
        Set<JsonFormatTypes> types = new HashSet<JsonFormatTypes>();
        for (Map.Entry<String, ? extends JsonSchema> entry : typesToWrap.entrySet()) {
            final JsonSchema schema = entry.getValue();
            if (schema == null) {
                throw new IllegalStateException("No schema for type [" + entry.getKey() + "]! All given types: " + typesToWrap);
            }
            wrappedSchema.getDefinitions().put(entry.getKey(), schema);
            if (schema.isReferenceSchema()) {
                referenceSchemas.add(schema.asReferenceSchema());
            } else {
                referenceSchemas.add(new ReferenceSchema(DEFINITION_PREFIX + entry.getKey(), schema.getType()));
            }
            if (schema.getType().isSingleJSONType()) {
                types.add(JsonFormatTypes.forValue(schema.getType().asSingleJsonType().getFormatType()));
            } else if (schema.getType().isArrayJSONType()) {
                types.addAll(Arrays.asList(schema.getType().asArrayJsonType().getFormatTypes()));
            }
        }

        if (types.size() == 1) {
            wrappedSchema.setType(new JsonSchema.SingleJsonType(types.iterator().next()));
        } else {
            wrappedSchema.setType(new JsonSchema.ArrayJsonType(types.toArray(new JsonFormatTypes[types.size()])));
        }
        ReferenceSchema[] referenceSchemaArray = referenceSchemas.toArray(new ReferenceSchema[referenceSchemas.size()]);
        switch (type) {
            case ANY_OF:
                wrappedSchema.setAnyOf(referenceSchemaArray);
                break;
            case ALL_OF:
                wrappedSchema.setAllOf(referenceSchemaArray);
                break;
            case ONE_OF:
                wrappedSchema.setOneOf(referenceSchemaArray);
                break;
            case NOT:
            default:
                throw new IllegalArgumentException("Unknonw polymorphic object type " + type);
        }
        return wrappedSchema;
    }

    private final SerializerProvider provider;

    public interface PolymorphiSchemaDefinition {
        JsonSchema getPolymorphicObjectSchema();

        String getOrigianlTypeName();

        String getDefinitionsReference();
    }


    protected final JavaType originalType;

    protected final String originalTypeName;

    private Collection<NamedJavaType> subTypes = Collections.emptyList();

    public PolymorphicSchemaUtil(JavaType originalType, SerializerProvider provider) {
        this.originalType = originalType;
        this.provider = provider;
        if (provider != null && provider.getConfig() != null) {
            Collection<NamedJavaType> namedTypes = extractSubTypes(originalType, getProvider().getConfig(), true);

            if (!namedTypes.isEmpty()) {
                subTypes = namedTypes;
            }
        }
        originalTypeName = getOriginalTypeName();

    }

    private String getOriginalTypeName() {
        for (NamedJavaType namedJavaType : subTypes) {
            if (namedJavaType.getRawClass() == originalType.getRawClass()) {
                if (namedJavaType.hasName()) {
                    return namedJavaType.getName();
                }
                break;
            }
        }
        return originalType.getRawClass().getSimpleName();
    }

    public static Collection<NamedJavaType> extractSubTypes(JavaType type, SerializationConfig config, boolean removeNonConcrete) {
        AnnotatedClass classWithoutSuperType = AnnotatedClass.constructWithoutSuperTypes(type.getRawClass(), config.getAnnotationIntrospector(), config);
        Collection<NamedType> subTypes = null;
        if (config.getSubtypeResolver() != null) {

            subTypes = new ArrayList<NamedType>(config.getSubtypeResolver().collectAndResolveSubtypes(classWithoutSuperType, config, config.getAnnotationIntrospector()));
        }

        List<NamedJavaType> result = new ArrayList<NamedJavaType>();
        for (NamedType subType : subTypes) {
            //remove abstract classes/intefaces when requested, should have full type information in subclasses
            boolean addType = true;
            if (removeNonConcrete && Modifier.isAbstract(subType.getType().getModifiers()) && !subType.getType().isArray()) {
                addType = false;
            }
            if (removeNonConcrete && Modifier.isInterface(subType.getType().getModifiers())) {
                addType = false;
            }
            if (addType) {
                result.add(new NamedJavaType(subType.hasName() ? subType.getName() : null, type.narrowBy(subType.getType())));
            }
        }

        return result;
    }

    public SerializerProvider getProvider() {
        return provider;
    }


    public PolymorphiSchemaDefinition extractPolyMorphicObjectSchema() throws JsonMappingException {
        if (!isPolymorphic()) {
            throw new IllegalArgumentException("Argument is not a polymorphic object (no JsonSubtype annotation (" + originalType.getRawClass().getSimpleName() + ")");
        }
        JsonSchema[] subSchemas = new JsonSchema[subTypes.size()];
        final Map<String, JsonSchema> definitions = new HashMap<String, JsonSchema>();

        Iterator<NamedJavaType> it = subTypes.iterator();
        for (int i = 0; i < subTypes.size(); ++i) {
            NamedJavaType namedType = it.next();
            if (!namedType.hasName()) {
                throw new IllegalArgumentException("No name associated with class " + namedType.getRawClass().getSimpleName() + " try using @JsonTypeName annotation");
            }
            JavaType subJavaType = namedType.getJavaType();
            JsonSchema subSchema = schema(subJavaType);
            String subTypeName = namedType.getName();

            if (namedType.getRawClass() == originalType.getRawClass()) {
                subTypeName = subTypeName + POLYMORPHIC_TYPE_NAME_SUFFIX;
            }

            TypeSerializer typeSerializer = getProvider().findTypeSerializer(subJavaType);
            if (typeSerializer != null) {
                subTypeName = typeSerializer.getTypeIdResolver().idFromBaseType();
            }

            subSchemas[i] = subSchema;

            definitions.put(subTypeName, subSchemas[i]);
        }

        final PolymorphicObjectSchema wrapperSchema = constructPolymorphicSchema(definitions, PolymorphicObjectSchema.Type.ANY_OF);
        return new PolymorphiSchemaDefinition() {

            @Override
            public JsonSchema getPolymorphicObjectSchema() {
                return wrapperSchema;
            }

            @Override
            public String getOrigianlTypeName() {
                return originalTypeName;
            }

            @Override
            public String getDefinitionsReference() {
                return DEFINITION_PREFIX + originalTypeName;
            }
        };
    }

    public boolean isPolymorphic() {
        return subTypes.size() > 1;
    }

    public static JsonSchema propagateDefinitionsUp(JsonSchema node) {
        Map<String, JsonSchema> allDefinitions = extractDefinitions(node, new HashMap<String, JsonSchema>());
        if (!allDefinitions.isEmpty()) {
            node.setDefinitions(allDefinitions);
        }
        return node;

    }

    protected JsonSchema schema(Type t) throws JsonMappingException {
        SchemaGenerationContext context = SchemaGenerationContext.get();
        ObjectMapper mapper = context.getMapper().copy();
        SchemaFactoryWrapper visitor = context.getNewSchemaFactoryWrapper(null);
        mapper.acceptJsonFormatVisitor(mapper.constructType(t), visitor);
        return visitor.finalSchema();
    }

    public static JsonSchema wrapNonNumericTypes(JsonSchema originalSchema) {
        if (!originalSchema.isNumberSchema()) {
            return originalSchema;
        }

        if (originalSchema instanceof IntegerSchema) {
            return originalSchema;
        }
        if (!SchemaGenerationContext.get().getMapper().isEnabled(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
            return originalSchema;
        }

        NumberSchema numberSchema = (NumberSchema) originalSchema;
        if (numberSchema.getEnums() != null && !numberSchema.getEnums().isEmpty()) {
            return numberSchema;
        }

        return getNumberSchemaWithAllowedString();

    }

    private static Map<String, JsonSchema> extractDefinitions(JsonSchema node, Map<String, JsonSchema> definitionsSoFar) {
        if (node.isObjectSchema()) {
            if (node.asObjectSchema().getProperties() != null) {
                for (Map.Entry<String, JsonSchema> entry : node.asObjectSchema().getProperties().entrySet()) {
                    mergeDefinitions(definitionsSoFar, extractDefinitions(entry.getValue(), definitionsSoFar));
                }
            }
            if (node.asObjectSchema().getAdditionalProperties() != null) {
                if (node.asObjectSchema().getAdditionalProperties().isSchemaAdditionalProperties()) {
                    JsonSchema additionalProps = node.asObjectSchema().getAdditionalProperties().asSchemaAdditionalProperties().getJsonSchema();
                    mergeDefinitions(definitionsSoFar, extractDefinitions(additionalProps, definitionsSoFar));
                }
            }
        } else if (node.isArraySchema()) {
            if (node.asArraySchema().getItems() != null) {
                if (node.asArraySchema().getItems().isSingleItems()) {
                    mergeDefinitions(definitionsSoFar, extractDefinitions(node.asArraySchema().getItems().asSingleItems().getSchema(), definitionsSoFar));
                } else if (node.asArraySchema().getItems().isArrayItems()) {
                    for (JsonSchema schema : node.asArraySchema().getItems().asArrayItems().getJsonSchemas()) {
                        mergeDefinitions(definitionsSoFar, extractDefinitions(schema, definitionsSoFar));
                    }
                }
            }
        }
        if (node.getDefinitions() != null) {
            for (Map.Entry<String, JsonSchema> entry : node.getDefinitions().entrySet()) {
                mergeDefinitions(definitionsSoFar, extractDefinitions(entry.getValue(), definitionsSoFar));
            }
            mergeDefinitions(definitionsSoFar, node.getDefinitions());
            node.setDefinitions(null);
        }
        return definitionsSoFar;
    }

    private static void mergeDefinitions(Map<String, JsonSchema> target, Map<String, JsonSchema> source) {
        if (source == null) {
            return;
        }
        for (Map.Entry<String, JsonSchema> entry : source.entrySet()) {
            if (!(entry.getValue() instanceof ReferenceSchema) && !(entry.getValue() instanceof AnyOfSchema)) {
                target.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
