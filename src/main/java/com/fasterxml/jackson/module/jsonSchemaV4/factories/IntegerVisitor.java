package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchemaV4.types.IntegerSchema;

import java.util.Set;

public class IntegerVisitor extends JsonIntegerFormatVisitor.Base
        implements JsonSchemaProducer {
    protected final IntegerSchema schema;

    public IntegerVisitor(IntegerSchema schema) {
        this.schema = schema;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
    /*********************************************************************
     */

    @Override
    public IntegerSchema getSchema() {
        return schema;
    }

    /*
    /*********************************************************************
    /* JsonIntegerFormatVisitor
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
