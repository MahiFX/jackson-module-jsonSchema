package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;

import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.*;

/**
 * Created by zoliszel on 09/06/2015.
 */

public class PolymorphicTypeTest {


    @Test
    public void polymorphicSchemaGenerationArray() {
        Type type = JSONSubTypeBaseClass[].class;
        JsonSchema schema = schema(type);
        String json = toJson(schema, schema.getClass());
        System.out.println(json);
        Assert.assertNotNull("definitions should have been added", schema.getDefinitions());
        Assert.assertEquals("there should be 3 sub schema", 3, schema.getDefinitions().entrySet().size());
        Assert.assertTrue("Found no Company schema", schema.getDefinitions().containsKey("Company"));
        Assert.assertTrue("Found no Person schema", schema.getDefinitions().containsKey("Person"));
        Assert.assertTrue("Found no BigCompany schema", schema.getDefinitions().containsKey("BigCompany"));
        Assert.assertTrue("Array items should be a one of schema", schema.asArraySchema().getItems().asSingleItems().getSchema() instanceof AnyOfSchema);
        Assert.assertTrue("Any OF Schema Should Contain Company Reference", containsReference(((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema()).getAnyOf(), "Person"));
        Assert.assertTrue("Any OF Schema Should Contain Person Reference", containsReference(((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema()).getAnyOf(), "Company"));
        Assert.assertTrue("Any OF Schema Should Contain Person Reference", containsReference(((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema()).getAnyOf(), "BigCompany"));

    }

    @Test
    public void polymorphicSchemaGenerationObject() {
        Type type = JSONSubTypeBaseClass.class;
        JsonSchema schema = schema(type);
        String json = toJson(schema, schema.getClass());
        System.out.println(json);
        Assert.assertNotNull("definitions should have been added", schema.getDefinitions());
        Assert.assertEquals("there should be 3 sub schema", 3, schema.getDefinitions().entrySet().size());
        Assert.assertTrue("Found no Company schema", schema.getDefinitions().containsKey("Company"));
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
        JSONSubTypeBaseClass[] media = loadJson(PolymorphicTypeTest.class.getResourceAsStream("/polymorphic.json"), JSONSubTypeBaseClass[].class);
        Assert.assertEquals("mismatch in array size", 3, media.length);
        Assert.assertTrue("First item is not a person", media[0] instanceof Person);
        Assert.assertTrue("Second item is not a Company", media[1] instanceof Company);
        Assert.assertTrue("Third item is not a BigCompany", media[2] instanceof BigCompany);
    }


}
