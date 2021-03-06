package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * This class represents a {@link JsonSchema} as a null type
 *
 * @author jphelan
 */
public class NullSchema extends SimpleTypeSchema {
    @Override
    public NullSchema asNullSchema() {
        return this;
    }

    @Override
    public JSONType getType() {
        return new SingleJsonType(JsonFormatTypes.NULL);
    }

    @Override
    public NullSchema clone() {
        return new NullSchema();
    }

    @Override
    public boolean isNullSchema() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof NullSchema)) return false;
        return _equals((NullSchema) obj);
    }
}
