package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.FormatVisitorFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zoliszel on 24/06/2015.
 */
public class SchemaGenerationContext {

    private static final ThreadLocal<SchemaGenerationContext> VISITOR_CONTEXT_THREAD_LOCAL = new ThreadLocal<SchemaGenerationContext>();

    public static SchemaGenerationContext get(){
        return VISITOR_CONTEXT_THREAD_LOCAL.get();
    }

    static void set(SchemaGenerationContext schemaGenerationContext){
        VISITOR_CONTEXT_THREAD_LOCAL.set(schemaGenerationContext);
    }

    private final Map<JavaType,JsonSchema.JSONType> seenSchemas = new HashMap<JavaType, JsonSchema.JSONType>();

    private final Set<JavaType> seenPolymorphicTypes = new HashSet<JavaType>();

    private final Set<JavaType> visitedTypes = new HashSet<JavaType>();

    private final FormatVisitorFactory visitorFactory;

    private final JsonSchemaFactory schemaProvider;

    private final WrapperFactory wrapperFactory;

    private final ObjectMapper mapper;

    public FormatVisitorFactory getVisitorFactory() {
        return visitorFactory;
    }

    public JsonSchemaFactory getSchemaProvider() {
        return schemaProvider;
    }

 /*   public SerializerProvider getProvider() {
        return mapper.getSerializerProvider();
    }
*/
    public WrapperFactory getWrapperFactory() {
        return wrapperFactory;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public SchemaFactoryWrapper getNewSchemaFactoryWrapper(SerializerProvider provider){
        return getWrapperFactory().getWrapper(provider);
    }

    public SchemaGenerationContext(FormatVisitorFactory visitorFactory, JsonSchemaFactory schemaProvider, WrapperFactory wrapperFactory, ObjectMapper mapper) {
        this.visitorFactory = visitorFactory;
        this.schemaProvider = schemaProvider;
        this.wrapperFactory = wrapperFactory;
        this.mapper = mapper;
    }

    public boolean visitedPolymorphicType(JavaType type){
        return seenPolymorphicTypes.contains(type);
    }

    public String addSeenSchemaUriForPolymorphic(JavaType type){
        seenPolymorphicTypes.add(type);
        return javaTypeToUrn(type) + ":polymorphic";
    }

    public boolean addVisitedPolymorphicType(JavaType t){
        return visitedTypes.add(t);
    }
    public String getSeenSchemaUriPolymorphic(JavaType aSeenSchema) {
        if(seenPolymorphicTypes.contains(aSeenSchema)){
            return javaTypeToUrn(aSeenSchema) + ":polymorphic";
        }
        return null;
    }

    public String addSeenSchemaUri(JavaType aSeenSchema,JsonSchema.JSONType jsonType) {
        if (aSeenSchema != null && !aSeenSchema.isPrimitive()) {
            seenSchemas.put(aSeenSchema, jsonType);
            return javaTypeToUrn(aSeenSchema);
        }
        return null;
    }

    public JsonSchema.JSONType getJsonTypeForVisitedSchema(JavaType aSeenSchema){
        return seenSchemas.get(aSeenSchema);
    }
    public String getSeenSchemaUri(JavaType aSeenSchema) {
        return (seenSchemas.containsKey(aSeenSchema)) ? javaTypeToUrn(aSeenSchema) : null;
    }

    public String javaTypeToUrn(JavaType jt) {
        return "urn:jsonschema:" + jt.toCanonical().replace('.', ':').replace('$', ':').replace("<", ":").replace(">", ":");
    }

}
