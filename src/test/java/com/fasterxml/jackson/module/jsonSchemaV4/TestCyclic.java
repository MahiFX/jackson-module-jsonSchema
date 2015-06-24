package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class TestCyclic extends SchemaTestBase {
    // [Issue#4]
    public class Loop {
        public String name;
        public Loop next;
    }

    public class ListLoop {
        public List<ListLoop> list;
    }

    public class MapLoop {
        public Map<String, MapLoop> map;
    }

    public class OuterLoop {
        public InnerLoop inner;
    }

    public class InnerLoop {
        public OuterLoop outer;
    }

    private final ObjectMapper MAPPER = objectMapper();

    // [Issue#4]
    public void testSimpleCyclic() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(MAPPER);
        JsonSchema schema = generator.generateSchema(Loop.class);

        String json = MAPPER.writeValueAsString(schema);
        String EXP = "{\"type\":\"object\"," +
                "\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:Loop\"," +
                "\"properties\":{\"next\":{\"type\":\"object\"," +
                "\"$ref\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:Loop\"}" +
                ",\"name\":{\"type\":\"string\"}}}";

        assertEquals(aposToQuotes(EXP), json);
    }

    public void testListCyclic() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(MAPPER);
        JsonSchema schema = generator.generateSchema(ListLoop.class);

        String json = MAPPER.writeValueAsString(schema);
        String EXP = "{\"type\":\"object\"," +
                "\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:ListLoop\"," +
                "\"properties\":{\"list\":{\"type\":\"array\",\"items\":{\"$ref\":\"" +
                "urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:ListLoop\"}}}}";

        assertEquals(aposToQuotes(EXP), json);
    }

    public void testMapCyclic() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(MAPPER);
        JsonSchema schema = generator.generateSchema(MapLoop.class);

        String json = MAPPER.writeValueAsString(schema);
        String EXP = "{\"type\":\"object\"," +
                "\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:MapLoop\"," +
                "\"properties\":{\"map\":{\"type\":\"object\",\"additionalProperties\":{" +
                "\"$ref\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:MapLoop\"}}}}";

        assertEquals(aposToQuotes(EXP), json);
    }

    public void testInnerOuterCyclic() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator(MAPPER);
        JsonSchema schema = generator.generateSchema(OuterLoop.class);

        String json = MAPPER.writeValueAsString(schema);
        String EXP = "{\"type\":\"object\"," +
                "\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:OuterLoop\"," +
                "\"properties\":{\"inner\":{\"type\":\"object\"," +
                "\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:InnerLoop\"," +
                "\"properties\":{\"outer\":{\"type\":\"object\"," +
                "\"$ref\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:OuterLoop\"}}}}}";

        assertEquals(aposToQuotes(EXP), json);
    }
}