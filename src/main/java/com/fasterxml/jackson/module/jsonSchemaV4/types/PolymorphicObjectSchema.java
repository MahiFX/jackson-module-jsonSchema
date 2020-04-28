package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectSchema extends ObjectSchema {

    public enum Type {
        ANY_OF, ALL_OF, ONE_OF, NOT;

    }

    public PolymorphicObjectSchema() {

    }

    @Override
    public JsonSchema.JSONType getType() {
        return type; //just return it for now;
    }

    @JsonProperty("anyOf")
    private ReferenceSchema[] anyOf;

    @JsonProperty("allOf")
    private ReferenceSchema[] allOf;

    @JsonProperty("oneOf")
    private ReferenceSchema[] oneOf;

    @JsonProperty("not")
    private ReferenceSchema not;

    public void setTypes(JsonFormatTypes[] types) {
        if (types.length == 1) {
            super.setType(new SingleJsonType(types[0]));
        } else {
            super.setType(new ArrayJsonType(types));
        }
    }

    public ReferenceSchema[] getAnyOf() {
        return anyOf;
    }

    public void setAnyOf(ReferenceSchema[] anyOf) {
        this.anyOf = anyOf;
    }

    public ReferenceSchema[] getAllOf() {
        return allOf;
    }

    public void setAllOf(ReferenceSchema[] allOf) {
        this.allOf = allOf;
    }

    public ReferenceSchema[] getOneOf() {
        return oneOf;
    }

    public void setOneOf(ReferenceSchema[] oneOf) {
        this.oneOf = oneOf;
    }

    public ReferenceSchema getNot() {
        return not;
    }

    public void setNot(ReferenceSchema not) {
        this.not = not;
    }

    @Override
    public boolean isPolymorhpicObjectSchema() {
        return true;
    }

    @Override
    public PolymorphicObjectSchema asPolymorphicObjectSchema() {
        return this;
    }
}
