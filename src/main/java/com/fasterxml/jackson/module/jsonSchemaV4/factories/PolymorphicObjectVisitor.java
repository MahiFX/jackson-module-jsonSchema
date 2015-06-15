package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;

import java.util.HashMap;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectVisitor implements JsonSchemaProducer {

    private PolymorphicObjectSchema schema;

    private VisitorContext visitorContext;

    private JavaType originalType;

    public PolymorphicObjectVisitor(VisitorContext visitorContext, PolymorphicObjectSchema schema, JavaType type) {
        this.schema = schema;
        this.visitorContext = visitorContext;
        this.originalType = type;
    }

    @Override
    public JsonSchema getSchema() {
        return null;
    }


    public void polymorphic(JavaType type) {

        if (VisitorUtils.isPolymorphic(type.getRawClass())) {
            VisitorUtils.PolymorphiSchemaDefinition schemaDefs = new VisitorUtils(visitorContext).extractPolymophicTypes(type.getRawClass());
            schema.setAnyOf(schemaDefs.getReferences());
            if (schema.getDefinitions() == null) {
                schema.setDefinitions(new HashMap<String, JsonSchema>());
            }
            schema.getDefinitions().putAll(schemaDefs.getDefinitions());
        }

    }
}
