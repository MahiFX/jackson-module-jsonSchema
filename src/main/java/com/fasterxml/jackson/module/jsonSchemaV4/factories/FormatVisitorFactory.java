package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.*;
import com.fasterxml.jackson.module.jsonSchemaV4.types.*;

/**
 * Factory class used for constructing visitors for building various
 * JSON Schema instances via visitor interface.
 */
public class FormatVisitorFactory {

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
    public JsonArrayFormatVisitor arrayFormatVisitor(ArraySchema arraySchema, JavaType convertedType) {
        ArrayVisitor visitor = new ArrayVisitor(arraySchema, convertedType);
        return visitor;
    }


    protected JsonMapFormatVisitor mapFormatVisitor(ObjectSchema objectSchema) {
        MapVisitor v = new MapVisitor(objectSchema);
        return v;
    }

    protected JsonObjectFormatVisitor objectFormatVisitor(ObjectSchema objectSchema, JavaType convertedType) {
        ObjectVisitor v = new ObjectVisitor(objectSchema, convertedType);
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

    public PolymorphicObjectVisitor polymorphicObjectVisitor(PolymorphicObjectSchema s, JavaType type, SerializerProvider provider) {
        return new PolymorphicObjectVisitor(s, type, provider);
    }
}
