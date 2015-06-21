package com.fasterxml.jackson.module.jsonSchemaV4.factories.utils;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class VisitorUtils {

    protected final SchemaFactoryWrapper visitor;

    protected final VisitorContext visitorContext;

    protected final SerializerProvider provider;

    public VisitorUtils(ObjectMapper mapper, VisitorContext visitorContext, SerializerProvider provider) {
        this.visitorContext = visitorContext;
        visitor = new SchemaFactoryWrapper(mapper);
        visitor.setVisitorContext(visitorContext);
        this.provider = provider;
    }

    protected JsonSchema schema(Type t, ObjectMapper mapper) {
        /*
        if (visitorContext != null) {
            if (visitorContext != null) {
                String seenSchemaUri = visitorContext.getSeenSchemaUri(mapper.constructType(t));
                if (seenSchemaUri != null) {
                    return new ReferenceSchema(seenSchemaUri);
                }
            }
        }
        */
        try {
            mapper.acceptJsonFormatVisitor(mapper.constructType(t), visitor);
            return visitor.finalSchema();
        } catch (JsonMappingException e) {
            //TODO throw and sort out exception
            return null;
        }
    }


    public JsonSchema decorateWithTypeInformation(JsonSchema originalSchema, JavaType originalType) {
        if (provider.getConfig() == null) {
            return originalSchema;
        }
        BeanDescription beanDescription = provider.getConfig().introspectClassAnnotations(originalType);
        if (beanDescription == null) {
            return originalSchema;
        }
        Collection<NamedType> namedTypes = null;
        if (provider.getConfig().getSubtypeResolver() == null) {
            namedTypes = Collections.emptyList();
        } else {
            namedTypes = provider.getConfig().getSubtypeResolver().collectAndResolveSubtypes(beanDescription.getClassInfo(), provider.getConfig(), provider.getConfig().getAnnotationIntrospector());

        }
        TypeResolverBuilder<?> typer = provider.getConfig().getDefaultTyper(originalType);
        if (typer == null) {
            return originalSchema;
        }

        TypeSerializer typeSerializer = typer.buildTypeSerializer(provider.getConfig(), originalType, namedTypes);

        if (typeSerializer == null) {
            return originalSchema;
        }
        if (typeSerializer.getTypeIdResolver() == null) {
            return originalSchema;
        }
        StringSchema typeSchema = new StringSchema();
        String typeName = typeSerializer.getTypeIdResolver().idFromValueAndType(null, originalType.getRawClass());
        if (typeName != null && typeName.length() > 0) {
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
                    ArraySchema arraySchema = new ArraySchema();
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
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.setAdditionalItems(new ArraySchema.NoAdditionalItems());
                arraySchema.setItems(new ArraySchema.ArrayItems(new JsonSchema[]{typeSchema, originalSchema}));
                break;
            case WRAPPER_OBJECT:
                //TODO: support wrapper objects
            case EXTERNAL_PROPERTY:
            case EXISTING_PROPERTY:
            default:
                //not implemented.

        }
        return originalSchema;
    }

}
