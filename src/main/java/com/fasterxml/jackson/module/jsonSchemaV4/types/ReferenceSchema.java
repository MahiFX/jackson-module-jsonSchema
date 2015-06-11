package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * This type represents an JSON reference to a {@link JsonSchema}.
 *
 * @author adb
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class ReferenceSchema extends SimpleTypeSchema {
    @JsonProperty
    protected String $ref;

    public ReferenceSchema(String ref) {
        this.$ref = ref;
    }

    @Override
    @JsonIgnore
    public JsonFormatTypes getType() {
        return JsonFormatTypes.OBJECT;
    }

    @Override
    public String get$ref() {
        return $ref;
    }

    @Override
    public void set$ref(String $ref) {
        this.$ref = $ref;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof ReferenceSchema)) return false;
        return _equals((ReferenceSchema) obj);
    }

    protected boolean _equals(ReferenceSchema that) {
        return JsonSchema.equals($ref, that.$ref)
                && super._equals(that);
    }
}
