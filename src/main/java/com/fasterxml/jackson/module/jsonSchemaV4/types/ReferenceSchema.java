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
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class ReferenceSchema extends SimpleTypeSchema {
    @JsonProperty
    protected String $ref;

    public ReferenceSchema(String ref, JSONType referedSchemaType) {
        this.$ref = ref;
        this.type = referedSchemaType;
    }

    public ReferenceSchema() {
        this.type = new SingleJsonType(JsonFormatTypes.OBJECT);
    }

    @Override
    @JsonIgnore
    public JSONType getType() {
        return type;
    }

    @Override
    public JsonSchema clone() {
        ReferenceSchema referenceSchema = new ReferenceSchema();
        cloneSimple(referenceSchema);
        referenceSchema.set$ref(get$ref());
        referenceSchema.setType(getType());
        return referenceSchema;
    }

    private JSONType type;

    public void setType(JSONType type) {
        this.type = type;
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

    @Override
    public boolean isReferenceSchema() {
        return true;
    }

    @Override
    public ReferenceSchema asReferenceSchema() {
        return this;
    }
}
