package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * Created by zoliszel on 10/06/2015.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class AnyOfSchema extends JsonSchema {

    public AnyOfSchema(ReferenceSchema[] anyOf) {
        this.anyOf = anyOf;
    }

    @Override
    public JsonFormatTypes getType() {
        return JsonFormatTypes.NULL;
    }

    public void setAnyOf(ReferenceSchema[] anyOf) {
        this.anyOf = anyOf;
    }

    public ReferenceSchema[] getAnyOf() {
        return anyOf;
    }

    @JsonProperty("anyOf")
    private ReferenceSchema[] anyOf;
}
