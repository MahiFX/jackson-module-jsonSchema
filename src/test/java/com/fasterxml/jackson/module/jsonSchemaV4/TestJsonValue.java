package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;

import java.util.Collection;

public class TestJsonValue extends SchemaTestBase {
    static class ContainerWithAsValue {
        private Leaf value;

        @JsonValue
        public Leaf getValue() {
            return value;
        }
    }

    static class Leaf {
        public int value;
    }

    static class Issue34Bean {
        @JsonValue
        public Collection<String> getNames() {
            return null;
        }
    }

    /*
    /**********************************************************
    /* Unit tests, success
    /**********************************************************
     */

    public void testJsonValueAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(mapper.constructType(Leaf.class), visitor);
        JsonSchema schemaExp = visitor.finalSchema();
        assertNotNull(schemaExp);

        visitor = new SchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(mapper.constructType(ContainerWithAsValue.class), visitor);
        JsonSchema schemaAct = visitor.finalSchema();
        assertNotNull(schemaAct);

        // these are minimal checks:
        assertEquals(schemaExp.getType(), schemaAct.getType());
        assertEquals(schemaExp, schemaAct);

        // but let's require bit fuller match:

        // construction from bean results in an 'id' being set, whereas from @AsValue it doesn't.
        schemaExp.setId(null);

        String expStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaExp);
        String actStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaAct);

        assertEquals(expStr, actStr);
    }

    // For [Issue#34]
    public void testJsonValueForCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(mapper.constructType(Issue34Bean.class), visitor);
        JsonSchema schema = visitor.finalSchema();
        assertNotNull(schema);

        String schemaStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        assertNotNull(schemaStr);
    }
}
