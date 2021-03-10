package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.TypeDecorationUtils;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

import java.util.HashMap;


public class ArrayVisitor extends JsonArrayFormatVisitor.Base
        implements JsonSchemaProducer {

    protected ArraySchema schema;

    public ArrayVisitor(ArraySchema schema) {
        this.schema = schema;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
    /*********************************************************************
     */

    @Override
    public JsonSchema getSchema() {
        return new TypeDecorationUtils(getProvider()).decorateWithTypeInformation(schema, null);
    }

    /*
    /*********************************************************************
    /* JsonArrayFormatVisitor
    /*********************************************************************
     */

    @Override
    public void itemsFormat(JsonFormatVisitable handler, JavaType contentType) throws JsonMappingException {
        SchemaGenerationContext context = SchemaGenerationContext.get();
        if (context.isVisited(contentType)) {
            //We should have a definition ready to go, just set a ref
            schema.setItemsSchema(context.getReferenceSchemaForVisitedType(contentType));
        } else {
            SchemaFactoryWrapper visitor = context.getNewSchemaFactoryWrapper();
            handler.acceptJsonFormatVisitor(visitor, contentType);
            JsonSchema itemSchema = visitor.finalSchema();
            schema.setItemsSchema(itemSchema);
            context.setVisitedAsNonPolymorphic(contentType);
            if (!(itemSchema instanceof ReferenceSchema)) {
                context.setSchemaForNonPolymorphicType(contentType, itemSchema);
            }
        }
    }

    @Override
    public void itemsFormat(JsonFormatTypes format) {
        schema.setItemsSchema(JsonSchema.minimalForFormat(format));
    }

}
