package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;

import java.util.HashSet;

public class VisitorContext {
    private final HashSet<JavaType> seenSchemas = new HashSet<JavaType>();

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

    public String addSeenSchemaUri(JavaType aSeenSchema) {
        if (aSeenSchema != null && !aSeenSchema.isPrimitive()) {
            seenSchemas.add(aSeenSchema);
            return javaTypeToUrn(aSeenSchema);
        }
        return null;
    }

    public String getSeenSchemaUri(JavaType aSeenSchema) {
        return (seenSchemas.contains(aSeenSchema)) ? javaTypeToUrn(aSeenSchema) : null;
    }

    public String javaTypeToUrn(JavaType jt) {
        return "urn:jsonschema:" + jt.toCanonical().replace('.', ':').replace('$', ':').replace("<", ":").replace(">", ":");
    }
}
