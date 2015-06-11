package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * Created by zoliszel on 10/06/2015.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class OneOfSchema extends JsonSchema {

    public OneOfSchema(ReferenceSchema[] oneOf) {
        this.oneOf = oneOf;
    }

    @Override
    public JsonFormatTypes getType() {
        return JsonFormatTypes.NULL;
    }

    public void setOneOf(ReferenceSchema[] oneOf) {
        this.oneOf = oneOf;
    }

    public ReferenceSchema[] getOneOf() {
        return oneOf;
    }

    @JsonProperty("oneOf")
    private ReferenceSchema[] oneOf;
}
