package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.module.jsonSchemaV4.types.AnySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.BooleanSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NullSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

public class JsonSchemaFactory {
    public AnySchema anySchema() {
        return new AnySchema();
    }

    public ArraySchema arraySchema() {
        return new ArraySchema();
    }

    public BooleanSchema booleanSchema() {
        return new BooleanSchema();
    }

    public IntegerSchema integerSchema() {
        return new IntegerSchema();
    }

    public NullSchema nullSchema() {
        return new NullSchema();
    }

    public NumberSchema numberSchema() {
        return new NumberSchema();
    }

    public ObjectSchema objectSchema() {
        return new ObjectSchema();
    }

    public StringSchema stringSchema() {
        return new StringSchema();
    }
}