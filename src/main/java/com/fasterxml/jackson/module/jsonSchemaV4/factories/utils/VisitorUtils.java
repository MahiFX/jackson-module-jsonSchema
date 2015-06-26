package com.fasterxml.jackson.module.jsonSchemaV4.factories.utils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class VisitorUtils {

/*
    public static String translateToV4(JsonValueFormat format) {
        if (format == null) {
            return null;
        }
        switch (format) {
            case DATE_TIME:
            case EMAIL:
            case HOST_NAME:
            case IPV6:
                return format.toString();
            case IP_ADDRESS:
                return "ipv4";

            case URI:
            case DATE:
            case TIME:
            case UTC_MILLISEC:
            case REGEX:
            case COLOR:
            case STYLE:
            case PHONE:
            default:
                return null;
        }
    }
    */

    public static class TypeInfo {

        public static final TypeInfo NOT_AVAILABLE = new TypeInfo(null,null,null);

        private final String typeName;

        private final JsonTypeInfo.As typeInclusion;

        private final String propertyName;

        public TypeInfo(String typeName, JsonTypeInfo.As typeInclusion, String propertyName) {
            this.typeName = typeName;
            this.typeInclusion = typeInclusion;
            this.propertyName = propertyName;
        }

        public String getTypeName() {
            return typeName;
        }

        public JsonTypeInfo.As getTypeInclusion() {
            return typeInclusion;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }

    private SerializerProvider provider;

    public VisitorUtils(SerializerProvider provider) {
        this.provider = provider;
    }

    protected JsonSchema schema(Type t) {
        try {
            SchemaGenerationContext context = SchemaGenerationContext.get();
            ObjectMapper mapper = context.getMapper().copy();
            SchemaFactoryWrapper visitor = context.getNewSchemaFactoryWrapper(null);
            mapper.acceptJsonFormatVisitor(mapper.constructType(t), visitor);
            return visitor.finalSchema();
        } catch (JsonMappingException e) {
            //TODO throw and sort out exception
            return null;
        }
    }


    public JsonSchema decorateWithTypeInformation(JsonSchema originalSchema, JavaType originalType) {
        TypeInfo typeInfo = extractTypeInformation(originalType);
        if(typeInfo == TypeInfo.NOT_AVAILABLE){
            return originalSchema;
        }

        StringSchema typeSchema = SchemaGenerationContext.get().getSchemaProvider().stringSchema();

        if (typeInfo.getTypeName()!=null) {
            Set<String> allowedValues = new HashSet<String>();
            allowedValues.add(typeInfo.getTypeName());
            typeSchema.setEnums(allowedValues);
        }

        switch (typeInfo.getTypeInclusion()) {
            case PROPERTY:
                if (originalSchema instanceof PolymorphicObjectSchema) {
                    return originalSchema; //PolymorphicObjects will have type information in it's sub-schemas
                }

                if (originalSchema instanceof ArraySchema || originalSchema instanceof StringSchema) {
                    return asArraySchema(typeSchema,originalSchema);
                }
                if (originalSchema instanceof ObjectSchema) {
                    ((ObjectSchema) originalSchema).putProperty(typeInfo.getPropertyName(), typeSchema);
                    return originalSchema;
                }
                break; //Unsupported schema type

            case WRAPPER_ARRAY:
                return asArraySchema(typeSchema,originalSchema);

            case WRAPPER_OBJECT:
                if (typeInfo.getTypeName() == null) {
                    throw new IllegalArgumentException("Requested WRAPPER Object resolution but no typename was found");
                }
                ObjectSchema wrapperObject = SchemaGenerationContext.get().getSchemaProvider().objectSchema();
                wrapperObject.putProperty(typeInfo.getTypeName(), originalSchema);
                return wrapperObject;
            case EXTERNAL_PROPERTY:
            case EXISTING_PROPERTY:
            default:
                //not implemented.

        }
        return originalSchema;
    }

    public TypeInfo extractTypeInformation(JavaType originalType){
        if (this.provider.getConfig() == null) {
            return TypeInfo.NOT_AVAILABLE;
        }
        BeanDescription beanDescription = this.provider.getConfig().introspectClassAnnotations(originalType);
        if (beanDescription == null) {
            return TypeInfo.NOT_AVAILABLE;
        }

        TypeSerializer typeSerializer = null;
        try {
            typeSerializer = this.provider.getSerializerFactory().createTypeSerializer(this.provider.getConfig(), originalType);
        } catch (JsonMappingException e) {
            //Can't get serializer, just return...
            return TypeInfo.NOT_AVAILABLE;
        }

        if (typeSerializer == null) {
            return TypeInfo.NOT_AVAILABLE;
        }
        if (typeSerializer.getTypeIdResolver() == null) {
            return TypeInfo.NOT_AVAILABLE;
        }
        String typeName = typeSerializer.getTypeIdResolver().idFromValueAndType(null, originalType.getRawClass());
        if(typeName ==null){
            return TypeInfo.NOT_AVAILABLE;
        }
        if(originalType.getRawClass() == Object.class){
            typeName=null;
        }

        return new TypeInfo(typeName,typeSerializer.getTypeInclusion(),typeSerializer.getPropertyName());



    }
    private static JsonSchema asArraySchema(JsonSchema typeSchema,JsonSchema originalSchema){
        ArraySchema arraySchema = SchemaGenerationContext.get().getSchemaProvider().arraySchema();
        arraySchema.setAdditionalItems(new ArraySchema.NoAdditionalItems());
        arraySchema.setItems(new ArraySchema.ArrayItems(new JsonSchema[]{typeSchema, originalSchema}));
        return arraySchema;
    }
}
