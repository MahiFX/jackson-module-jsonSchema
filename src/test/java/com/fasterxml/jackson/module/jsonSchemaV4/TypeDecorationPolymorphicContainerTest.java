package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.google.common.collect.Sets;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by zoliszel on 02/07/2015.
 */
public class TypeDecorationPolymorphicContainerTest {

    public static final HashSet<String> MAP_TYPES = Sets.newHashSet("HashMap", "Map", "TreeMap", "LinkedHashMap");
    public static final HashSet<String> SET_TYPES = Sets.newHashSet("HashSet", "Set", "LinkedHashSet", "TreeSet");

    //    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.WRAPPER_ARRAY)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = HashMap.class, name = "HashMap"),
            @JsonSubTypes.Type(value = TreeMap.class, name = "TreeMap"),
            @JsonSubTypes.Type(value = LinkedHashMap.class, name = "LinkedHashMap")
    })
    public static class MapMixIn {
    }



//    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.WRAPPER_ARRAY)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = HashSet.class, name = "HashSet"),
            @JsonSubTypes.Type(value = LinkedHashSet.class, name = "LinkedHashSet"),
            @JsonSubTypes.Type(value = TreeSet.class, name = "TreeSet")
    })
    public static class SetMixIn {
    }

//    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.WRAPPER_ARRAY)
    @JsonSubTypes(@JsonSubTypes.Type(value = ArrayList.class, name = "ArrayList"))
    public static class ListMixin {

    }

    private List<JsonSchemaGenerator> createGeneratorsForModes(List<JsonTypeInfo.As> modes){
        List<JsonSchemaGenerator> result = new ArrayList<JsonSchemaGenerator>();
        for(JsonTypeInfo.As mode : modes){
            ObjectMapper mapper = new ObjectMapper();
            mapper.addMixIn(List.class, ListMixin.class);
            mapper.addMixIn(Map.class, MapMixIn.class);
            mapper.addMixIn(Set.class, SetMixIn.class);

            TypeResolverBuilder typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL)
                    .inclusion(mode)
                    .init(JsonTypeInfo.Id.NAME,null);
            mapper.setDefaultTyping(typer);
            result.add(new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build());
        }
        return result;
    }

    private void verifyGeneratedAsWrapperArraySchema(Set<String> expectedEnumTypes, JsonSchema schema,boolean itemsAreStringArrays){
        Assert.assertTrue("Was expecting array schema, found" + schema.getClass().getSimpleName(),schema.isArraySchema());
        ArraySchema arraySchema = schema.asArraySchema();
        Assert.assertTrue("it should have 2 items",arraySchema.getItems().asArrayItems().getJsonSchemas().length==2);
        JsonSchema[] items = arraySchema.getItems().asArrayItems().getJsonSchemas();
        verifyTypeSchema(expectedEnumTypes, items[0]);
        if(itemsAreStringArrays){
            verifyArrayContentType(items[1]);
        }
    }

    private void verifyArrayContentType(JsonSchema item) {
        Assert.assertTrue("Second should be an array schema", item.isArraySchema());
        Assert.assertTrue("Second should be an array schema of strings", item.asArraySchema().getItems().asSingleItems().getSchema().isStringSchema());
    }

    private void verifyTypeSchema(Set<String> expectedEnumTypes, JsonSchema typeSchema) {
        Assert.assertNotNull("Type schema shuld not be null",typeSchema);
        Assert.assertTrue("Type schema should be a string schema", typeSchema.isStringSchema());
        Assert.assertNotNull("Allowed types should be restricted", typeSchema.asStringSchema().getEnums());
        Assert.assertEquals("Found differences in available types", expectedEnumTypes, typeSchema.asStringSchema().getEnums());
    }

    private void verifyMapContentType(JsonSchema mapTypeSchema) {
        Assert.assertNotNull("mapTypeSchema should not be null");
        Assert.assertTrue("expecting object schema", mapTypeSchema.isObjectSchema());
        ObjectSchema objectSchema = mapTypeSchema.asObjectSchema();
        Assert.assertNotNull("additional properties should be set", objectSchema.getAdditionalProperties());
        Assert.assertTrue("additional properties should be string schema", objectSchema.getAdditionalProperties().asSchemaAdditionalProperties().getJsonSchema().isStringSchema());
    }

    @org.junit.Test
    public void listTestAsWrapperArray() throws JsonProcessingException {
        for(JsonSchemaGenerator unitUnderTest : createGeneratorsForModes(Arrays.asList(JsonTypeInfo.As.WRAPPER_ARRAY,JsonTypeInfo.As.PROPERTY)) ) {
            JsonSchema schema = unitUnderTest.generateSchema(new TypeReference<List<String>>() {
            }.getType());
            System.out.println(unitUnderTest.schemaAsString(schema));
            verifyGeneratedAsWrapperArraySchema(Sets.newHashSet("ArrayList", "List"), schema, true);
        }
    }

    @org.junit.Test
    public void setTestAsWrapperArray() throws JsonProcessingException {
        for(JsonSchemaGenerator unitUnderTest : createGeneratorsForModes(Arrays.asList(JsonTypeInfo.As.WRAPPER_ARRAY,JsonTypeInfo.As.PROPERTY)) ) {
            JsonSchema schema = unitUnderTest.generateSchema(new TypeReference<Set<String>>() {
            }.getType());
            System.out.println(unitUnderTest.schemaAsString(schema));
            verifyGeneratedAsWrapperArraySchema(SET_TYPES, schema, true);
        }
    }

    @org.junit.Test
    public void mapTestAsWrapperArray() throws JsonProcessingException {
        for (JsonSchemaGenerator unitUnderTest : createGeneratorsForModes(Arrays.asList(JsonTypeInfo.As.WRAPPER_ARRAY))) {

            JsonSchema schema = unitUnderTest.generateSchema(new TypeReference<Map<String, String>>() {
            }.getType());
            System.out.println(unitUnderTest.schemaAsString(schema));
            verifyGeneratedAsWrapperArraySchema(MAP_TYPES, schema, false);
            verifyMapContentType(schema.asArraySchema().getItems().asArrayItems().getJsonSchemas()[1]);

        }
    }

    @org.junit.Test
    public void mapTestAsProperty() throws JsonProcessingException {
        for (JsonSchemaGenerator unitUnderTest : createGeneratorsForModes(Arrays.asList(JsonTypeInfo.As.PROPERTY))) {
            JsonSchema schema = unitUnderTest.generateSchema(new TypeReference<Map<String, String>>() {
            }.getType());
            System.out.println(unitUnderTest.schemaAsString(schema));
            verifyMapContentType(schema);
            ObjectSchema objectSchema = schema.asObjectSchema();
            Assert.assertNotNull("properties should be set",objectSchema.getProperties());
            JsonSchema typePropertySchema = objectSchema.getProperties().get(JsonTypeInfo.Id.NAME.getDefaultPropertyName());
            verifyTypeSchema(MAP_TYPES,typePropertySchema);;
        }
    }

    @org.junit.Test
    public void listTestAsWrappeObject() throws JsonProcessingException {
        for(JsonSchemaGenerator unitUnderTest : createGeneratorsForModes(Arrays.asList(JsonTypeInfo.As.WRAPPER_OBJECT)) ) {
            JsonSchema schema = unitUnderTest.generateSchema(new TypeReference<List<String>>() {
            }.getType());
            System.out.println(unitUnderTest.schemaAsString(schema));
            Assert.assertTrue("expecting object schema",schema.isObjectSchema());
            Map<String, JsonSchema> properties = schema.asObjectSchema().getDefinitions();
            Assert.assertNotNull("Should have properties", properties);
            JsonSchema arrayListDefn = properties.get("ArrayList");
            verifyArrayContentType(arrayListDefn.asObjectSchema().getProperties().get("ArrayList"));
        }
    }


    @org.junit.Test
         public void setAsWrappeObject() throws JsonProcessingException {
        for(JsonSchemaGenerator unitUnderTest : createGeneratorsForModes(Arrays.asList(JsonTypeInfo.As.WRAPPER_OBJECT)) ) {
            JsonSchema schema = unitUnderTest.generateSchema(new TypeReference<Set<String>>() {
            }.getType());
            System.out.println(unitUnderTest.schemaAsString(schema));
            JsonSchema contentType=verifyWrapperObjectSchemaAndReturnContentType(SET_TYPES, schema);
            verifyArrayContentType(contentType);
        }
    }

    @org.junit.Test
    public void mapAsWrappeObject() throws JsonProcessingException {
        for(JsonSchemaGenerator unitUnderTest : createGeneratorsForModes(Arrays.asList(JsonTypeInfo.As.WRAPPER_OBJECT)) ) {
            JsonSchema schema = unitUnderTest.generateSchema(new TypeReference<Map<String,String>>() {
            }.getType());
            System.out.println(unitUnderTest.schemaAsString(schema));
            JsonSchema contentType=verifyWrapperObjectSchemaAndReturnContentType(MAP_TYPES, schema);
            verifyMapContentType(contentType);
        }
    }

    public JsonSchema verifyWrapperObjectSchemaAndReturnContentType(Set<String> allowedTypes,JsonSchema schema){
        Assert.assertNotNull("schema should not be null",schema);
        Assert.assertTrue("was expecting  polymorphic object schema",schema.isPolymorhpicObjectSchema());
        PolymorphicObjectSchema polymorphicObjectSchema = schema.asPolymorphicObjectSchema();
        Assert.assertNotNull("definitions should not be null", schema.getDefinitions());
        Set<String> typeNames = new HashSet<String>();
        Set<JsonSchema> contentSchemas = new HashSet<JsonSchema>();
        for(JsonSchema definition : schema.getDefinitions().values()){
            Assert.assertTrue("was expecting object schema",definition.isObjectSchema());
            ObjectSchema objectSchema = definition.asObjectSchema();
            Assert.assertNotNull("should have properties",objectSchema.getProperties());
            Assert.assertEquals("should have a single property", 1, objectSchema.getProperties().size());
            for(Map.Entry<String,JsonSchema> props : objectSchema.getProperties().entrySet()){
                typeNames.add(props.getKey());
                if(!props.getValue().isReferenceSchema()) {
                    contentSchemas.add(props.getValue());
                }
            }
        }
        Assert.assertEquals("type set does not match",allowedTypes,typeNames);
        Assert.assertEquals("There should be only 1 non ref content schema", 1, contentSchemas.size());
        return contentSchemas.iterator().next();
    }





}
