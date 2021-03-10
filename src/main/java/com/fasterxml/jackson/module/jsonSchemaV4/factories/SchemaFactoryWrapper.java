package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.*;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.TypeDecorationUtils;
import com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer.PolymorphicObjectSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.types.*;

import static com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext.isNotJvmType;

/**
 * @author jphelan
 * @author tsaloranta
 */
public class SchemaFactoryWrapper implements PolymorphicJsonFormatVisitorWrapper {

    protected final SchemaGenerationContext schemaGenerationContext;

    protected JsonSchema schema;

    protected JavaType originalType;

    protected SchemaFactoryWrapper() {
        this.schemaGenerationContext = SchemaGenerationContext.get();
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
        PolymorphicObjectSchema simulatedAny = schemaGenerationContext.getSchemaProvider().polymorphicObjectSchema();
        simulatedAny.setTypes(AnyVisitor.FORMAT_TYPES_EXCEPT_ANY);
        this.schema = simulatedAny;

        return new AnyVisitor(this.schema);
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType arrayType) {
        ArraySchema s = schemaGenerationContext.getSchemaProvider().arraySchema();
        this.schema = s;
        this.originalType = arrayType;
        JsonArrayFormatVisitor visitor = schemaGenerationContext.getVisitorFactory().arrayFormatVisitor(s);
        visitor.setProvider(getProvider());
        JavaType itemType = arrayType.getContentType();
        if (schemaGenerationContext.isVisited(itemType) && (arrayType.isCollectionLikeType() || arrayType.isArrayType()) &&
                isNotJvmType(arrayType.getContentType())) {

            schemaGenerationContext.setVisitedAsNonPolymorphic(arrayType);

            //Need to create a definition for item type so ArrayVisitor can reference that
            try {
                if (isNotJvmType(itemType) && isNotPolymorphic(itemType)) {
                    schemaGenerationContext.createDefinitionForNonPolymorphic(itemType, schema);

                }
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            }
        }

        return visitor;
    }

    private boolean isNotPolymorphic(JavaType itemType) {
        return !PolymorphicObjectSerializer.isPolyMorphic(schemaGenerationContext.getMapper().getSerializationConfig(), itemType);
    }

    @Override
    public JsonBooleanFormatVisitor expectBooleanFormat(JavaType convertedType) {
        BooleanSchema s = schemaGenerationContext.getSchemaProvider().booleanSchema();
        this.schema = s;
        this.originalType = convertedType;
        return schemaGenerationContext.getVisitorFactory().booleanFormatVisitor(s);
    }

    @Override
    public JsonIntegerFormatVisitor expectIntegerFormat(JavaType convertedType) {
        IntegerSchema s = schemaGenerationContext.getSchemaProvider().integerSchema();
        this.schema = s;
        this.originalType = convertedType;
        return schemaGenerationContext.getVisitorFactory().integerFormatVisitor(s);
    }

    @Override
    public JsonNullFormatVisitor expectNullFormat(JavaType convertedType) {
        NullSchema s = schemaGenerationContext.getSchemaProvider().nullSchema();
        schema = s;
        this.originalType = convertedType;
        return schemaGenerationContext.getVisitorFactory().nullFormatVisitor(s);
    }

    @Override
    public JsonNumberFormatVisitor expectNumberFormat(JavaType convertedType) {
        NumberSchema s = schemaGenerationContext.getSchemaProvider().numberSchema();
        schema = s;
        this.originalType = convertedType;
        return schemaGenerationContext.getVisitorFactory().numberFormatVisitor(s);
    }

    @Override
    public JsonStringFormatVisitor expectStringFormat(JavaType convertedType) {
        StringSchema s = schemaGenerationContext.getSchemaProvider().stringSchema();
        schema = s;
        this.originalType = convertedType;
        return schemaGenerationContext.getVisitorFactory().stringFormatVisitor(s);
    }

    @Override
    public JsonMapFormatVisitor expectMapFormat(JavaType mapType) {
        /* 22-Nov-2012, tatu: Looks as if JSON Schema did not have
         *   concept of Map (distinct from Record or Object); so best
         *   we can do is to consider it a vague kind-a Object...
         */
        ObjectSchema s = schemaGenerationContext.getSchemaProvider().objectSchema();
        schema = s;
        this.originalType = mapType;
        JsonMapFormatVisitor visitor = schemaGenerationContext.getVisitorFactory().mapFormatVisitor(s);
        visitor.setProvider(getProvider());

        JavaType keyType = mapType.getKeyType();
        JavaType valueType = mapType.getContentType();

        if (schemaGenerationContext.isVisited(valueType) && isNotJvmType(valueType) && isNotPolymorphic(keyType)) {

            schemaGenerationContext.setVisitedAsNonPolymorphic(mapType);

            //Need to create a definition for item type so MapVisitor can reference that
            try {
                schemaGenerationContext.createDefinitionForNonPolymorphic(valueType, schema);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            }
        }
        if (schemaGenerationContext.isVisited(keyType) && isNotJvmType(keyType) && isNotPolymorphic(keyType)) {
            //Need to create a definition for item type so MapVisitor can reference that
            try {
                schemaGenerationContext.createDefinitionForNonPolymorphic(keyType, schema);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            }
        }

        return visitor;
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
        SchemaGenerationContext context = schemaGenerationContext;

        ObjectSchema s = context.getSchemaProvider().objectSchema();
        JsonObjectFormatVisitor visitor = context.getVisitorFactory().objectFormatVisitor(s);
        visitor.setProvider(getProvider());

        if (context.isVisited(convertedType, true)) {
            schema = context.getReferenceSchemaForVisitedType(convertedType);
            s.set$ref(schema.get$ref());
            return visitor;
        }

        if (isNotJvmType(convertedType)) {
            context.setVisitedAsNonPolymorphic(convertedType);
        }

        schema = s;
        this.originalType = convertedType;
        if (!context.isWithAdditionalProperties()) {
            s.setAdditionalProperties(ObjectSchema.NoAdditionalProperties.instance);
        }

        String id = context.getIdForType(convertedType);
        s.setId(id);

        return visitor;
    }


    @Override
    public PolymorphicObjectVisitor expectPolyMorphicObjectFormat(JavaType convertedType) {
        PolymorphicObjectSchema s = schemaGenerationContext.getSchemaProvider().polymorphicObjectSchema();
        schema = s;
        this.originalType = convertedType;
        return schemaGenerationContext.getVisitorFactory().polymorphicObjectVisitor(s, getProvider());
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
        result = PolymorphicSchemaUtil.wrapNonNumericTypes(result, schemaGenerationContext);
        PolymorphicSchemaUtil.propagateDefinitionsUp(result);
        return result;
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
