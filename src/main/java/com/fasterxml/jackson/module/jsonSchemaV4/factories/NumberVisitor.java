package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NumberSchema;

import java.util.Set;

public class NumberVisitor extends JsonNumberFormatVisitor.Base
        implements JsonSchemaProducer {
    protected final NumberSchema schema;

    public NumberVisitor(NumberSchema schema) {
        this.schema = schema;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
    /*********************************************************************
     */

    @Override
    public NumberSchema getSchema() {
        return schema;
    }
    
    /*
    /*********************************************************************
    /* JsonNumberFormatVisitor
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
