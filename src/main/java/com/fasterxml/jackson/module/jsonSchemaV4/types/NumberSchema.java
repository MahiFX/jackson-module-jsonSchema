package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * This class represents a {@link JsonSchema} as a number type
 *
 * @author jphelan
 */
public class NumberSchema extends ValueTypeSchema {
    /**
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "maximum" attribute.
     */
    @JsonProperty
    private Boolean exclusiveMaximum;

    /**
     * This attribute indicates if the value of the instance (if the
     * instance is a number) can not equal the number defined by the
     * "minimum" attribute.
     */
    @JsonProperty
    private Boolean exclusiveMinimum;

    /**
     * This attribute defines the maximum value of the instance property
     */
    @JsonProperty
    private Double maximum = null;

    /**
     * This attribute defines the minimum value of the instance property
     */
    @JsonProperty
    private Double minimum = null;

    @Override
    public NumberSchema asNumberSchema() {
        return this;
    }

    public Boolean getExclusiveMaximum() {
        return exclusiveMaximum;
    }

    public Boolean getExclusiveMinimum() {
        return exclusiveMinimum;
    }

    public Double getMaximum() {
        return maximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsonSchema.types.JsonSchema#getType()
     */
    @Override
    public JSONType getType() {
        return new SingleJsonType(JsonFormatTypes.NUMBER);
    }

    @Override
    public JsonSchema clone() {
        NumberSchema numberSchema = new NumberSchema();
        cloneNumber(numberSchema);
        return numberSchema;
    }

    protected void cloneNumber(NumberSchema numberSchema) {
        cloneValue(numberSchema);
        numberSchema.setExclusiveMaximum(getExclusiveMaximum());
        numberSchema.setExclusiveMinimum(getExclusiveMinimum());
        numberSchema.setMaximum(getMaximum());
        numberSchema.setMinimum(getMinimum());
    }


    @Override
    public boolean isNumberSchema() {
        return true;
    }

    public void setExclusiveMaximum(Boolean exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    public void setExclusiveMinimum(Boolean exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof NumberSchema)) return false;
        return _equals((NumberSchema) obj);
    }

    protected boolean _equals(NumberSchema that) {
        return JsonSchema.equals(getExclusiveMaximum(), that.getExclusiveMaximum()) &&
                JsonSchema.equals(getExclusiveMinimum(), that.getExclusiveMinimum()) &&
                JsonSchema.equals(getMaximum(), that.getMaximum()) &&
                JsonSchema.equals(getMinimum(), that.getMinimum()) &&
                super._equals(that);
    }

}
