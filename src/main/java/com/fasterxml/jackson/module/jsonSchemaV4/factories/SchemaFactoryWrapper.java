package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.*;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.TypeDecorationUtils;
import com.fasterxml.jackson.module.jsonSchemaV4.types.*;

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
        this.schema = simulatedAny;

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
        if (context.isVisited(convertedType, true)) {
            schema = context.getReferenceSchemaForVisitedType(convertedType);
            return new JsonObjectFormatVisitor.Base();
        }

        ObjectSchema s = context.getSchemaProvider().objectSchema();
        schema = s;
        this.originalType = convertedType;
        if (!context.isWithAdditionalProperties()) {
            s.setAdditionalProperties(ObjectSchema.NoAdditionalProperties.instance);
        }

        // give each object schema a reference id and keep track of the ones we've seen
        context.setVisitedAsNonPolymorphic(convertedType);

        if(convertedType != null) {
            String id = SchemaGenerationContext.javaTypeToUrn(toCanonicalName(convertedType));
            context.setSchemaRefForNonPolymorphicType(convertedType, id);
            s.setId(id);
        }
        context.setFormatTypeForVisitedType(convertedType, new JsonSchema.SingleJsonType(JsonFormatTypes.OBJECT));
        JsonObjectFormatVisitor visitor = context.getVisitorFactory().objectFormatVisitor(s, convertedType);
        visitor.setProvider(getProvider());
        return visitor;
    }

    private String toCanonicalName(JavaType convertedType) {
        try {
            TypeSerializer serializer = getProvider().findTypeSerializer(convertedType);
            if (serializer != null) {
                TypeIdResolver typeIdResolver = serializer.getTypeIdResolver();
                String typeName = typeIdResolver.idFromBaseType();
                String packageName = convertedType.getRawClass().getPackage().getName();
                return packageName + "." + typeName;
            } else {
                return convertedType.toCanonical();
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public PolymorphicObjectVisitor expectPolyMorphicObjectFormat(JavaType convertedType) throws JsonMappingException {
        PolymorphicObjectSchema s = SchemaGenerationContext.get().getSchemaProvider().polymorphicObjectSchema();
        schema = s;
        this.originalType = convertedType;
        return SchemaGenerationContext.get().getVisitorFactory().polymorphicObjectVisitor(s, getProvider());
    }

    /*
    /*********************************************************************
    /* API
    /*********************************************************************
     */

    public JsonSchema finalSchema() {
        JsonSchema result = schema;
        if (originalType != null) {
            result = new TypeDecorationUtils(getProvider()).decorateWithTypeInformation(schema, originalType);
        }
        result = PolymorphicSchemaUtil.wrapNonNumericTypes(result);
        result = PolymorphicSchemaUtil.propagateDefinitionsUp(result);

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
        this.provider = provider;
    }
}
