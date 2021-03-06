package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;

import java.util.HashMap;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectVisitor implements JsonSchemaProducer {

    private final PolymorphicObjectSchema schema;

    private final SerializerProvider provider;

    public PolymorphicObjectVisitor(PolymorphicObjectSchema schema, SerializerProvider provider) {
        this.schema = schema;
        this.provider = provider;
    }

    @Override
    public JsonSchema getSchema() {
        return null;
    }

    public void visitPolymorphicObject(JavaType type) throws JsonMappingException {

        SchemaGenerationContext context = SchemaGenerationContext.get();

        if (context.isVisitedAsPolymorphicType(type)) {
            throw new IllegalStateException("JavaType: " + type.getRawClass().getSimpleName() + " has already been visited. A single class can be handled polymorphicly only once");

        }
        context.setVisitedAsPolymorphic(type);

        PolymorphicSchemaUtil handlingUtil = new PolymorphicSchemaUtil(type, this.provider);
        if (handlingUtil.isPolymorphic()) {
            PolymorphicSchemaUtil.PolymorphiSchemaDefinition def = handlingUtil.extractPolyMorphicObjectSchema();
            if (schema.getDefinitions() == null) {
                schema.setDefinitions(new HashMap<>());
            }

            schema.getDefinitions().put(def.getDefinitionKey(), def.getPolymorphicObjectSchema());
            schema.set$ref(def.getDefinitionRef());
            schema.setType(def.getPolymorphicObjectSchema().getType());
            context.setFormatTypeForVisitedType(type, def.getPolymorphicObjectSchema().getType());
            context.setSchemaRefForPolymorphicType(type, def.getDefinitionRef());
        }


    }
}
