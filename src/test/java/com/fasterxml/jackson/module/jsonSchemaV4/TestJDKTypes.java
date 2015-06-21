package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

public class TestJDKTypes extends SchemaTestBase {
    private final ObjectMapper MAPPER = new ObjectMapper();

    private static class TestObj{

        @JsonProperty(required = true)
        private double value;

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    /**
     * Test simple generation for simple/primitive numeric types
     */
    public void testSimpleNumbers() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(MAPPER);
        JsonSchema schema;

        schema = generator.generateSchema(Long.class);
        assertEquals("{\"type\":\"integer\"}", MAPPER.writeValueAsString(schema));

        /* 07-Nov-2014, tatu: Won't work correctly before 2.5, due to various things; will work
         *    with 2.5. Uncomment then.
         */
        /*
        schema = generator.generateSchema(BigInteger.class);
        assertEquals("{\"type\":\"integer\"}", MAPPER.writeValueAsString(schema));
        */

        schema = generator.generateSchema(Double.class);
        assertEquals("{\"type\":\"number\"}", MAPPER.writeValueAsString(schema));

        schema = generator.generateSchema(BigDecimal.class);
        assertEquals("{\"type\":\"number\"}", MAPPER.writeValueAsString(schema));

        schema = generator.generateSchema(TestObj.class);
        assertEquals("{\"type\":\"object\",\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestJDKTypes:TestObj\",\"properties\":{\"value\":{\"type\":\"number\"}},\"required\":[\"value\"]}",MAPPER.writeValueAsString(schema));

        schema = generator.generateSchema(TestObj[].class);
        assertEquals("{\"type\":\"number\"}", MAPPER.writeValueAsString(schema));
    }
}
