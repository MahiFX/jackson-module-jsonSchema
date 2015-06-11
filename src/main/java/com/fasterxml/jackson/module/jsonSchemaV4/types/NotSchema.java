package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * Created by zoliszel on 10/06/2015.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class NotSchema extends JsonSchema {

    public NotSchema(JsonSchema not) {
        this.not = not;
    }

    @Override
    public JsonFormatTypes getType() {
        return JsonFormatTypes.NULL;
    }

    public void setNot(JsonSchema not) {
        this.not = not;
    }

    @JsonProperty("not")
    private JsonSchema not;
}
