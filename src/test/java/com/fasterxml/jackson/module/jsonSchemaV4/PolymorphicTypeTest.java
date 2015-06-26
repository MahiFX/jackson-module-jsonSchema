package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer.PolymorphicObjectSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;

import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.*;

/**
 * Created by zoliszel on 09/06/2015.
 */

public class PolymorphicTypeTest {
    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
       // mapper.setSerializerFactory(BeanSerializerFactory.instance.withAdditionalSerializers(new PolymorphicObjectSerializer()));
    }


    @Test
    public void polymorphicSchemaGenerationArray() {
        Type type = JSONSubTypeBaseClass[].class;
        JsonSchema schema = schema(type, mapper);
        String json = toJson(schema, schema.getClass(), mapper);
        System.out.println(json);
        Assert.assertNotNull("definitions should have been added", schema.getDefinitions());
        Assert.assertEquals("there should be 4 sub schema", 4, schema.getDefinitions().entrySet().size());
        Assert.assertTrue("Found no Company schema", schema.getDefinitions().containsKey("Company"));
        Assert.assertTrue("Found no Company_1 schema", schema.getDefinitions().containsKey("Company" + PolymorphicHandlingUtil.POLYMORPHIC_TYPE_NAME_SUFFIX));
        Assert.assertTrue("Found no Person schema", schema.getDefinitions().containsKey("Person"));
        Assert.assertTrue("Found no BigCompany schema", schema.getDefinitions().containsKey("BigCompany"));
        Assert.assertTrue("Array items should be a one of schema", (schema.asArraySchema().getItems()).asSingleItems().getSchema() instanceof AnyOfSchema);
        Assert.assertTrue("Any OF Schema Should Contain Person Reference", containsReference(getAnyOfFromArray(schema).getAnyOf(), "Person"));
        Assert.assertTrue("Any OF Schema Should Contain Company Reference", containsReference(getAnyOfFromArray(schema).getAnyOf(), "Company"));
        Assert.assertTrue("Any OF Schema Should Contain BigCompany Reference", containsReference(getAnyOfFromArray(schema).getAnyOf(), "BigCompany"));

    }

    private AnyOfSchema getAnyOfFromArray(JsonSchema schema) {
        return (AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema();
    }


    @Test
    public void polymorphicSchemaGenerationObject() {
        Type type = JSONSubTypeBaseClass.class;
        JsonSchema schema = schema(type, mapper);
        String json = toJson(schema, schema.getClass(), mapper);
        System.out.println(json);
        Assert.assertNotNull("definitions should have been added", schema.getDefinitions());
        Assert.assertEquals("there should be 4 sub schema", 4, schema.getDefinitions().entrySet().size());
        Assert.assertTrue("Found no Company schema", schema.getDefinitions().containsKey("Company"));
        Assert.assertTrue("Found no Company_1 schema", schema.getDefinitions().containsKey("Company" + PolymorphicHandlingUtil.POLYMORPHIC_TYPE_NAME_SUFFIX));
        Assert.assertTrue("Found no Person schema", schema.getDefinitions().containsKey("Person"));
        Assert.assertTrue("Found no BigCompany schema", schema.getDefinitions().containsKey("BigCompany"));
        Assert.assertTrue("Expected polymorphicObject", schema instanceof PolymorphicObjectSchema);
        Assert.assertTrue("Expected polymorphicObject", (((PolymorphicObjectSchema)schema).getId()!=null));
        Assert.assertTrue("PolymoprhicSchema should contain Person Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), "Person"));
        Assert.assertTrue("PolymoprhicSchema should contain Company Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), "Company"));
        Assert.assertTrue("PolymoprhicSchema should contain BigCompany Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), "BigCompany"));

    }


    @Test
    public void testSchemaGenerationIsIdempotent() throws JsonProcessingException {
        Type type = JSONSubTypeBaseClass.class;
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        JsonSchema schema = schemaGenerator.generateSchema(type);
        schema = schemaGenerator.generateSchema(type);
        String json = schemaGenerator.schemaAsString(schema);
        System.out.println(json);
        Assert.assertNotNull("definitions should have been added", schema.getDefinitions());
        Assert.assertEquals("there should be 4 sub schema", 4, schema.getDefinitions().entrySet().size());
        Assert.assertTrue("Found no Company schema", schema.getDefinitions().containsKey("Company"));
        Assert.assertTrue("Found no Company_1 schema", schema.getDefinitions().containsKey("Company" + PolymorphicHandlingUtil.POLYMORPHIC_TYPE_NAME_SUFFIX));
        Assert.assertTrue("Found no Person schema", schema.getDefinitions().containsKey("Person"));
        Assert.assertTrue("Found no BigCompany schema", schema.getDefinitions().containsKey("BigCompany"));
        Assert.assertTrue("Expected polymorphicObject", schema instanceof PolymorphicObjectSchema);
        Assert.assertTrue("PolymoprhicSchema should contain Person Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), "Person"));
        Assert.assertTrue("PolymoprhicSchema should contain Company Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), "Company"));
        Assert.assertTrue("PolymoprhicSchema should contain BigCompany Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), "BigCompany"));

    }

    private boolean containsReference(ReferenceSchema[] refSchemas, String name) {
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
        Assert.assertTrue("Second item is not a Company", media[1] instanceof Company);
        Assert.assertTrue("Third item is not a BigCompany", media[2] instanceof BigCompany);
    }


    @JsonTypeName("Number")
    @JsonSubTypes({@JsonSubTypes.Type(Integer.class),
                   @JsonSubTypes.Type(Double.class)})
    interface UnionType{

    }

    @JsonTypeName("Double")
    interface DoubleType{

    }

    @JsonTypeName("Integer")
    interface IntegerType{

    }
    @Test
    public void testUnionType() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Number.class, UnionType.class);
        mapper.addMixIn(Double.class, DoubleType.class);
        mapper.addMixIn(Integer.class, IntegerType.class);
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        String schema = generator.schemaAsString(Number.class);
        System.out.println(schema);
        Assert.assertEquals("{\"id\":\"urn:jsonschema:java:lang:Number\",\"type\":[\"number\",\"integer\"],\"definitions\":{\"Integer\":{\"type\":\"integer\"},\"Double\":{\"type\":\"number\"}},\"anyOf\":[{\"$ref\":\"#/definitions/Double\"},{\"$ref\":\"#/definitions/Integer\"}]}", schema);
    }


}
