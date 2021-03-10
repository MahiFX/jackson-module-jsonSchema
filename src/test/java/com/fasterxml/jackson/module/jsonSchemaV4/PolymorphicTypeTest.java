package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.*;

/**
 * Created by zoliszel on 09/06/2015.
 */

public class PolymorphicTypeTest {
    public static final HashSet<String> JSON_SUB_TYPES = Sets.newHashSet("Person", "Company", "BigCompany");
    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
        mapper.addMixIn(Intf[].class, IntfArrayMixin.class);
        mapper.addMixIn(Intf2[].class, Intf2ArrayMixin.class);
    }

    @Test
    public void polymorphicSchemaGenerationArray() {
        Type type = JSONSubTypeBaseClass[].class;
        JsonSchema schema = schema(type, mapper);
        String json = toJson(schema, schema.getClass(), mapper);
        System.out.println(json);
        verifyDefinitions(schema);
        Assert.assertTrue("Expected array schema", schema.isArraySchema());
        Assert.assertNotNull("Array items should have a reference set", (schema.asArraySchema().getItems()).asSingleItems().getSchema().get$ref());
    }

    @Test
    public void arrayOfInterfaceWithSingleImpl() {
        JsonSchema schema = schema(Intf[].class, mapper);

        String json = toJson(schema, schema.getClass(), mapper);
        System.out.println(json);
        ArraySchema arraySchema = schema.asArraySchema();
        ArraySchema.Items items = arraySchema.getItems();
        JsonSchema[] itemTypes = items.asArrayItems().getJsonSchemas();
        itemTypes[0].asStringSchema().getEnums().contains("Intf[]"); // First element in array is type
        ArraySchema.Items elementType = itemTypes[1].asArraySchema().getItems();
        String $ref = elementType.asSingleItems().getSchema().get$ref();
        String itemSchemaRef = $ref.substring($ref.lastIndexOf("/") + 1);
        PolymorphicObjectSchema itemSchema = schema.getDefinitions().get(itemSchemaRef).asPolymorphicObjectSchema();
        verifyAnyOfContent(itemSchema.getAnyOf(), new HashSet<>(Arrays.asList("Impl")));

    }


    @Test
    public void arrayOfInterfaceWithMultipleImpl() {
        JsonSchema schema = schema(Intf2[].class, mapper);

        String json = toJson(schema, schema.getClass(), mapper);
        System.out.println(json);
        ArraySchema arraySchema = schema.asArraySchema();
        ArraySchema.Items items = arraySchema.getItems();
        JsonSchema[] itemTypes = items.asArrayItems().getJsonSchemas();
        itemTypes[0].asStringSchema().getEnums().contains("Intf2[]"); // First element in array is type
        ArraySchema.Items elementType = itemTypes[1].asArraySchema().getItems();
        String $ref = elementType.asSingleItems().getSchema().get$ref();
        String itemSchemaRef = $ref.substring($ref.lastIndexOf("/") + 1);
        PolymorphicObjectSchema itemSchema = schema.getDefinitions().get(itemSchemaRef).asPolymorphicObjectSchema();
        verifyAnyOfContent(itemSchema.getAnyOf(), new HashSet<>(Arrays.asList("Impl", "Impl2")));

    }


    @Test
    public void polymorphicSchemaGenerationObject() {
        Type type = JSONSubTypeBaseClass.class;
        JsonSchema schema = schema(type, mapper);
        String json = toJson(schema, schema.getClass(), mapper);
        System.out.println(json);
        verifyDefinitions(schema);
        Assert.assertTrue("Expected polymorphicObject", schema.isPolymorhpicObjectSchema());
//        Assert.assertNotNull("Polymorphic object should have an ID", schema.getId());
    }

    @Test
    public void testSchemaGenerationIsIdempotent() throws JsonProcessingException {
        Type type = JSONSubTypeBaseClass.class;
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        //noinspection UnusedAssignment
        JsonSchema schema = schemaGenerator.generateSchema(type);
        schema = schemaGenerator.generateSchema(type);
        String json = schemaGenerator.schemaAsString(schema);
        System.out.println(json);
        verifyDefinitions(schema);
        Assert.assertTrue("Expected polymorphicObject", schema instanceof PolymorphicObjectSchema);
    }

    public static void verifyAnyOfContent(ReferenceSchema[] anyOf, Set<String> references) {
        for (String ref : references) {
            Assert.assertTrue("Any OF Schema Should Contain" + ref + " reference", containsReference(anyOf, ref));
        }
    }

    public static void containsDefinitions(JsonSchema schema, Set<String> definitions) {
        Assert.assertNotNull("definitions should not be null", schema.getDefinitions());
        Assert.assertEquals("there should be " + definitions.size() + " sub schema", definitions.size(), schema.getDefinitions().size());
        for (String def : definitions) {
            Assert.assertTrue("should contain " + def + " schema", schema.getDefinitions().containsKey(def));
        }
    }

    public static void verifyDefinitions(JsonSchema schema) {
        containsDefinitions(schema, Sets.newHashSet("Person", "Company", "BigCompany", "Company_1", JSONSubTypeBaseClass.class.getSimpleName()));
        Map<String, JsonSchema> definitions = schema.getDefinitions();
        JsonSchema companySchema = definitions.get("Company");
        Assert.assertTrue("Company schema should be polymorphic", companySchema.isPolymorhpicObjectSchema());
        PolymorphicObjectSchema companyPolymorphic = companySchema.asPolymorphicObjectSchema();
        verifyAnyOfContent(companyPolymorphic.asPolymorphicObjectSchema().getAnyOf(), Sets.newHashSet("Company_1", "BigCompany"));
        JsonSchema jsonSubTypeSchema = schema.getDefinitions().get(JSONSubTypeBaseClass.class.getSimpleName());
        Assert.assertTrue("JSONSubType schema should be polymorhpic", jsonSubTypeSchema.isPolymorhpicObjectSchema());
        ReferenceSchema[] jsonSubTypeRef = jsonSubTypeSchema.asPolymorphicObjectSchema().getAnyOf();
        verifyAnyOfContent(jsonSubTypeRef, JSON_SUB_TYPES);
    }

    public static boolean containsReference(ReferenceSchema[] refSchemas, String name) {
        if (refSchemas == null) {
            return false;
        }
        for (com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema refSchema : refSchemas) {
            if (("#/definitions/" + name).equals(refSchema.get$ref())) {
                return true;
            }
            if (refSchema.get$ref().contains(name)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void parseTest() {
        JSONSubTypeBaseClass[] media = loadJson(PolymorphicTypeTest.class.getResourceAsStream("/polymorphic.json"), JSONSubTypeBaseClass[].class, mapper);
        Assert.assertEquals("mismatch in array size", 3, media.length);
        Assert.assertTrue("First item is not a person", media[0] instanceof Person);
        Assert.assertTrue("Second item is not a Company", media[1] instanceof CompanyObfuscated);
        Assert.assertTrue("Third item is not a BigCompany", media[2] instanceof BigCompany);
    }


    @JsonTypeName("Number")
    @JsonSubTypes({@JsonSubTypes.Type(Integer.class),
            @JsonSubTypes.Type(Double.class)})
    interface UnionType {

    }

    @JsonTypeName("Double")
    interface DoubleType {

    }

    @JsonTypeName("Integer")
    interface IntegerType {

    }

    @Test
    public void testUnionType() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Number.class, UnionType.class);
        mapper.addMixIn(Double.class, DoubleType.class);
        mapper.addMixIn(Integer.class, IntegerType.class);
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        JsonSchema schema = generator.generateSchema(Number.class);
        containsDefinitions(schema, Sets.newHashSet("Number", "Double", "Integer"));
        JsonSchema number = schema.getDefinitions().get("Number");
        Assert.assertTrue("Number should be polymorphic", number.isPolymorhpicObjectSchema());
        verifyAnyOfContent(number.asPolymorphicObjectSchema().getAnyOf(), Sets.newHashSet("Integer", "Double"));
        Assert.assertTrue("Number should have an array of types", number.getType().isArrayJSONType());
        Assert.assertEquals("Number has wrong set of types", Sets.newHashSet(JsonFormatTypes.INTEGER, JsonFormatTypes.NUMBER), Sets.newHashSet(Arrays.asList(number.getType().asArrayJsonType().getFormatTypes())));
    }
}
