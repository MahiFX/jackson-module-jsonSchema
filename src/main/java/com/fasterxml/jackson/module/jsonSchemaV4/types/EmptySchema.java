package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * Created by zoliszel on 23/06/2015.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class EmptySchema extends JsonSchema {

    @JsonIgnore
    @Override
    public JSONType getType() {
        return new SingleJsonType(JsonFormatTypes.ANY);
    }


}
