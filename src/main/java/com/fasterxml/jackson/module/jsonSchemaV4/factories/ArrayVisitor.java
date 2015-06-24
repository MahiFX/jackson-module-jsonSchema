package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.VisitorUtils;
import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;



public class ArrayVisitor extends JsonArrayFormatVisitor.Base
        implements JsonSchemaProducer, Visitor {

    private final JavaType originalType;

    protected ArraySchema schema;

    protected SerializerProvider provider;

    private WrapperFactory wrapperFactory;

    private VisitorContext visitorContext;
    private ObjectMapper originalMapper;


    public ArrayVisitor(SerializerProvider provider, ArraySchema schema) {
        this(provider, schema, new WrapperFactory(), null);
    }

    public ArrayVisitor(SerializerProvider provider, ArraySchema schema, WrapperFactory wrapperFactory, JavaType originalType) {
        this.provider = provider;
        this.schema = schema;
        this.wrapperFactory = wrapperFactory;
        this.originalType = originalType;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
    /*********************************************************************
     */

    @Override
    public JsonSchema getSchema() {
        return new VisitorUtils(wrapperFactory.getWrapper(originalMapper,provider), visitorContext, provider).decorateWithTypeInformation(schema, null);
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
            PolymorphicHandlingUtil polyMorphicHandlingUtil = new PolymorphicHandlingUtil(visitorContext, provider, originalMapper, contentType,wrapperFactory);
            if (polyMorphicHandlingUtil.isPolymorphic()) {
                PolymorphicHandlingUtil.PolymorphiSchemaDefinition polymorphicDefinition = polyMorphicHandlingUtil.extractPolymophicTypes();
                schema.setItemsSchema(new AnyOfSchema(polymorphicDefinition.getReferences()));
                schema.setDefinitions(polymorphicDefinition.getDefinitions());
            } else {
                // check if we've seen this sub-schema already and return a reference-schema if we have
                if (visitorContext != null) {
                    String seenSchemaUri = visitorContext.getSeenSchemaUri(contentType);
                    if (seenSchemaUri != null) {
                        schema.setItemsSchema(new ReferenceSchema(seenSchemaUri,visitorContext.getJsonTypeForVisitedSchema(contentType)));
                        return;
                    }
                }
                SchemaFactoryWrapper visitor = wrapperFactory.getWrapper(originalMapper, getProvider(), visitorContext);
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


    public void setOriginalMapper(ObjectMapper originalMapper) {
        this.originalMapper = originalMapper;
    }
}
