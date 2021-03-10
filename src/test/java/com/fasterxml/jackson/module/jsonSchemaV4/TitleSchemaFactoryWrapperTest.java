package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.module.jsonSchemaV4.customProperties.TitleSchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import junit.framework.TestCase;

public class TitleSchemaFactoryWrapperTest extends TestCase {

    public class Pet {
        public String genus;
    }

    public class Person {
        public String name;
        public String hat;
        public Pet pet;
        public Typed typed;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonTypeName("TypeName")
    public class Typed {
        public String foo;
    }

    public void testAddingTitle() throws Exception {
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator.Builder().withWrapperFactory(new TitleSchemaFactoryWrapper.TitleSchemaFactoryWrapperFactory()).build();

        JsonSchema schema = schemaGenerator.generateSchema(Person.class);

        assertTrue("schema should be an objectSchema.", schema.isObjectSchema());
        String title = schema.asObjectSchema().getTitle();
        assertNotNull(title);
        assertTrue("schema should have a title", title.indexOf("Person") != -1);
        JsonSchema schema2 = schema.asObjectSchema().getProperties().get("pet");
        assertTrue("schema should be an objectSchema.", schema2.isObjectSchema());
        String title2 = schema2.asObjectSchema().getTitle();
        assertNotNull(title2);
        assertTrue("schema should have a title", title2.indexOf("Pet") != -1);

        JsonSchema schema3 = schema.asObjectSchema().getProperties().get("typed");
        assertTrue("schema should be an objectSchema.", schema2.isObjectSchema());
        String title3 = schema3.asObjectSchema().getTitle();
        assertNotNull(title3);
        assertTrue("schema should have a title", title3.indexOf("TypeName") != -1);
    }
}
