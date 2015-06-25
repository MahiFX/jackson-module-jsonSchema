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
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
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
public class SchemaFactoryWrapper implements PolymorphicJsonFormatVisitorWrapper {

    protected JsonSchema schema;

    protected JavaType originalType;

    protected SchemaFactoryWrapper() {

    }

    /*
    /*********************************************************************
    /* JsonFormatVisitorWrapper implementation
    /*********************************************************************
     */

    /**
     * Any is not supported in V4. Backbridging it with Object Format
     */
    @Override
    public JsonAnyFormatVisitor expectAnyFormat(JavaType convertedType) {
        PolymorphicObjectSchema simulatedAny = SchemaGenerationContext.get().getSchemaProvider().polymorphicObjectSchema();
        simulatedAny.setTypes(AnyVisitor.FORMAT_TYPES_EXCEPT_ANY);
        this.schema=simulatedAny;

        return new AnyVisitor(this.schema);
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType convertedType) {
        ArraySchema s = SchemaGenerationContext.get().getSchemaProvider().arraySchema();
        this.schema = s;
        this.originalType = convertedType;
        JsonArrayFormatVisitor visitor = SchemaGenerationContext.get().getVisitorFactory().arrayFormatVisitor(s, convertedType);
        visitor.setProvider(getProvider());
        return visitor;
    }

    @Override
    public JsonBooleanFormatVisitor expectBooleanFormat(JavaType convertedType) {
        BooleanSchema s = SchemaGenerationContext.get().getSchemaProvider().booleanSchema();
        this.schema = s;
        this.originalType = convertedType;
        return SchemaGenerationContext.get().getVisitorFactory().booleanFormatVisitor(s);
    }

    @Override
    public JsonIntegerFormatVisitor expectIntegerFormat(JavaType convertedType) {
        IntegerSchema s = SchemaGenerationContext.get().getSchemaProvider().integerSchema();
        this.schema = s;
        this.originalType = convertedType;
        return SchemaGenerationContext.get().getVisitorFactory().integerFormatVisitor(s);
    }

    @Override
    public JsonNullFormatVisitor expectNullFormat(JavaType convertedType) {
        NullSchema s = SchemaGenerationContext.get().getSchemaProvider().nullSchema();
        schema = s;
        this.originalType = convertedType;
        return SchemaGenerationContext.get().getVisitorFactory().nullFormatVisitor(s);
    }

    @Override
    public JsonNumberFormatVisitor expectNumberFormat(JavaType convertedType) {
        NumberSchema s = SchemaGenerationContext.get().getSchemaProvider().numberSchema();
        schema = s;
        this.originalType = convertedType;
        return SchemaGenerationContext.get().getVisitorFactory().numberFormatVisitor(s);
    }

    @Override
    public JsonStringFormatVisitor expectStringFormat(JavaType convertedType) {
        StringSchema s = SchemaGenerationContext.get().getSchemaProvider().stringSchema();
        schema = s;
        this.originalType = convertedType;
        return SchemaGenerationContext.get().getVisitorFactory().stringFormatVisitor(s);
    }

    @Override
    public JsonMapFormatVisitor expectMapFormat(JavaType convertedType)
            throws JsonMappingException {
        /* 22-Nov-2012, tatu: Looks as if JSON Schema did not have
         *   concept of Map (distinct from Record or Object); so best
         *   we can do is to consider it a vague kind-a Object...
         */
        ObjectSchema s = SchemaGenerationContext.get().getSchemaProvider().objectSchema();
        schema = s;
        this.originalType = convertedType;
        JsonMapFormatVisitor visitor = SchemaGenerationContext.get().getVisitorFactory().mapFormatVisitor(s);
        visitor.setProvider(getProvider());
        return visitor;
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
        SchemaGenerationContext context = SchemaGenerationContext.get();
        ObjectSchema s = context.getSchemaProvider().objectSchema();
        schema = s;
        this.originalType = convertedType;
        if(!context.isWithAdditonalProperties()){
            s.setAdditionalProperties(ObjectSchema.NoAdditionalProperties.instance);
        }

        // give each object schema a reference id and keep track of the ones we've seen
        String schemaUri = context.addSeenSchemaUri(convertedType, s.getType());
        if (schemaUri != null) {
            s.setId(schemaUri);
        }
        JsonObjectFormatVisitor visitor = context.getVisitorFactory().objectFormatVisitor(s ,convertedType);
        visitor.setProvider(getProvider());
        return visitor;
    }


    @Override
    public PolymorphicObjectVisitor expectPolyMorphicObjectFormat(JavaType convertedType) throws JsonMappingException {
        PolymorphicObjectSchema s = SchemaGenerationContext.get().getSchemaProvider().polymorphicObjectSchema();
        schema = s;
        this.originalType = convertedType;
        return SchemaGenerationContext.get().getVisitorFactory().polymorphicObjectVisitor( s, convertedType,getProvider());
    }

    /*
    /*********************************************************************
    /* API
    /*********************************************************************
     */

    public JsonSchema finalSchema() {
        JsonSchema result = schema;
        if (originalType != null) {
            result = new VisitorUtils(getProvider()).decorateWithTypeInformation(schema, originalType);
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

    private SerializerProvider provider;
    @Override
    public SerializerProvider getProvider() {
        return provider;
    }

    @Override
    public void setProvider(SerializerProvider provider) {
        this.provider=provider;
    }
}
