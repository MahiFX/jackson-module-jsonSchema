package com.fasterxml.jackson.module.jsonSchemaV4.factories.utils;

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
        if (this.provider.getConfig() == null) {
            return originalSchema;
        }
        BeanDescription beanDescription = this.provider.getConfig().introspectClassAnnotations(originalType);
        if (beanDescription == null) {
            return originalSchema;
        }
        /*
        Collection<NamedType> namedTypes = null;
        if (provider.getConfig().getSubtypeResolver() == null) {
            namedTypes = Collections.emptyList();
        } else {
            namedTypes = provider.getConfig().getSubtypeResolver().collectAndResolveSubtypes(beanDescription.getClassInfo(), provider.getConfig(), provider.getConfig().getAnnotationIntrospector());

        }
        */

        TypeSerializer typeSerializer = null;
        try {
            typeSerializer = this.provider.getSerializerFactory().createTypeSerializer(this.provider.getConfig(), originalType);
        } catch (JsonMappingException e) {
            //Can't get serializer, just return...
            return originalSchema;
        }
        /*
        TypeResolverBuilder<?> typer = provider.getConfig().getDefaultTyper(originalType);
        if (typer == null) {
            return originalSchema;
        }
        */

        //TypeSerializer typeSerializer = typer.buildTypeSerializer(provider.getConfig(), originalType, namedTypes);

        if (typeSerializer == null) {
            return originalSchema;
        }
        if (typeSerializer.getTypeIdResolver() == null) {
            return originalSchema;
        }
        StringSchema typeSchema = SchemaGenerationContext.get().getSchemaProvider().stringSchema();
        String typeName = typeSerializer.getTypeIdResolver().idFromValueAndType(null, originalType.getRawClass());
        if (typeName != null && typeName.length() > 0 && originalType.getRawClass() != Object.class) {
            Set<String> allowedValues = new HashSet<String>();
            allowedValues.add(typeName);
            typeSchema.setEnums(allowedValues);
        }

        switch (typeSerializer.getTypeInclusion()) {
            case PROPERTY:
                if (originalSchema instanceof PolymorphicObjectSchema) {
                    return originalSchema; //PolymorphicObjects will have type information in it's sub-schemas
                }

                if (originalSchema instanceof ArraySchema) {
                    ArraySchema arraySchema = SchemaGenerationContext.get().getSchemaProvider().arraySchema();
                    arraySchema.setAdditionalItems(new ArraySchema.NoAdditionalItems());
                    arraySchema.setItems(new ArraySchema.ArrayItems(new JsonSchema[]{typeSchema, originalSchema}));
                    return arraySchema;
                }
                if (originalSchema instanceof ObjectSchema) {
                    ((ObjectSchema) originalSchema).putProperty(typeSerializer.getPropertyName(), typeSchema);
                    return originalSchema;
                }
                break; //Unsupported schema type

            case WRAPPER_ARRAY:
                ArraySchema arraySchema = SchemaGenerationContext.get().getSchemaProvider().arraySchema();
                arraySchema.setAdditionalItems(new ArraySchema.NoAdditionalItems());
                arraySchema.setItems(new ArraySchema.ArrayItems(new JsonSchema[]{typeSchema, originalSchema}));
                return arraySchema;

            case WRAPPER_OBJECT:
                if (typeName == null) {
                    throw new IllegalArgumentException("Requested WRAPPER Object resolution but no typename was found");
                }
                ObjectSchema wrapperObject = SchemaGenerationContext.get().getSchemaProvider().objectSchema();
                wrapperObject.putProperty(typeName, originalSchema);
                return wrapperObject;
            case EXTERNAL_PROPERTY:
            case EXISTING_PROPERTY:
            default:
                //not implemented.

        }
        return originalSchema;
    }

}
