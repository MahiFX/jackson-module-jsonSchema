package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

import java.util.Set;

public class StringVisitor extends JsonStringFormatVisitor.Base
        implements JsonSchemaProducer {
    protected final StringSchema schema;

    public StringVisitor(StringSchema schema) {
        this.schema = schema;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
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

    @Override
    public StringSchema getSchema() {
        return schema;
    }
}
