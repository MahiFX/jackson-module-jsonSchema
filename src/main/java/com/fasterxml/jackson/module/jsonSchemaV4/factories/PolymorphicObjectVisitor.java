package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
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

    private JavaType originalType;

    private SerializerProvider provider;

    public PolymorphicObjectVisitor(PolymorphicObjectSchema schema, JavaType type,SerializerProvider provider) {
        this.schema = schema;
        this.originalType = type;
        this.provider=provider;
    }

    @Override
    public JsonSchema getSchema() {
        return null;
    }

    public void visitPolymorphicObject(JavaType type) {

        SchemaGenerationContext context = SchemaGenerationContext.get();
        String seenSchemaUri = context.getSeenSchemaUri(type);
        if (seenSchemaUri != null) {
            throw new IllegalStateException("JavaType: " + type.getRawClass().getSimpleName() + "has already been visited. A single class can be handled polymorphicly only once");
        }


        PolymorphicHandlingUtil handlingUtil = new PolymorphicHandlingUtil(type,this.provider);
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
            JsonFormatTypes[] formatTypes = types.toArray(new JsonFormatTypes[0]);
            schema.setTypes(formatTypes);
            String id=context.addSeenSchemaUri(type,schema.getType());
            schema.setId(id);
        }


    }
}
