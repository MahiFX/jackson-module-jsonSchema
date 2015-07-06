package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;

import java.util.HashMap;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectVisitor implements JsonSchemaProducer {

    private PolymorphicObjectSchema schema;

    private JavaType originalType;

    private SerializerProvider provider;

    public PolymorphicObjectVisitor(PolymorphicObjectSchema schema, JavaType type,SerializerProvider provider) {
        this.schema = schema;
        this.originalType = type;
        this.provider=provider;
    }

    @Override
    public JsonSchema getSchema() {
        return null;
    }

    public void visitPolymorphicObject(JavaType type) {

        SchemaGenerationContext context = SchemaGenerationContext.get();

        if(context.isVisitedAsPolymorphicType(type)){
            throw new IllegalStateException("JavaType: " + type.getRawClass().getSimpleName() + "has already been visited. A single class can be handled polymorphicly only once");

        }
        context.setVisitedAsPolymorphic(type);

        PolymorphicHandlingUtil handlingUtil = new PolymorphicHandlingUtil(type,this.provider);
        if (handlingUtil.isPolymorphic()) {
            PolymorphicHandlingUtil.PolymorphiSchemaDefinition def =handlingUtil.extractPolyMorphicObjectSchema();
            if (schema.getDefinitions() == null) {
                schema.setDefinitions(new HashMap<String, JsonSchema>());
            }
            schema.getDefinitions().put(def.getOrigianlTypeName(), def.getPolymorphicObjectSchema());
            schema.set$ref(def.getDefinitionsReference());
            schema.setType(def.getPolymorphicObjectSchema().getType());
            context.setFormatTypeForVisitedType(type,def.getPolymorphicObjectSchema().getType());
            context.setSchemaRefForPolymorphicType(type, def.getDefinitionsReference());
            //schema.setId(id);

        }


    }
}
