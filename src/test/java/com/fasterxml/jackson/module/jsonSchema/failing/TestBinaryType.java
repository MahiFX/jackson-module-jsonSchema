package com.fasterxml.jackson.module.jsonSchema.failing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.SchemaTestBase;

public class TestBinaryType extends SchemaTestBase {
    private final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Test simple generation for simple/primitive numeric types
     */
    public void testBinaryType() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(MAPPER);
        JsonSchema schema;

        schema = generator.generateSchema(byte[].class);

        // Should be either an array of bytes, or, String with 'format' of "base64"
        String json = MAPPER.writeValueAsString(schema);

        if (!json.equals(aposToQuotes("{'type':'array','items':{'type':'integer'}}"))) {
            String pretty = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
            fail("Should get 'array of integer' or 'String as Base64', instead got: " + pretty);
        }
    }
}
