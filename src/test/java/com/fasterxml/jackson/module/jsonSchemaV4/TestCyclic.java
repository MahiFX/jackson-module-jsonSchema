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

    private final ObjectMapper MAPPER = new ObjectMapper();

    private final JsonSchemaGenerator GENERATOR = new JsonSchemaGenerator.Builder().withObjectMapper(MAPPER).build();

    // [Issue#4]
    public void testSimpleCyclic() throws Exception {
        JsonSchema schema = GENERATOR.generateSchema(Loop.class);

        String json = MAPPER.writeValueAsString(schema);
        String EXP = "{\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:Loop\",\"type\":\"object\",\"properties\":{\"next\":{\"$ref\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:Loop\"},\"name\":{\"type\":\"string\"}}}";

        assertEquals(aposToQuotes(EXP), json);
    }

    public void testListCyclic() throws Exception {
        JsonSchema schema = GENERATOR.generateSchema(ListLoop.class);

        String json = MAPPER.writeValueAsString(schema);
        String EXP = "{\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:ListLoop\",\"type\":\"object\",\"properties\":{\"list\":{\"type\":\"array\",\"items\":{\"$ref\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:ListLoop\"}}}}";

        assertEquals(aposToQuotes(EXP), json);
    }

    public void testMapCyclic() throws Exception {
        JsonSchema schema = GENERATOR.generateSchema(MapLoop.class);

        String json = MAPPER.writeValueAsString(schema);
        String EXP = "{\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:MapLoop\",\"type\":\"object\",\"properties\":{\"map\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:MapLoop\"}}}}";

        assertEquals(aposToQuotes(EXP), json);
    }

    public void testInnerOuterCyclic() throws Exception {
        JsonSchema schema = GENERATOR.generateSchema(OuterLoop.class);

        String json = MAPPER.writeValueAsString(schema);
        String EXP = "{\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:OuterLoop\",\"type\":\"object\",\"properties\":{\"inner\":{\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:InnerLoop\",\"type\":\"object\",\"properties\":{\"outer\":{\"$ref\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TestCyclic:OuterLoop\"}}}}}";

        assertEquals(aposToQuotes(EXP), json);
    }
}
