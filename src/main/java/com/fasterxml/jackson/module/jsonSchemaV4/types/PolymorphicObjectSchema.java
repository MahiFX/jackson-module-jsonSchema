package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectSchema extends ObjectSchema {

    public enum Type {
        ANY_OF, ALL_OF, ONE_OF, NOT;

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

    @Override
    public ObjectSchema clone() {
        PolymorphicObjectSchema polymorphicObjectSchema = new PolymorphicObjectSchema();
        cloneObject(polymorphicObjectSchema);
        polymorphicObjectSchema.setAllOf(cloneArray(getAllOf()));
        polymorphicObjectSchema.setAnyOf(cloneArray(getAnyOf()));
        polymorphicObjectSchema.setNot(getNot() == null ? null : (ReferenceSchema) getNot().clone());
        polymorphicObjectSchema.setOneOf(cloneArray(getOneOf()));
        return polymorphicObjectSchema;
    }

    private ReferenceSchema[] cloneArray(ReferenceSchema[] refs) {
        if (refs == null) return refs;
        return Arrays.stream(refs).map(ReferenceSchema::clone).toArray(ReferenceSchema[]::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PolymorphicObjectSchema that = (PolymorphicObjectSchema) o;
        return Arrays.equals(getAnyOf(), that.getAnyOf()) && Arrays.equals(getAllOf(), that.getAllOf()) && Arrays.equals(getOneOf(), that.getOneOf()) && Objects.equals(getNot(), that.getNot());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getNot());
        result = 31 * result + Arrays.hashCode(getAnyOf());
        result = 31 * result + Arrays.hashCode(getAllOf());
        result = 31 * result + Arrays.hashCode(getOneOf());
        return result;
    }
}
