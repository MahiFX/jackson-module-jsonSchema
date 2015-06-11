package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;

import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.*;

/**
 * Created by zoliszel on 09/06/2015.
 */

public class PolymorphicTypeTest {


    @Test
    public void polymorphicSchemaGeneration() {
        Type type = JSONSubTypeBaseClass[].class;
        JsonSchema schema = schema(type);
        String json = toJson(schema, schema.getClass());
        System.out.println(json);
        Assert.assertNotNull("definitionts should have been added", schema.getDefinitions());
        Assert.assertEquals("there should be 3 sub schena", 3, schema.getDefinitions().entrySet().size());
        Assert.assertTrue("Found no Company schema", schema.getDefinitions().containsKey("Company"));
        Assert.assertTrue("Found no Person schema", schema.getDefinitions().containsKey("Person"));
        Assert.assertTrue("Found no BigCompany schema", schema.getDefinitions().containsKey("BigCompany"));
        Assert.assertTrue("Array items should be a one of schema", schema.asArraySchema().getItems().asSingleItems().getSchema() instanceof AnyOfSchema);
        Assert.assertTrue("One OF Schema Should Contain Company Reference", containsReference((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema(), "Person"));
        Assert.assertTrue("One OF Schema Should Contain Person Reference", containsReference((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema(), "Company"));
        Assert.assertTrue("One OF Schema Should Contain Person Reference", containsReference((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema(), "BigCompany"));

    }

    private boolean containsReference(AnyOfSchema schema, String name) {
        if (schema.getAnyOf() == null) {
            return false;
        }
        for (com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema refSchema : schema.getAnyOf()) {
            if (("#/definitions/" + name).equals(refSchema.get$ref())) {
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
