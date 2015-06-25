package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

import java.util.HashMap;


public class ObjectVisitor extends JsonObjectFormatVisitor.Base
        implements JsonSchemaProducer {
    protected final ObjectSchema schema;
    private final JavaType originalType;


    public ObjectVisitor(ObjectSchema schema,JavaType originalType) {
        this.schema = schema;
        this.originalType = originalType;
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
    /* JsonObjectFormatVisitor impl
    /*********************************************************************
     */

    @Override
    public void optionalProperty(BeanProperty prop) throws JsonMappingException {
        schema.putOptionalProperty(prop, propertySchema(prop));
    }

    @Override
    public void optionalProperty(String name, JsonFormatVisitable handler, JavaType propertyTypeHint)
            throws JsonMappingException {
        schema.putOptionalProperty(name, propertySchema(handler, propertyTypeHint));
    }

    @Override
    public void property(BeanProperty prop) throws JsonMappingException {
        schema.putProperty(prop, propertySchema(prop));
    }

    @Override
    public void property(String name, JsonFormatVisitable handler, JavaType propertyTypeHint)
            throws JsonMappingException {
        schema.putProperty(name, propertySchema(handler, propertyTypeHint));
    }

    protected JsonSchema propertySchema(BeanProperty prop)
            throws JsonMappingException {
        if (prop == null) {
            throw new IllegalArgumentException("Null property");
        }

        // check if we've seen this argument's sub-schema already and return a reference-schema if we have
        String seenSchemaUri = SchemaGenerationContext.get().getSeenSchemaUri(prop.getType());
        if (seenSchemaUri != null) {
            return new ReferenceSchema(seenSchemaUri,SchemaGenerationContext.get().getJsonTypeForVisitedSchema(prop.getType()));
        }

        SchemaFactoryWrapper visitor = SchemaGenerationContext.get().getNewSchemaFactoryWrapper(getProvider());
        JsonSerializer<Object> ser = getSer(prop);
        if (ser != null) {
            JavaType type = prop.getType();
            if (type == null) {
                throw new IllegalStateException("Missing type for property '" + prop.getName() + "'");
            }
            ser.acceptJsonFormatVisitor(visitor, type);

        }
        return visitor.finalSchema();
    }

    protected JsonSchema propertySchema(JsonFormatVisitable handler, JavaType propertyTypeHint)
            throws JsonMappingException {
        // check if we've seen this argument's sub-schema already and return a reference-schema if we have
        String seenSchemaUri = SchemaGenerationContext.get().getSeenSchemaUri(propertyTypeHint);
        if (seenSchemaUri != null) {
            return new ReferenceSchema(seenSchemaUri,SchemaGenerationContext.get().getJsonTypeForVisitedSchema(propertyTypeHint));
        }

        // do we need this here?(wouldn't the schema visiotr create a polymorphic object for us anyway?
        PolymorphicHandlingUtil polymorphicHandlingUtil = new PolymorphicHandlingUtil(propertyTypeHint,getProvider());
        if (polymorphicHandlingUtil.isPolymorphic()) {
            PolymorphicHandlingUtil.PolymorphiSchemaDefinition polymorphiSchemaDefinition = polymorphicHandlingUtil.extractPolymophicTypes();
            if (schema.getDefinitions() == null) {
                schema.setDefinitions(new HashMap<String, JsonSchema>());
            }
            schema.getDefinitions().putAll(polymorphiSchemaDefinition.getDefinitions());
            return new AnyOfSchema(polymorphiSchemaDefinition.getReferences());
        } else {
            SchemaFactoryWrapper visitor = SchemaGenerationContext.get().getNewSchemaFactoryWrapper(getProvider());
            handler.acceptJsonFormatVisitor(visitor, propertyTypeHint);
            return visitor.finalSchema();
        }
    }

    protected JsonSerializer<Object> getSer(BeanProperty prop)
            throws JsonMappingException {
        JsonSerializer<Object> ser = null;
        // 26-Jul-2013, tatu: This is ugly, should NOT require cast...
        if (prop instanceof BeanPropertyWriter) {
            ser = ((BeanPropertyWriter) prop).getSerializer();
        }
        if (ser == null) {
            ser = getProvider().findValueSerializer(prop.getType(), prop);
        }
        return ser;
    }

}