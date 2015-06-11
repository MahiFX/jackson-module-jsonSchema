package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.SerializerProvider;

public abstract class StructuredTypeVisitor implements JsonSchemaProducer {
    protected SerializerProvider provider;

    protected StructuredTypeVisitor(SerializerProvider provider) {
        this.provider = provider;
    }

    // // // Partial implementation for visitors; handling of SerializerProvider

    public SerializerProvider getProvider() {
        return provider;
    }

    public void setProvider(SerializerProvider p) {
        provider = p;
    }

}

