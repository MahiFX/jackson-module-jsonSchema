package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonBooleanFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNullFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.BooleanSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NullSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

/**
 * @author jphelan
 * @author tsaloranta
 */
public class SchemaFactoryWrapper implements PolymorphicJsonFormatVisitorWrapper, Visitor {
    protected FormatVisitorFactory visitorFactory;
    protected JsonSchemaFactory schemaProvider;
    protected SerializerProvider provider;
    protected JsonSchema schema;
    protected VisitorContext visitorContext;

    public SchemaFactoryWrapper() {
        this(null, new WrapperFactory());
    }

    public SchemaFactoryWrapper(SerializerProvider p) {
        this(p, new WrapperFactory());
    }

    protected SchemaFactoryWrapper(WrapperFactory wrapperFactory) {
        this(null, wrapperFactory);
    }

    protected SchemaFactoryWrapper(SerializerProvider p, WrapperFactory wrapperFactory) {
        provider = p;
        schemaProvider = new JsonSchemaFactory();
        visitorFactory = new FormatVisitorFactory(wrapperFactory);
    }

    /*
    /*********************************************************************
    /* JsonFormatVisitorWrapper implementation
    /*********************************************************************
     */

    @Override
    public SerializerProvider getProvider() {
        return provider;
    }

    @Override
    public void setProvider(SerializerProvider p) {
        provider = p;
    }

    @Override
    public JsonAnyFormatVisitor expectAnyFormat(JavaType convertedType) {
        ObjectSchema s = schemaProvider.objectSchema();
        this.schema = s;
        return new AnyVisitor(s);
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType convertedType) {
        ArraySchema s = schemaProvider.arraySchema();
        this.schema = s;
        return visitorFactory.arrayFormatVisitor(provider, s, visitorContext);
    }

    @Override
    public JsonBooleanFormatVisitor expectBooleanFormat(JavaType convertedType) {
        BooleanSchema s = schemaProvider.booleanSchema();
        this.schema = s;
        return visitorFactory.booleanFormatVisitor(s);
    }

    @Override
    public JsonIntegerFormatVisitor expectIntegerFormat(JavaType convertedType) {
        IntegerSchema s = schemaProvider.integerSchema();
        this.schema = s;
        return visitorFactory.integerFormatVisitor(s);
    }

    @Override
    public JsonNullFormatVisitor expectNullFormat(JavaType convertedType) {
        NullSchema s = schemaProvider.nullSchema();
        schema = s;
        return visitorFactory.nullFormatVisitor(s);
    }

    @Override
    public JsonNumberFormatVisitor expectNumberFormat(JavaType convertedType) {
        NumberSchema s = schemaProvider.numberSchema();
        schema = s;
        return visitorFactory.numberFormatVisitor(s);
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
        ObjectSchema s = schemaProvider.objectSchema();
        schema = s;

        // if we don't already have a recursive visitor context, create one
        if (visitorContext == null) {
            visitorContext = new VisitorContext();
        }

        // give each object schema a reference id and keep track of the ones we've seen
        String schemaUri = visitorContext.addSeenSchemaUri(convertedType);
        if (schemaUri != null) {
            s.setId(schemaUri);
        }

        return visitorFactory.objectFormatVisitor(provider, s, visitorContext);
    }

    @Override
    public JsonStringFormatVisitor expectStringFormat(JavaType convertedType) {
        StringSchema s = schemaProvider.stringSchema();
        schema = s;
        return visitorFactory.stringFormatVisitor(s);
    }

    @Override
    public JsonMapFormatVisitor expectMapFormat(JavaType type)
            throws JsonMappingException {
        /* 22-Nov-2012, tatu: Looks as if JSON Schema did not have
         *   concept of Map (distinct from Record or Object); so best
         *   we can do is to consider it a vague kind-a Object...
         */
        ObjectSchema s = schemaProvider.objectSchema();
        schema = s;
        return visitorFactory.mapFormatVisitor(provider, s, visitorContext);
    }

    @Override
    public SchemaFactoryWrapper setVisitorContext(VisitorContext rvc) {
        visitorContext = rvc;
        return this;
    }

    /*
    /*********************************************************************
    /* API
    /*********************************************************************
     */

    public JsonSchema finalSchema() {
        return schema;
    }

    @Override
    public PolymorphicObjectVisitor expectPolyMorphicObjectFormat(JavaType type) throws JsonMappingException {
        PolymorphicObjectSchema s = schemaProvider.polymorphicObjectSchema();
        schema = s;
        // if we don't already have a recursive visitor context, create one
        if (visitorContext == null) {
            visitorContext = new VisitorContext();
        }

        // give each object schema a reference id and keep track of the ones we've seen
        String schemaUri = visitorContext.addSeenSchemaUri(type);
        if (schemaUri != null) {
            s.setId(schemaUri);
        }


        return visitorFactory.polymorphicObjectVisitor(visitorContext, s, type);
    }
}
