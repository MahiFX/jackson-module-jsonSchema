package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;

/**
 * Exists to supply {@link SchemaFactoryWrapper} or its subclasses
 * to nested schema factories.
 *
 * @author jphelan
 */
public class WrapperFactory {

    public SchemaFactoryWrapper getWrapper(SerializerProvider provider) {
        SchemaFactoryWrapper schemaFactoryWrapper = new SchemaFactoryWrapper();
        schemaFactoryWrapper.setProvider(provider);
        return schemaFactoryWrapper;
    }

}
