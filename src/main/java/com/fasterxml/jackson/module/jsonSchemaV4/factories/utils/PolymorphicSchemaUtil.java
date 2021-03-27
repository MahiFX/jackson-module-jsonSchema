package com.fasterxml.jackson.module.jsonSchemaV4.factories.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
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

import static com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext.uriSafe;

/**
 * Created by zoliszel on 16/06/2015.
 */
public class PolymorphicSchemaUtil {

    private PolymorphiSchemaDefinition schemaDefinition;

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

    private static final Set<String> ALLOWED_NON_NUMERIC_VALUES = new HashSet<>(Arrays.asList("INF", "Infinity", "-INF", "-Infinity", "NaN"));

    public static final String POLYMORPHIC_TYPE_NAME_SUFFIX = "_1";

    public static final String NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE = "___NUMBER___";

    public static final String NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE = "__ALLOWED_NON_NUMERIC_VALUES___";

    public static final String NUMBER_WITH_NON_NUMERIC_VALUES = "___NUMBER_WITH_NON_NUMERIC_VALUES___";

    public static final String DEFINITION_PREFIX = "#/definitions/";

    private static JsonSchema getNumberSchemaWithAllowedString(SchemaGenerationContext context) {
        NumberSchema numberSchema = new NumberSchema();
        numberSchema.setId(context.javaTypeToId(NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE));

        StringSchema allowedStringValues = new StringSchema();
        allowedStringValues.setId(context.javaTypeToId(NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE));
        allowedStringValues.setEnums(ALLOWED_NON_NUMERIC_VALUES);

        Map<String, JsonSchema> typesToWrap = new HashMap<>();
        typesToWrap.put(NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE, numberSchema);
        typesToWrap.put(NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE, allowedStringValues);

        PolymorphicObjectSchema wrappedSchema = constructPolymorphicSchema(typesToWrap, PolymorphicObjectSchema.Type.ONE_OF);
        wrappedSchema.setId(context.javaTypeToId(NUMBER_WITH_NON_NUMERIC_VALUES));
        wrappedSchema.setTypes(new JsonFormatTypes[]{JsonFormatTypes.NUMBER, JsonFormatTypes.STRING});

        ReferenceSchema referenceSchema = new ReferenceSchema(DEFINITION_PREFIX + NUMBER_WITH_NON_NUMERIC_VALUES, wrappedSchema.getType());
        referenceSchema.setDefinitions(new HashMap<>());
        referenceSchema.getDefinitions().put(NUMBER_WITH_NON_NUMERIC_VALUES, wrappedSchema);
        return referenceSchema;
    }

    public static PolymorphicObjectSchema constructPolymorphicSchema(Map<String, JsonSchema> typesToWrap, PolymorphicObjectSchema.Type type) {

        PolymorphicObjectSchema wrappedSchema = new PolymorphicObjectSchema();
        wrappedSchema.setDefinitions(new HashMap<>());
        List<ReferenceSchema> referenceSchemas = new ArrayList<>();
        Set<JsonFormatTypes> types = new HashSet<>();
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
            wrappedSchema.setType(new JsonSchema.ArrayJsonType(types.toArray(new JsonFormatTypes[0])));
        }
        ReferenceSchema[] referenceSchemaArray = referenceSchemas.toArray(new ReferenceSchema[0]);
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

        String getDefinitionRef();

        String getDefinitionKey();
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
        AnnotatedClass classWithoutSuperType = AnnotatedClassResolver.resolveWithoutSuperTypes(config, type, config);
        Collection<NamedType> subTypes;

        if (config.getSubtypeResolver() != null) {
            subTypes = new ArrayList<>(config.getSubtypeResolver().collectAndResolveSubtypesByClass(config, classWithoutSuperType));
        } else {
            return Collections.emptyList();
        }

        List<NamedJavaType> result = new ArrayList<>();
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
                JavaType javaSubType = config.getTypeFactory().constructType(subType.getType());
                result.add(new NamedJavaType(subType.hasName() ? subType.getName() : null, javaSubType));
            }
        }

        //Don't return a list of only the parent which sometimes is returned due to generics
        if(result.size() == 1 && result.get(0).getRawClass().equals(type.getRawClass()) && !type.isContainerType()){ //TODO find out why this breaks arrays
            return Collections.emptyList();
        }

        return result;
    }

    public SerializerProvider getProvider() {
        return provider;
    }


    public PolymorphiSchemaDefinition extractPolyMorphicObjectSchema() throws JsonMappingException {
        if (schemaDefinition == null) {
            if (!isPolymorphic()) {
                throw new IllegalArgumentException("Argument is not a polymorphic object (no JsonSubtype annotation (" + originalType.getRawClass().getSimpleName() + ")");
            }
            JsonSchema[] subSchemas = new JsonSchema[subTypes.size()];
            final Map<String, JsonSchema> definitions = new HashMap<>();

            Iterator<NamedJavaType> it = subTypes.iterator();
            for (int i = 0; i < subTypes.size(); ++i) {
                NamedJavaType namedType = it.next();
                if (!namedType.hasName()) {
                    throw new IllegalArgumentException("No name associated with class " + namedType.getRawClass().getSimpleName() + " try using @JsonTypeName annotation");
                }
                JavaType subJavaType = namedType.getJavaType();
                JsonSchema subSchema = schema(subJavaType);
                String subTypeName = namedType.getName();

                TypeSerializer typeSerializer = getProvider().findTypeSerializer(subJavaType);
                if (typeSerializer != null) {
                    subTypeName = typeSerializer.getTypeIdResolver().idFromBaseType();
                }

                if (namedType.getRawClass() == originalType.getRawClass()) {
                    subTypeName = subTypeName + POLYMORPHIC_TYPE_NAME_SUFFIX;
                }

                subSchemas[i] = subSchema;

                definitions.put(uriSafe(subTypeName), subSchemas[i]);
            }

            final PolymorphicObjectSchema wrapperSchema = constructPolymorphicSchema(definitions, PolymorphicObjectSchema.Type.ANY_OF);
            schemaDefinition = new PolymorphiSchemaDefinition() {

                @Override
                public JsonSchema getPolymorphicObjectSchema() {
                    return wrapperSchema;
                }

                @Override
                public String getDefinitionRef() {
                    return DEFINITION_PREFIX + uriSafe(originalTypeName);
                }

                @Override
                public String getDefinitionKey() {
                    return uriSafe(originalTypeName);
                }
            };
        }
        return schemaDefinition;
    }

    public boolean isPolymorphic() {
        return subTypes.size() > 0;
    }

    public static void propagateDefinitionsUp(JsonSchema node) {
        Map<String, JsonSchema> allDefinitions = extractDefinitions(node, new HashMap<>());
        if (!allDefinitions.isEmpty()) {
            node.setDefinitions(allDefinitions);
        }
    }

    private static final Map<Type, SchemaGenerationContext> typeContexts = new HashMap<>();

    public static JsonSchema schema(Type t) throws JsonMappingException {
        SchemaGenerationContext context = SchemaGenerationContext.get();
        boolean createdContext = false;
        if (!typeContexts.containsKey(t)) {
            // We only want to create a new context once per type when recursing
            SchemaGenerationContext newContext = context.copy();
            typeContexts.put(t, newContext);
            createdContext = true;
            SchemaGenerationContext.set(newContext);
        }
        try {
            ObjectMapper mapper = context.getMapper().copy();
            SchemaFactoryWrapper visitor = context.getNewSchemaFactoryWrapper();
            mapper.acceptJsonFormatVisitor(mapper.constructType(t), visitor);
            return visitor.finalSchema();
        } finally {
            SchemaGenerationContext.set(context);
            if (createdContext) {
                typeContexts.remove(t);
            }
        }
    }

    public static JsonSchema wrapNonNumericTypes(JsonSchema originalSchema, SchemaGenerationContext context) {
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

        return getNumberSchemaWithAllowedString(context);

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
            JsonSchema sourceSchema = entry.getValue();
            String key = entry.getKey();
            if (!target.containsKey(key)) {
                target.put(key, sourceSchema);
            } else {
                boolean isPolyMorph = sourceSchema instanceof PolymorphicObjectSchema;
                if (!(sourceSchema instanceof ReferenceSchema) && !(sourceSchema instanceof AnyOfSchema) && !isPolyMorph) {
                    target.put(key, sourceSchema);
                }
            }
        }
    }

}
