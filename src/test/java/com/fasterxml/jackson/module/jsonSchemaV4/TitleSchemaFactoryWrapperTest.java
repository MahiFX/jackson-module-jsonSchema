package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchemaV4.customProperties.TitleSchemaFactoryWrapper;
import junit.framework.TestCase;

public class TitleSchemaFactoryWrapperTest extends TestCase {

    public class Pet {
        public String genus;
    }

    public class Person {
        public String name;
        public String hat;
        public Pet pet;
    }

    public void testAddingTitle() throws Exception {
        JsonSchemaGenerator schemaGenerator= new JsonSchemaGenerator.Builder().withWrapperFactory(new TitleSchemaFactoryWrapper.TitleSchemaFactoryWrapperFactory()).build();

        JsonSchema schema =schemaGenerator.generateSchema(Person.class);

        assertTrue("schema should be an objectSchema.", schema.isObjectSchema());
        String title = schema.asObjectSchema().getTitle();
        assertNotNull(title);
        assertTrue("schema should have a title", title.indexOf("Person") != -1);
        JsonSchema schema2 = schema.asObjectSchema().getProperties().get("pet");
        assertTrue("schema should be an objectSchema.", schema2.isObjectSchema());
        String title2 = schema2.asObjectSchema().getTitle();
        assertNotNull(title2);
        assertTrue("schema should have a title", title2.indexOf("Pet") != -1);
    }
}
