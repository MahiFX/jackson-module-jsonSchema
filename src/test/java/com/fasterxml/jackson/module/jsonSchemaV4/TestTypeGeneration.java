package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;

public class TestTypeGeneration extends SchemaTestBase {
    static class Issue14Bean {
        public Date date;
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    final ObjectMapper MAPPER = objectMapper();

    // [Issue#14]: multiple type attributes
    public void testCorrectType() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(MAPPER);
        JsonSchema jsonSchema = generator.generateSchema(Issue14Bean.class);
        String json = MAPPER.writeValueAsString(jsonSchema).replace('"', '\'');
        final String EXP = "{'type':'object'," +
                "'id':'urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestTypeGeneration:Issue14Bean'," +
                "'properties':{'date':{'type':'integer','format':'UTC_MILLISEC'}}}";
        assertEquals(EXP, json);
    }

}
