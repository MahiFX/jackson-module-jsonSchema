package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonBooleanFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchemaV4.types.BooleanSchema;

import java.util.Set;

public class BooleanVisitor extends JsonBooleanFormatVisitor.Base
        implements JsonSchemaProducer {
    protected final BooleanSchema schema;

    public BooleanVisitor(BooleanSchema schema) {
        this.schema = schema;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
    /*********************************************************************
     */

    @Override
    public BooleanSchema getSchema() {
        return schema;
    }
    
    /*
    /*********************************************************************
    /* JsonBooleanFormatVisitor impl
    /*********************************************************************
     */

    @Override
    public void enumTypes(Set<String> enums) {
        schema.setEnums(enums);
    }

    @Override
    public void format(JsonValueFormat format) {
        schema.setFormat(format);
    }
}
