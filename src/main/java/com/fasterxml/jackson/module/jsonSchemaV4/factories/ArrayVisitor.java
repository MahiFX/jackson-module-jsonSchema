package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayVisitor extends JsonArrayFormatVisitor.Base
        implements JsonSchemaProducer, Visitor {
    protected final ArraySchema schema;

    protected SerializerProvider provider;

    private WrapperFactory wrapperFactory;

    private VisitorContext visitorContext;

    public ArrayVisitor(SerializerProvider provider, ArraySchema schema) {
        this(provider, schema, new WrapperFactory());
    }

    public ArrayVisitor(SerializerProvider provider, ArraySchema schema, WrapperFactory wrapperFactory) {
        this.provider = provider;
        this.schema = schema;
        this.wrapperFactory = wrapperFactory;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
    /*********************************************************************
     */

    @Override
    public JsonSchema getSchema() {
        return schema;
    }

    /*
    /*********************************************************************
    /* JsonArrayFormatVisitor
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

    public WrapperFactory getWrapperFactory() {
        return wrapperFactory;
    }

    public void setWrapperFactory(WrapperFactory wrapperFactory) {
        this.wrapperFactory = wrapperFactory;
    }

    @Override
    public void itemsFormat(JsonFormatVisitable handler, JavaType contentType) throws JsonMappingException {
        // An array of object matches any values, thus we leave the schema empty.
        if (contentType.getRawClass() != Object.class) {
            if (isPolymorphic(contentType)) {
                Class<?>[] polymorphicTypes = extractPolymorphicTypes(contentType.getRawClass()).toArray(new Class<?>[0]);
                ReferenceSchema[] references = new ReferenceSchema[polymorphicTypes.length];
                JsonSchema[] subSchemas = new JsonSchema[polymorphicTypes.length];
                Map<String, JsonSchema> definitions = new HashMap<String, JsonSchema>();

                for (int i = 0; i < polymorphicTypes.length; ++i) {
                    references[i] = new ReferenceSchema(getDefinitionReference(polymorphicTypes[i]));
                    subSchemas[i] = schema(polymorphicTypes[i]);
                    definitions.put(getJsonTypeName(polymorphicTypes[i]), subSchemas[i]);
                }

                schema.setItemsSchema(new AnyOfSchema(references));
                //schema.setAllOf(new HashSet<Object>(definitions.values()));
                schema.setDefinitions(definitions);
                ;

            } else {
                // check if we've seen this sub-schema already and return a reference-schema if we have
                if (visitorContext != null) {
                    String seenSchemaUri = visitorContext.getSeenSchemaUri(contentType);
                    if (seenSchemaUri != null) {
                        schema.setItemsSchema(new ReferenceSchema(seenSchemaUri));
                        return;
                    }
                }

                SchemaFactoryWrapper visitor = wrapperFactory.getWrapper(getProvider(), visitorContext);
                handler.acceptJsonFormatVisitor(visitor, contentType);
                schema.setItemsSchema(visitor.finalSchema());
            }
        }
    }


    @Override
    public void itemsFormat(JsonFormatTypes format) throws JsonMappingException {
        schema.setItemsSchema(JsonSchema.minimalForFormat(format));
    }

    @Override
    public Visitor setVisitorContext(VisitorContext rvc) {
        visitorContext = rvc;
        return this;
    }

    protected String getDefinitionReference(Class modelClass) {
        return "#/definitions/" + getJsonTypeName(modelClass);
    }

    protected String getJsonTypeName(Class modelClass) {
        return ((JsonTypeName) modelClass.getAnnotation(JsonTypeName.class)).value();
    }

    private List<Class<?>> extractPolymorphicTypes(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(JsonSubTypes.class)) {
            return Collections.emptyList();
        }

        JsonSubTypes subTypes = clazz.getAnnotation(JsonSubTypes.class);
        List<Class<?>> javaSubTypes = new ArrayList<Class<?>>();
        for (int i = 0; i < subTypes.value().length; ++i) {
            javaSubTypes.add(subTypes.value()[i].value());
            javaSubTypes.addAll(extractPolymorphicTypes(subTypes.value()[i].value()));

        }
        return javaSubTypes;
    }

    private boolean isPolymorphic(JavaType type) {
        return type.getRawClass().isAnnotationPresent(JsonSubTypes.class);
    }

    private static JsonSchema schema(Type t) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();

            mapper.acceptJsonFormatVisitor(mapper.constructType(t), visitor);
            return visitor.finalSchema();
        } catch (JsonMappingException e) {
            //TODO throw and sort out exception
            return null;
        }
    }
}
