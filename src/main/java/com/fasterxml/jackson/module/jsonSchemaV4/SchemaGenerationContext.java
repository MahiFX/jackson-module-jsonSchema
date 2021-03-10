package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.FormatVisitorFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil.DEFINITION_PREFIX;

/**
 * Created by zoliszel on 24/06/2015.
 */
public class SchemaGenerationContext {

    private final SerializerProvider provider;

    public static boolean isNotJvmType(JavaType propType) {
        Package pack = propType.getRawClass() != null ? propType.getRawClass().getPackage() : null;
        return !propType.isArrayType() && !propType.isPrimitive() && (pack != null && !pack.getName().startsWith("java.lang"));
    }

    public JsonSchema createDefinitionForNonPolymorphic(JavaType contentType, JsonSchema schema) throws JsonMappingException {

        String definitionKey = getDefinitionKeyForType(contentType);
        if (schema.getDefinitions() == null || !schema.getDefinitions().containsKey(definitionKey)) {
            if (schema.getDefinitions() == null) {
                schema.setDefinitions(new HashMap<>());
            }
            JsonSchema subSchema = getSchemaForNonPolymorphicType(contentType);
            if (subSchema == null) {
                subSchema = PolymorphicSchemaUtil.schema(contentType.getRawClass());
                if (!subSchema.isReferenceSchema()) {
                    setSchemaForNonPolymorphicType(contentType, subSchema);
                }
            }
            if (!subSchema.isReferenceSchema() && !schema.getDefinitions().containsKey(definitionKey)) {
                schema.getDefinitions().put(definitionKey, subSchema);
            }

            //TODO convert original usage to defn ref
        }

        return schema.getDefinitions().get(definitionKey);
    }

    public boolean isVisited(JavaType contentType) {
        return seenSchemas.containsKey(contentType);
    }

    public SerializerProvider getProvider() {
        return provider;
    }

    public JsonSchema getSchemaForNonPolymorphicType(JavaType contentType) {
        TypeVisitationStatus typeVisitationStatus = seenSchemas.get(contentType);
        return typeVisitationStatus != null ? typeVisitationStatus.getNonPolymorphicSchema() : null;
    }

    public boolean isCopy() {
        return false;
    }

    private static final ThreadLocal<SchemaGenerationContext> VISITOR_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static SchemaGenerationContext get() {
        return VISITOR_CONTEXT_THREAD_LOCAL.get();
    }

    public static void set(SchemaGenerationContext schemaGenerationContext) {
        VISITOR_CONTEXT_THREAD_LOCAL.set(schemaGenerationContext);
    }

    private final Map<JavaType, TypeVisitationStatus> seenSchemas = new HashMap<>();


    private final FormatVisitorFactory visitorFactory;

    private final JsonSchemaFactory schemaProvider;

    private final WrapperFactory wrapperFactory;

    private final ObjectMapper mapper;

    private final boolean withAdditionalProperties;

    public FormatVisitorFactory getVisitorFactory() {
        return visitorFactory;
    }

    public JsonSchemaFactory getSchemaProvider() {
        return schemaProvider;
    }

    public WrapperFactory getWrapperFactory() {
        return wrapperFactory;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public SchemaFactoryWrapper getNewSchemaFactoryWrapper() {
        return getWrapperFactory().getWrapper(provider);
    }

    public SchemaGenerationContext(SerializerProvider provider, FormatVisitorFactory visitorFactory, JsonSchemaFactory schemaProvider, WrapperFactory wrapperFactory, ObjectMapper mapper, boolean withAdditionalProperties) {
        this.provider = provider;
        this.visitorFactory = visitorFactory;
        this.schemaProvider = schemaProvider;
        this.wrapperFactory = wrapperFactory;
        this.mapper = mapper;
        this.withAdditionalProperties = withAdditionalProperties;
    }

    public boolean isWithAdditionalProperties() {
        return withAdditionalProperties;
    }

    public boolean isVisitedAsPolymorphicType(JavaType type) {
        return seenSchemas.containsKey(type) && seenSchemas.get(type).isVisitedAsPolymorphic();
    }

    public boolean isVisited(JavaType type, boolean forObjectSchema) {
        if (seenSchemas.containsKey(type)) {
            if (forObjectSchema) {
                return seenSchemas.get(type).isVisitedAsNonPolymorphic();
            } else {
                return seenSchemas.get(type).isVisitedAsPolymorphic();
            }
        }
        return false;
    }

    private final Map<JavaType, AtomicInteger> referenceCounter = new HashMap<>();

    public ReferenceSchema getReferenceSchemaForVisitedType(JavaType type) {
        TypeVisitationStatus visitationStatus = seenSchemas.get(type);
        if (visitationStatus.isPolymorphic) {
            return visitationStatus.getPolymorphicReference();
        } else {
            //Return a ref to the schema using the ID for the type
            return getReferenceSchemaForType(type);
        }
    }

    public ReferenceSchema getReferenceSchemaForType(JavaType type) {
        referenceCounter.computeIfAbsent(type, (t) -> new AtomicInteger()).incrementAndGet();
        JsonSchema actualSchema = getSchemaForNonPolymorphicType(type);
        if (actualSchema != null) {
            actualSchema.setId(getIdForType(type));
        }
        return new ReferenceSchema(getDefinitionRefForType(type), new ObjectSchema().getType());
    }


    public int getReferenceCount(JavaType type) {
        return referenceCounter.computeIfAbsent(type, (t) -> new AtomicInteger()).get();
    }

    public void setVisitedAsNonPolymorphic(JavaType javaType) {
        if (seenSchemas.get(javaType) == null) {
            TypeVisitationStatus typeVisitationStatus = new TypeVisitationStatus();
            seenSchemas.put(javaType, typeVisitationStatus);
        }
        TypeVisitationStatus typeVisitationStatus = seenSchemas.get(javaType);
        typeVisitationStatus.setVisitedAsNonPolymorphic(true);
    }

    public void setVisitedAsPolymorphic(JavaType type) {
        if (isVisitedAsPolymorphicType(type)) {
            throw new IllegalStateException("Type " + type.getRawClass().getSimpleName() + " was visited more than once as polymorphic");
        }
        TypeVisitationStatus typeVisitationStatus = new TypeVisitationStatus();
        typeVisitationStatus.setIsPolymorphic(true);
        typeVisitationStatus.setVisitedAsPolymorphic(true);
        seenSchemas.put(type, typeVisitationStatus);
    }

    public void setFormatTypeForVisitedType(JavaType type, JsonSchema.JSONType jsonType) {
        seenSchemas.get(type).setJsonTypeFormat(jsonType);
    }

    public void setSchemaRefForPolymorphicType(JavaType type, String id) {
        seenSchemas.get(type).setPolymorphicReference(id);
    }

    public void setSchemaForNonPolymorphicType(JavaType type, JsonSchema schema) {
        if (schema instanceof ReferenceSchema) {
            throw new IllegalArgumentException("Don't store a reference schema, we need the proper one");
        }
        TypeVisitationStatus typeVisitationStatus = seenSchemas.get(type);
        if (typeVisitationStatus == null) {
            setVisitedAsNonPolymorphic(type);
        }
        seenSchemas.get(type).setNonPolymorphicSchema(schema);
    }

    public String javaTypeToId(String typeName) {
        return "#" + uriSafe(typeName);
    }

    public static String uriSafe(String typeName) {
        return typeName.replace('.', ':').replace('$', ':').replace("<", ":").replace(">", ":");
    }

    public String getIdForType(JavaType convertedType) {
        return javaTypeToId(toCanonicalName(convertedType, true));
    }

    public String getDefinitionRefForType(JavaType convertedType) {
        return DEFINITION_PREFIX + uriSafe(toCanonicalName(convertedType, false));
    }


    public String getDefinitionKeyForType(JavaType convertedType) {
        return uriSafe(toCanonicalName(convertedType, false));
    }

    private String toCanonicalName(JavaType convertedType, boolean withPackage) {
        String typeName = getTypeName(convertedType);

        if (typeName != null) {
            if (withPackage) {
                Package classPackage = convertedType.getRawClass().getPackage();
                if (classPackage != null) {
                    String packageName = classPackage.getName();
                    return packageName + "." + typeName;
                }
            }
            return typeName;
        } else {
            return convertedType.toCanonical();
        }

    }

    private String getTypeName(JavaType convertedType) {
        try {
            TypeSerializer serializer = getProvider().findTypeSerializer(convertedType);
            String typeName = null;
            if (serializer != null) {
                TypeIdResolver typeIdResolver = serializer.getTypeIdResolver();
                typeName = typeIdResolver.idFromBaseType();
            }
            return typeName;
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }

    public SchemaGenerationContext copy() {
        return new SchemaGenerationContextCopy(provider, visitorFactory, schemaProvider, wrapperFactory, mapper.copy(), withAdditionalProperties);
    }

    public static class SchemaGenerationContextCopy extends SchemaGenerationContext {

        public SchemaGenerationContextCopy(SerializerProvider provider, FormatVisitorFactory visitorFactory, JsonSchemaFactory schemaProvider, WrapperFactory wrapperFactory, ObjectMapper mapper, boolean withAdditionalProperties) {
            super(provider, visitorFactory, schemaProvider, wrapperFactory, mapper, withAdditionalProperties);
        }

        public boolean isCopy() {
            return true;
        }
    }

    public static class TypeVisitationStatus {
        private boolean isPolymorphic;

        private boolean visitedAsPolymorphic;

        private boolean visitedAsNonPolymorphic;

        private final ReferenceSchema polyMorphicreference = new ReferenceSchema();

        private JsonSchema nonPolyMorphicSchema;

        public void setIsPolymorphic(boolean isPolymorphic) {
            this.isPolymorphic = isPolymorphic;
        }

        public boolean isVisitedAsPolymorphic() {
            return visitedAsPolymorphic;
        }

        public void setVisitedAsPolymorphic(boolean visitedAsPolymorphic) {
            this.visitedAsPolymorphic = visitedAsPolymorphic;
        }

        public boolean isVisitedAsNonPolymorphic() {
            return visitedAsNonPolymorphic;
        }

        public void setVisitedAsNonPolymorphic(boolean visitedAsNonPolymorphic) {
            this.visitedAsNonPolymorphic = visitedAsNonPolymorphic;
        }

        public ReferenceSchema getPolymorphicReference() {
            return polyMorphicreference;
        }

        public void setPolymorphicReference(String polymorphicReference) {
            polyMorphicreference.set$ref(polymorphicReference);
        }

        public void setNonPolymorphicSchema(JsonSchema nonPolyMorphicSchema) {
            this.nonPolyMorphicSchema = nonPolyMorphicSchema;
        }

        public void setJsonTypeFormat(JsonSchema.JSONType jsonTypeFormat) {
            polyMorphicreference.setType(jsonTypeFormat);
        }

        public JsonSchema getNonPolymorphicSchema() {
            return nonPolyMorphicSchema;
        }
    }

}
