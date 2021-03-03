package com.fasterxml.jackson.module.jsonSchema.failing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.TestBase;
import org.junit.Ignore;

import java.util.Map;

// for [module-jsonSchema#89]
public class MapTest extends TestBase {
    static class MapBean {
        private Map<String, Integer> counts;

        public void setCounts(Map<String, Integer> counts) {
            this.counts = counts;
        }

        public Map<String, Integer> getCounts() {
            return counts;
        }
    }

    /*
    /**********************************************************
    /* Tests methods
    /**********************************************************
    */

    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testSimpleMapKeyType89() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(MAPPER);
        JsonSchema schema;

        schema = generator.generateSchema(MapBean.class);
        String json = MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(schema);
//        System.out.println(json);


        assertTrue(json.contains("\"type\" : \"integer\""));
    }
}
