package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * Created by zoliszel on 12/06/2015.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class PolymorphicObjectSchema extends ObjectSchema {
    @Override
    public JsonFormatTypes getType() {
        return JsonFormatTypes.OBJECT;
    }

    @JsonProperty("anyOf")
    private ReferenceSchema[] anyOf;

    @JsonProperty("allOf")
    private ReferenceSchema[] allOf;

    @JsonProperty("oneOf")
    private ReferenceSchema[] oneOf;

    @JsonProperty("not")
    private ReferenceSchema not;

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
}
