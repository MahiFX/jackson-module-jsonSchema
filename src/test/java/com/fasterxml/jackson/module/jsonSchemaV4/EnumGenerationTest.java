package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnumGenerationTest extends SchemaTestBase {
    public enum Enumerated {
        A, B, C;

        // add this; should NOT matter but...
        @Override
        public String toString() {
            return "ToString:" + name();
        }
    }

    public static class LetterBean {

        public Enumerated letter;
    }

    // for [jsonSchema#57]
    public enum EnumViaJsonValue {
        A, B, C;

        @JsonValue
        public String asJson() {
            return name().toLowerCase();
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = new ObjectMapper();
    private final JsonSchemaGenerator SCHEMA_GEN = new JsonSchemaGenerator.Builder().withObjectMapper(MAPPER).build();

    public void testEnumDefault() throws Exception {
        JsonSchema jsonSchema = SCHEMA_GEN.generateSchema(LetterBean.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) MAPPER.convertValue(jsonSchema, Map.class);
        assertNotNull(result);
        assertTrue(jsonSchema.isObjectSchema());
        assertEquals(expectedAsMap(false), result);
    }

    public void testEnumWithToString() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        JsonSchema jsonSchema = schemaGenerator.generateSchema(LetterBean.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) mapper.convertValue(jsonSchema, Map.class);
        assertNotNull(result);
        assertTrue(jsonSchema.isObjectSchema());
        Map<String, Object> exp = expectedAsMap(true);
        if (!exp.equals(result)) {
            String expJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exp);
            String actJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            fail("Different JSON: expected:\n" + expJson + "\ngot:\n" + actJson);
        }
    }

    @SuppressWarnings("serial")
    private Map<String, Object> expectedAsMap(final boolean useToString) {
        return new LinkedHashMap<String, Object>() {
            {
                put("type", "object");
                put("id", "urn:com:fasterxml:jackson:module:jsonSchemaV4:EnumGenerationTest:LetterBean");
                put("properties",
                        new LinkedHashMap<String, Object>() {
                            {
                                put("letter",
                                        new LinkedHashMap<String, Object>() {
                                            {
                                                put("type", "string");
                                                put("enum", new ArrayList<String>() {
                                                    {
                                                        add(useToString ? "ToString:A" : "A");
                                                        add(useToString ? "ToString:B" : "B");
                                                        add(useToString ? "ToString:C" : "C");
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
            }
        };
    }

    // for [jsonSchema#57]
    @SuppressWarnings("unchecked")
    // @Ignore("Requires jackson-databind 2.6.0")
    public void testEnumWithJsonValue() throws Exception {
        JsonSchema schema = SCHEMA_GEN.generateSchema(EnumViaJsonValue.class);

        Map<String, Object> result = (Map<String, Object>) MAPPER.convertValue(schema, Map.class);
        assertEquals("string", result.get("type"));

        Object values = result.get("enum");
        if (values == null) {
            fail("Expected 'enum' entry, not found; schema: " + MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        assertNotNull(values);
        assertTrue(values instanceof List<?>);
        List<?> enumValues = (List<?>) values;
        assertEquals(3, enumValues.size());
        assertEquals("a", enumValues.get(0));
        assertEquals("b", enumValues.get(1));
        assertEquals("c", enumValues.get(2));
    }


    @JsonTypeName("MyEnum_WrapperArray")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_ARRAY)
    enum MyEnum_WrapperArray {
        TEST1, TEST2;
    }

    @JsonTypeName("MyEnum_Property")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    enum MyEnum_Property {
        TEST1, TEST2;
    }

    @JsonTypeName("MyEnum_WrapperObject")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    enum MyEnum_WrapperObject {
        TEST1, TEST2;
    }

    public void testEnumWithTypeInformationWrapperArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        JsonSchema schema = generator.generateSchema(MyEnum_WrapperArray.class);
        Assert.assertTrue("Was expecting array schema", schema.isArraySchema());
        JsonSchema firstItem = schema.asArraySchema().getItems().asArrayItems().getJsonSchemas()[0];
        JsonSchema secondItem = schema.asArraySchema().getItems().asArrayItems().getJsonSchemas()[1];

        Assert.assertTrue("first item should be string schema", firstItem.isStringSchema());
        Assert.assertTrue("second item should be string schema", secondItem.isStringSchema());

        Assert.assertTrue("first item should be restricted to  MyEnum_WrapperArray", firstItem.asStringSchema().getEnums().contains("MyEnum_WrapperArray"));
        Assert.assertTrue("second item should contain TEST1 restriction", secondItem.asStringSchema().getEnums().contains("TEST1"));
        Assert.assertTrue("second item should contain TEST2 restriction", secondItem.asStringSchema().getEnums().contains("TEST2"));

    }

    public void testEnumWithTypeInformationProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();


        // mapper.setDefaultTyping(new ObjectMapper.DefaultTypeResolverBuilder().inclusion(JsonTypeInfo.As.PROPERTY));
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        JsonSchema schema = generator.generateSchema(MyEnum_Property.class);
        Assert.assertTrue("Was expecting array schema", schema.isArraySchema());
        JsonSchema firstItem = schema.asArraySchema().getItems().asArrayItems().getJsonSchemas()[0];
        JsonSchema secondItem = schema.asArraySchema().getItems().asArrayItems().getJsonSchemas()[1];

        Assert.assertTrue("first item should be string schema", firstItem.isStringSchema());
        Assert.assertTrue("second item should be string schema", secondItem.isStringSchema());

        Assert.assertTrue("first item should be restricted to  MyEnum_WrapperArray", firstItem.asStringSchema().getEnums().contains("MyEnum_Property"));
        Assert.assertTrue("second item should contain TEST1 restriction", secondItem.asStringSchema().getEnums().contains("TEST1"));
        Assert.assertTrue("second item should contain TEST2 restriction", secondItem.asStringSchema().getEnums().contains("TEST2"));

    }

    public void testEnumWithTypeInformationWrapperObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // mapper.setDefaultTyping(new ObjectMapper.DefaultTypeResolverBuilder().inclusion(JsonTypeInfo.As.PROPERTY));
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        JsonSchema schema = generator.generateSchema(MyEnum_WrapperObject.class);
        Assert.assertTrue("Was expecting Object schema", schema.isObjectSchema());

        Assert.assertTrue("property name should be MyEnum_WrapperObject", schema.asObjectSchema().getProperties().containsKey("MyEnum_WrapperObject"));
        JsonSchema propertySchema = schema.asObjectSchema().getProperties().get("MyEnum_WrapperObject");

        Assert.assertTrue("property schema should be string schema", propertySchema.isStringSchema());
        Assert.assertTrue("property schema should contain TEST1 restriction", propertySchema.asStringSchema().getEnums().contains("TEST1"));
        Assert.assertTrue("property schema should contain TEST2 restriction", propertySchema.asStringSchema().getEnums().contains("TEST2"));
    }
}
