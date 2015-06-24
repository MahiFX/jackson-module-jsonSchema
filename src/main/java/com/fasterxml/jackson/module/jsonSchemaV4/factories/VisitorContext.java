package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

import java.util.HashMap;
import java.util.HashSet;

public class VisitorContext {
    private final HashMap<JavaType,JsonSchema.JSONType> seenSchemas = new HashMap<JavaType, JsonSchema.JSONType>();

    private final HashSet<JavaType> seenPolymorphicTypes = new HashSet<JavaType>();


    public boolean visitedPolymorphicType(JavaType type){
        return seenPolymorphicTypes.contains(type);
    }


    public String addSeenSchemaUriForPolymorphic(JavaType type){
        seenPolymorphicTypes.add(type);
        return javaTypeToUrn(type) + ":polymorphic";
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
