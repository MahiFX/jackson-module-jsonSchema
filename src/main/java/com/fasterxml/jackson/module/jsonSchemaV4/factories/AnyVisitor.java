package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;

public class AnyVisitor extends JsonAnyFormatVisitor.Base
        implements JsonSchemaProducer {
    protected final ObjectSchema schema;

    public AnyVisitor(ObjectSchema schema) {
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
    /* AnyVisitor: no additional methods...
    /*********************************************************************
     */
}
