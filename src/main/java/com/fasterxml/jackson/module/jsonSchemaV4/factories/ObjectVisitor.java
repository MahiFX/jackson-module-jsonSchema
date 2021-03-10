package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

public class ObjectVisitor extends JsonObjectFormatVisitor.Base
        implements JsonSchemaProducer {
    protected final ObjectSchema schema;


    public ObjectVisitor(ObjectSchema schema) {
        this.schema = schema;
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

        JavaType propType = prop.getType();
        JsonSerializer<Object> handler = getSer(prop);
        return propertySchema(handler, propType);


    }

    protected JsonSchema propertySchema(JsonFormatVisitable handler, JavaType propType)
            throws JsonMappingException {
        // check if we've seen this argument's sub-schema already and return a reference-schema if we have
        SchemaGenerationContext context = SchemaGenerationContext.get();
        String definitionKey = context.getDefinitionKeyForType(propType);
        if (context.isVisited(propType) && SchemaGenerationContext.isNotJvmType(propType)) {
            ReferenceSchema referenceSchemaForVisitedType = context.getReferenceSchemaForVisitedType(propType);
            if (context.getReferenceCount(propType) == 1 && (schema.getDefinitions() == null || !schema.getDefinitions().containsKey(definitionKey))) {
                context.createDefinitionForNonPolymorphic(propType, schema);
            }
            return referenceSchemaForVisitedType;
        }

        SchemaFactoryWrapper visitor = SchemaGenerationContext.get().getNewSchemaFactoryWrapper();

        boolean wasFirst = !context.isVisited(propType);

        if (handler != null) {
            handler.acceptJsonFormatVisitor(visitor, propType);
        }

        JsonSchema jsonSchema = visitor.finalSchema();
        if (SchemaGenerationContext.isNotJvmType(propType)) {
            if (!(jsonSchema instanceof ReferenceSchema)) {
                context.setSchemaForNonPolymorphicType(propType, jsonSchema);
                context.setFormatTypeForVisitedType(propType, jsonSchema.getType());
            }
            if (wasFirst && context.getReferenceCount(propType) > 0 && (schema.getDefinitions() == null || !schema.getDefinitions().containsKey(definitionKey))) {
                context.createDefinitionForNonPolymorphic(propType, schema);
                jsonSchema = context.getReferenceSchemaForVisitedType(propType);
            }
        }

        return jsonSchema;
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
