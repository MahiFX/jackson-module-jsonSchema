package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * This class represents a {@link JsonSchema} as an integer type
 *
 * @author jphelan
 */
public class IntegerSchema extends NumberSchema {
    /**
     * A numeric instance is valid against "multipleOf" if the result of the division of the instance by this keyword's value is an integer.
     */
    private Integer multipleOf;

    @Override
    public boolean isIntegerSchema() {
        return true;
    }

    @Override
    public JSONType getType() {
        return new SingleJsonType(JsonFormatTypes.INTEGER);
    }

    @Override
    public IntegerSchema asIntegerSchema() {
        return this;
    }

    @JsonProperty
    public Integer getMultipleOf() {
        return multipleOf;
    }

    public void setMultipleOf(Integer multipleOf) {
        this.multipleOf = multipleOf;
    }

    @Override
    public JsonSchema clone() {
        IntegerSchema integerSchema = new IntegerSchema();
        cloneNumber(integerSchema);
        integerSchema.setMultipleOf(getMultipleOf());
        return integerSchema;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof IntegerSchema)) return false;
        return _equals((IntegerSchema) obj);
    }

    protected boolean _equals(IntegerSchema that) {
        return equals(getMultipleOf(), that.getMultipleOf())
                && super.equals(that);
    }
}
