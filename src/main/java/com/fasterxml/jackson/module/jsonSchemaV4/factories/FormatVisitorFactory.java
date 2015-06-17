package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonBooleanFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNullFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.BooleanSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NullSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

/**
 * Factory class used for constructing visitors for building various
 * JSON Schema instances via visitor interface.
 */
public class FormatVisitorFactory {

    private final WrapperFactory wrapperFactory;

    public FormatVisitorFactory() {
        this(new WrapperFactory());
    }

    public FormatVisitorFactory(WrapperFactory wrapperFactory) {
        this.wrapperFactory = wrapperFactory;
    }

	/*
    /**********************************************************
    /* Factory methods for visitors, structured types
    /**********************************************************
     */

    /*
    public JsonAnyFormatVisitor anyFormatVisitor(ObjectSchema anySchema) {
        return new AnyVisitor(anySchema);
    }

    public JsonMapFormatVisitor mapFormatVisitor(SerializerProvider provider,
                                                 ObjectSchema objectSchema) {
        return new MapVisitor(provider, objectSchema, wrapperFactory);
    }

    public JsonObjectFormatVisitor objectFormatVisitor(SerializerProvider provider,
                                                       ObjectSchema objectSchema, VisitorContext visitorContext, JavaType convertedType) {
        ObjectVisitor visitor = new ObjectVisitor(provider, objectSchema, wrapperFactory,convertedType);
        visitor.setVisitorContext(visitorContext);
        return visitor;
    }


    protected JsonArrayFormatVisitor arrayFormatVisitor(SerializerProvider provider,
                                                        ArraySchema arraySchema, VisitorContext rvc,JavaType convertedType) {
        ArrayVisitor v = new ArrayVisitor(provider, arraySchema, wrapperFactory, convertedType);
        v.setVisitorContext(rvc);
        return v;
    }

*/
    public JsonArrayFormatVisitor arrayFormatVisitor(SerializerProvider provider,
                                                     ArraySchema arraySchema, VisitorContext visitorContext, JavaType convertedType, ObjectMapper originalMapper) {

        ArrayVisitor visitor = new ArrayVisitor(provider, arraySchema, wrapperFactory, convertedType);
        visitor.setVisitorContext(visitorContext);
        visitor.setOriginalMapper(originalMapper);
        return visitor;
    }




    protected JsonMapFormatVisitor mapFormatVisitor(SerializerProvider provider,
                                                    ObjectSchema objectSchema, VisitorContext rvc, ObjectMapper originalMapper) {
        MapVisitor v = new MapVisitor(provider, objectSchema, wrapperFactory);
        v.setVisitorContext(rvc);
        v.setOriginalMapper(originalMapper);
        return v;
    }

    protected JsonObjectFormatVisitor objectFormatVisitor(SerializerProvider provider,
                                                          ObjectSchema objectSchema, VisitorContext rvc, JavaType convertedType, ObjectMapper originalMapper) {
        ObjectVisitor v = new ObjectVisitor(provider, objectSchema, wrapperFactory, convertedType);
        v.setVisitorContext(rvc);
        v.setOriginalMapper(originalMapper);
        return v;
    }

    /*
    /**********************************************************
    /* Factory methods for visitors, value types
    /**********************************************************
     */

    public JsonBooleanFormatVisitor booleanFormatVisitor(BooleanSchema booleanSchema) {
        return new BooleanVisitor(booleanSchema);
    }

    public JsonIntegerFormatVisitor integerFormatVisitor(IntegerSchema integerSchema) {
        return new IntegerVisitor(integerSchema);
    }

    // no ValueTypeSchemaFactory, since null type has no formatting
    public JsonNullFormatVisitor nullFormatVisitor(NullSchema nullSchema) {
        return new NullVisitor(nullSchema);
    }

    public JsonNumberFormatVisitor numberFormatVisitor(NumberSchema numberSchema) {
        return new NumberVisitor(numberSchema);
    }

    public JsonStringFormatVisitor stringFormatVisitor(StringSchema stringSchema) {
        return new StringVisitor(stringSchema);
    }

    public PolymorphicObjectVisitor polymorphicObjectVisitor(SerializerProvider provider, PolymorphicObjectSchema s, VisitorContext visitorContext, JavaType type, ObjectMapper originalMapper) {
        PolymorphicObjectVisitor polymorphicObjectVisitor = new PolymorphicObjectVisitor(provider, s, type);
        polymorphicObjectVisitor.setVisitorContext(visitorContext);
        polymorphicObjectVisitor.setOriginalMapper(originalMapper);
        return polymorphicObjectVisitor;
    }
}