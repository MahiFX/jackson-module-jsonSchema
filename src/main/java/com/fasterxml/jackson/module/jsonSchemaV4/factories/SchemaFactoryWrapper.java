package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.VisitorUtils;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.BooleanSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NullSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author jphelan
 * @author tsaloranta
 */
public class SchemaFactoryWrapper implements PolymorphicJsonFormatVisitorWrapper, Visitor {
    protected FormatVisitorFactory visitorFactory;
    protected JsonSchemaFactory schemaProvider;
    protected SerializerProvider provider;
    protected JsonSchema schema;
    protected JavaType originalType;
    protected VisitorContext visitorContext;

    protected ObjectMapper originalMapper;

    public SchemaFactoryWrapper(ObjectMapper originalMapper) {
        this(originalMapper, null, new WrapperFactory());
    }

    public SchemaFactoryWrapper(ObjectMapper originalMapper, SerializerProvider p) {
        this(originalMapper, p, new WrapperFactory());
    }

    protected SchemaFactoryWrapper(ObjectMapper originalMapper, WrapperFactory wrapperFactory) {
        this(originalMapper, null, wrapperFactory);
    }

    protected SchemaFactoryWrapper(ObjectMapper originalMapper, SerializerProvider p, WrapperFactory wrapperFactory) {
        provider = p;
        schemaProvider = new JsonSchemaFactory();
        visitorFactory = new FormatVisitorFactory(wrapperFactory);
        this.originalMapper = originalMapper != null ? originalMapper.copy() : null;
        this.visitorContext= new VisitorContext();
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
    public SchemaFactoryWrapper setVisitorContext(VisitorContext rvc) {
        visitorContext = rvc;
        return this;
    }

    @Override
    public JsonAnyFormatVisitor expectAnyFormat(JavaType convertedType) {
        ObjectSchema s = schemaProvider.objectSchema();
        this.schema = s;
        this.originalType = convertedType;
        return new AnyVisitor(s);
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType convertedType) {
        ArraySchema s = schemaProvider.arraySchema();
        this.schema = s;
        this.originalType = convertedType;
        return visitorFactory.arrayFormatVisitor(provider, s, visitorContext, convertedType, originalMapper);
    }

    @Override
    public JsonBooleanFormatVisitor expectBooleanFormat(JavaType convertedType) {
        BooleanSchema s = schemaProvider.booleanSchema();
        this.schema = s;
        this.originalType = convertedType;
        return visitorFactory.booleanFormatVisitor(s);
    }

    @Override
    public JsonIntegerFormatVisitor expectIntegerFormat(JavaType convertedType) {
        IntegerSchema s = schemaProvider.integerSchema();
        this.schema = s;
        this.originalType = convertedType;
        return visitorFactory.integerFormatVisitor(s);
    }

    @Override
    public JsonNullFormatVisitor expectNullFormat(JavaType convertedType) {
        NullSchema s = schemaProvider.nullSchema();
        schema = s;
        this.originalType = convertedType;
        return visitorFactory.nullFormatVisitor(s);
    }

    @Override
    public JsonNumberFormatVisitor expectNumberFormat(JavaType convertedType) {
        NumberSchema s = schemaProvider.numberSchema();
        schema = s;
        this.originalType = convertedType;
        return visitorFactory.numberFormatVisitor(s);
    }

    @Override
    public JsonStringFormatVisitor expectStringFormat(JavaType convertedType) {
        StringSchema s = schemaProvider.stringSchema();
        schema = s;
        this.originalType = convertedType;
        return visitorFactory.stringFormatVisitor(s);
    }

    @Override
    public JsonMapFormatVisitor expectMapFormat(JavaType convertedType)
            throws JsonMappingException {
        /* 22-Nov-2012, tatu: Looks as if JSON Schema did not have
         *   concept of Map (distinct from Record or Object); so best
         *   we can do is to consider it a vague kind-a Object...
         */
        ObjectSchema s = schemaProvider.objectSchema();
        schema = s;
        this.originalType = convertedType;
        return visitorFactory.mapFormatVisitor(provider, s, visitorContext, originalMapper);
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
        ObjectSchema s = schemaProvider.objectSchema();
        schema = s;
        this.originalType = convertedType;
        assert(visitorContext!=null);

        // give each object schema a reference id and keep track of the ones we've seen
        String schemaUri = visitorContext.addSeenSchemaUri(convertedType);
        if (schemaUri != null) {
            s.setId(schemaUri);
        }

        return visitorFactory.objectFormatVisitor(provider, s, visitorContext, convertedType, originalMapper);
    }


    @Override
    public PolymorphicObjectVisitor expectPolyMorphicObjectFormat(JavaType convertedType) throws JsonMappingException {
        PolymorphicObjectSchema s = schemaProvider.polymorphicObjectSchema();
        schema = s;
        this.originalType = convertedType;
        assert(visitorContext!=null);

        return visitorFactory.polymorphicObjectVisitor(provider, s, visitorContext, convertedType, originalMapper);
    }

    /*
    /*********************************************************************
    /* API
    /*********************************************************************
     */

    public JsonSchema finalSchema() {
        JsonSchema result = schema;
        if (originalType != null) {
            result = new VisitorUtils(originalMapper, visitorContext, provider).decorateWithTypeInformation(schema, originalType);
        }
        result = PolymorphicHandlingUtil.propagateDefinitionsUp(result);
      //  System.out.println(toJson(result,result.getClass(),new ObjectMapper()));
        return result;
    }

    public static String toJson(Object o, Type type, ObjectMapper mapper) {
        try {
            return mapper.writer().writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
