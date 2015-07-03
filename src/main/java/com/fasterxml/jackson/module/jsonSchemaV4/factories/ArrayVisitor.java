package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.TypeDecorationUtils;
import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;



public class ArrayVisitor extends JsonArrayFormatVisitor.Base
        implements JsonSchemaProducer {

    private final JavaType originalType;

    protected ArraySchema schema;



    public ArrayVisitor(ArraySchema schema,JavaType originalType) {
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
        return new TypeDecorationUtils(getProvider()).decorateWithTypeInformation(schema, null);
    }

    /*
    /*********************************************************************
    /* JsonArrayFormatVisitor
    /*********************************************************************
     */

    /*
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

*/
    @Override
    public void itemsFormat(JsonFormatVisitable handler, JavaType contentType) throws JsonMappingException {
        // An array of object matches any values, thus we leave the schema empty.
        if (contentType.getRawClass() != Object.class) {
            PolymorphicHandlingUtil polyMorphicHandlingUtil = new PolymorphicHandlingUtil(contentType,getProvider());
            if (polyMorphicHandlingUtil.isPolymorphic()) {
                PolymorphicHandlingUtil.PolymorphiSchemaDefinition polymorphicDefinition = polyMorphicHandlingUtil.extractPolymophicTypes();
                schema.setItemsSchema(new AnyOfSchema(polymorphicDefinition.getReferences()));
                schema.setDefinitions(polymorphicDefinition.getDefinitions());
            } else {
                String seenSchemaUri = SchemaGenerationContext.get().getSeenSchemaUri(contentType);
                if (seenSchemaUri != null) {
                    schema.setItemsSchema(new ReferenceSchema(seenSchemaUri,SchemaGenerationContext.get().getJsonTypeForVisitedSchema(contentType)));
                    return;
                }
                SchemaFactoryWrapper visitor = SchemaGenerationContext.get().getNewSchemaFactoryWrapper(getProvider());
                handler.acceptJsonFormatVisitor(visitor, contentType);
                schema.setItemsSchema(visitor.finalSchema());
            }
        }
    }

    @Override
    public void itemsFormat(JsonFormatTypes format) throws JsonMappingException {
        schema.setItemsSchema(JsonSchema.minimalForFormat(format));
    }

}
