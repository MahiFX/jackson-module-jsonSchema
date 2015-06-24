package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

/**
 * While JSON Schema does not have notion of "Map" type (unlimited property
 * names), Jackson has, so the distinction is exposed. We will need
 * to handle it here, produce JSON Schema Object type.
 */
public class MapVisitor extends JsonMapFormatVisitor.Base
        implements JsonSchemaProducer {
    protected final ObjectSchema schema;


    public MapVisitor(ObjectSchema schema) {
        this.schema = schema;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
    /*********************************************************************
     */

    @Override
    public ObjectSchema getSchema() {
        return schema;
    }
    
    /*
    /*********************************************************************
    /* JsonMapFormatVisitor
    /*********************************************************************
     */


    @Override
    public void keyFormat(JsonFormatVisitable handler, JavaType keyType)
            throws JsonMappingException {
        // JSON Schema only allows String types so let's not bother too much
    }

    @Override
    public void valueFormat(JsonFormatVisitable handler, JavaType valueType)
            throws JsonMappingException {

        // ISSUE #24: https://github.com/FasterXML/jackson-module-jsonSchema/issues/24

        JsonSchema valueSchema = propertySchema(handler, valueType);
        ObjectSchema.AdditionalProperties ap = new ObjectSchema.SchemaAdditionalProperties(valueSchema.asSimpleTypeSchema());
        this.schema.setAdditionalProperties(ap);
    }

    protected JsonSchema propertySchema(JsonFormatVisitable handler, JavaType propertyTypeHint)
            throws JsonMappingException {

        // check if we've seen this sub-schema already and return a reference-schema if we have
        String seenSchemaUri = SchemaGenerationContext.get().getSeenSchemaUri(propertyTypeHint);
        if (seenSchemaUri != null) {
            return new ReferenceSchema(seenSchemaUri,SchemaGenerationContext.get().getJsonTypeForVisitedSchema(propertyTypeHint));
        }

        SchemaFactoryWrapper visitor = SchemaGenerationContext.get().getNewSchemaFactoryWrapper(getProvider());
        handler.acceptJsonFormatVisitor(visitor, propertyTypeHint);
        return visitor.finalSchema();
    }
}
