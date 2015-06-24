package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectVisitor implements JsonSchemaProducer {

    private PolymorphicObjectSchema schema;

    private VisitorContext visitorContext;

    private JavaType originalType;

    protected SerializerProvider provider;
    private ObjectMapper originalMapper;

    public PolymorphicObjectVisitor(SerializerProvider provider, PolymorphicObjectSchema schema, JavaType type) {
        this.schema = schema;
        this.originalType = type;
        this.provider = provider;
    }

    public void setProvider(SerializerProvider provider) {
        this.provider = provider;
    }

    public SerializerProvider getProvider() {
        return provider;
    }

    public void setVisitorContext(VisitorContext visitorContext) {
        this.visitorContext = visitorContext;
    }

    public VisitorContext getVisitorContext() {
        return visitorContext;
    }

    @Override
    public JsonSchema getSchema() {
        return null;
    }

    public void visitPolymorphicObject(JavaType type) {
        PolymorphicHandlingUtil handlingUtil = new PolymorphicHandlingUtil(visitorContext, provider, originalMapper, type);
        if (handlingUtil.isPolymorphic()) {
            PolymorphicHandlingUtil.PolymorphiSchemaDefinition schemaDefs = handlingUtil.extractPolymophicTypes();
            schema.setAnyOf(schemaDefs.getReferences());
            if (schema.getDefinitions() == null) {
                schema.setDefinitions(new HashMap<String, JsonSchema>());
            }
            schema.getDefinitions().putAll(schemaDefs.getDefinitions());
            Set<JsonFormatTypes> types = new HashSet<JsonFormatTypes>();
            for(ReferenceSchema schema : schemaDefs.getReferences()){
                if(schema.getType().isSingleJSONType()){
                    types.add(JsonFormatTypes.forValue(schema.getType().asSingleJsonType().getFormatType()));
                }
                else if(schema.getType().isArrayJSONType()){
                    types.addAll(Arrays.asList(schema.getType().asArrayJsonType().getFormatTypes()));
                }
            }
            schema.setTypes(types.toArray(new JsonFormatTypes[0]));

        }

    }

    public void setOriginalMapper(ObjectMapper originalMapper) {
        this.originalMapper = originalMapper;
    }
}
