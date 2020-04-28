package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.FormatVisitorFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zoliszel on 24/06/2015.
 */
public class SchemaGenerationContext {

    public static class TypeVisitationStatus {
        private boolean isPolymorphic;

        private boolean visitedAsPolymorphic;

        private boolean visitedAsNonPolymorphic;

        private final ReferenceSchema polyMorphicreference = new ReferenceSchema();

        private final ReferenceSchema nonPolyMorphicReference = new ReferenceSchema();


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

        public ReferenceSchema getNonPolymorphicReference() {
            return nonPolyMorphicReference;
        }

        public void setNonPolymorphicReference(String nonPolymorphicReference) {
            nonPolyMorphicReference.set$ref(nonPolymorphicReference);
        }

        public void setJsonTypeFormat(JsonSchema.JSONType jsonTypeFormat) {
            polyMorphicreference.setType(jsonTypeFormat);
            nonPolyMorphicReference.setType(jsonTypeFormat);
        }
    }

    private static final ThreadLocal<SchemaGenerationContext> VISITOR_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static SchemaGenerationContext get() {
        return VISITOR_CONTEXT_THREAD_LOCAL.get();
    }

    static void set(SchemaGenerationContext schemaGenerationContext) {
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

    public SchemaFactoryWrapper getNewSchemaFactoryWrapper(SerializerProvider provider) {
        return getWrapperFactory().getWrapper(provider);
    }

    public SchemaGenerationContext(FormatVisitorFactory visitorFactory, JsonSchemaFactory schemaProvider, WrapperFactory wrapperFactory, ObjectMapper mapper, boolean withAdditionalProperties) {
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

    public ReferenceSchema getReferenceSchemaForVisitedType(JavaType type) {
        TypeVisitationStatus visitationStatus = seenSchemas.get(type);
        if (visitationStatus.isPolymorphic) {
            return visitationStatus.getPolymorphicReference();
        } else {
            return visitationStatus.getNonPolymorphicReference();
        }
    }

    public void setVisitedAsNonPolymorphic(JavaType javaType) {
        if (seenSchemas.get(javaType) == null) {
            TypeVisitationStatus typeVisitationStatus = new TypeVisitationStatus();
            seenSchemas.put(javaType, typeVisitationStatus);
        }
        seenSchemas.get(javaType).setVisitedAsNonPolymorphic(true);
    }

    public void setVisitedAsPolymorphic(JavaType type) {
        if (seenSchemas.get(type) != null) {
            throw new IllegalStateException("Type " + type.getRawClass().getSimpleName() + "was visited more than once as polymorphic");
        }
        TypeVisitationStatus typeVisitationStatus = new TypeVisitationStatus();
        typeVisitationStatus.setIsPolymorphic(true);
        typeVisitationStatus.setVisitedAsPolymorphic(true);
        seenSchemas.put(type, typeVisitationStatus);
    }

    public void setFormatTypeForVisitedType(JavaType type, JsonSchema.JSONType jsonType) {
        seenSchemas.get(type).setJsonTypeFormat(jsonType);
    }

    public String setSchemaRefForPolymorphicType(JavaType type, String id) {
        seenSchemas.get(type).setPolymorphicReference(id);
        return id;
    }

    public String setSchemaRefForNonPolymorphicType(JavaType aSeenSchema, String id) {
        seenSchemas.get(aSeenSchema).setNonPolymorphicReference(id);
        return id;
    }

    public static String javaTypeToUrn(String typeName) {
        return "urn:jsonschema:" + typeName.replace('.', ':').replace('$', ':').replace("<", ":").replace(">", ":");
    }

}
