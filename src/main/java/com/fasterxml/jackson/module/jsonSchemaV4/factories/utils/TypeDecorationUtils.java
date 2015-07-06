package com.fasterxml.jackson.module.jsonSchemaV4.factories.utils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class TypeDecorationUtils {

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

        private final Set<String> typeNames;

        private final JsonTypeInfo.As typeInclusion;

        private final String propertyName;

        public TypeInfo(Set<String> typeNames, JsonTypeInfo.As typeInclusion, String propertyName) {
            this.typeNames = typeNames;
            this.typeInclusion = typeInclusion;
            this.propertyName = propertyName;
        }

        public Set<String> getTypeNames() {
            return typeNames;
        }

        public JsonTypeInfo.As getTypeInclusion() {
            return typeInclusion;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }

    private SerializerProvider provider;

    public TypeDecorationUtils(SerializerProvider provider) {
        this.provider = provider;
    }


    public JsonSchema decorateWithTypeInformation(JsonSchema originalSchema, JavaType originalType) {
        TypeInfo typeInfo = extractTypeInformation(originalType);
        if(typeInfo == TypeInfo.NOT_AVAILABLE){
            return originalSchema;
        }

        StringSchema typeSchema = SchemaGenerationContext.get().getSchemaProvider().stringSchema();

        if (typeInfo.getTypeNames()!=null) {
            typeSchema.setEnums(typeInfo.getTypeNames());
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
                if (typeInfo.getTypeNames() == null) {
                    throw new IllegalArgumentException("Requested WRAPPER Object resolution but no typename was found");
                }
                Map<String,JsonSchema> types = new HashMap<String,JsonSchema>();
                for(String type : typeInfo.getTypeNames()){
                    ObjectSchema wrapperObject = SchemaGenerationContext.get().getSchemaProvider().objectSchema();
                    wrapperObject.putProperty(type, originalSchema);
                    types.put(type,wrapperObject);
                }
                if(types.size()==1){
                    return types.values().iterator().next();
                }
                else{
                    PolymorphicObjectSchema polymorphicObjectSchema= PolymorphicSchemaUtil.constructPolymorphicSchema(types, PolymorphicObjectSchema.Type.ANY_OF);
                    //TODO figur out id if needed
                    //polymorphicObjectSchema.setId();
                    return polymorphicObjectSchema;
                }

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

        TypeSerializer typeSerializer = null;
        try {
            typeSerializer = this.provider.getSerializerFactory().createTypeSerializer(provider.getConfig(),originalType);
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
        Set<String> typeNames = new HashSet<String>();
        if(originalType.isContainerType()) {
            for (PolymorphicSchemaUtil.NamedJavaType namedJavaType : PolymorphicSchemaUtil.extractSubTypes(originalType, provider.getConfig(), true)) {
                String typeName = typeSerializer.getTypeIdResolver().idFromValueAndType(null, namedJavaType.getRawClass());
                if (typeName != null) {
                    typeNames.add(typeName);
                }
            }
        }
        else{
            String typeName = typeSerializer.getTypeIdResolver().idFromValueAndType(null, originalType.getRawClass());
            if (typeName != null) {
                typeNames.add(typeName);
            }
        }

        if(originalType.getRawClass() == Object.class){
            typeNames= Collections.EMPTY_SET;
        }
        if(typeNames.isEmpty()){
            return TypeInfo.NOT_AVAILABLE;
        }

        return new TypeInfo(typeNames,typeSerializer.getTypeInclusion(),typeSerializer.getPropertyName());



    }
    private static JsonSchema asArraySchema(JsonSchema typeSchema,JsonSchema originalSchema){
        ArraySchema arraySchema = SchemaGenerationContext.get().getSchemaProvider().arraySchema();
        arraySchema.setAdditionalItems(new ArraySchema.NoAdditionalItems());
        arraySchema.setItems(new ArraySchema.ArrayItems(new JsonSchema[]{typeSchema, originalSchema}));
        return arraySchema;
    }
}
