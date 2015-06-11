package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

/**
 * Interface for objects that produce {@link JsonSchema} instances;
 * implemented by visitors.
 *
 * @author jphelan
 */
public interface JsonSchemaProducer {
    public JsonSchema getSchema();
}
