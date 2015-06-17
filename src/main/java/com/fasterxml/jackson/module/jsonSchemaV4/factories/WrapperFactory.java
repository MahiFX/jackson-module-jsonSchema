package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Exists to supply {@link SchemaFactoryWrapper} or its subclasses
 * to nested schema factories.
 *
 * @author jphelan
 */
public class WrapperFactory {
    public SchemaFactoryWrapper getWrapper(ObjectMapper mapper, SerializerProvider provider) {
        return new SchemaFactoryWrapper(mapper, provider);
    }

    public SchemaFactoryWrapper getWrapper(ObjectMapper mapper, SerializerProvider provider, VisitorContext rvc) {
        SchemaFactoryWrapper wrapper = new SchemaFactoryWrapper(mapper, provider);
        wrapper.setVisitorContext(rvc);
        return wrapper;
    }
}