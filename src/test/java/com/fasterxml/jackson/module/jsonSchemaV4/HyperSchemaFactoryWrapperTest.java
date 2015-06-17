package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchemaV4.annotation.JsonHyperSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.annotation.Link;
import com.fasterxml.jackson.module.jsonSchemaV4.customProperties.HyperSchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.types.LinkDescriptionObject;

/**
 * Created by mavarazy on 4/21/14.
 */
public class HyperSchemaFactoryWrapperTest extends SchemaTestBase {

    public class Pet {
        public String genus;
    }

    @JsonHyperSchema(pathStart = "/persons/", links = {
            @Link(href = "{name}", rel = "self"),
            @Link(href = "{name}/pet", rel = "pet", targetSchema = Pet.class)
    })
    public class Person {
        public String name;
        public String hat;
    }

    public void testSimpleHyperWithDefaultSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        HyperSchemaFactoryWrapper personVisitor = new HyperSchemaFactoryWrapper(mapper);
        personVisitor.setIgnoreDefaults(false);


        mapper.acceptJsonFormatVisitor(Person.class, personVisitor);
        JsonSchema personSchema = personVisitor.finalSchema();

        HyperSchemaFactoryWrapper petVisitor = new HyperSchemaFactoryWrapper(mapper);
        mapper.acceptJsonFormatVisitor(Pet.class, petVisitor);
        JsonSchema petSchema = petVisitor.finalSchema();

        assertTrue("schema should be an objectSchema.", personSchema.isObjectSchema());
        LinkDescriptionObject[] links = personSchema.asObjectSchema().getLinks();
        assertNotNull(links);
        assertEquals(links.length, 2);
        LinkDescriptionObject selfLink = links[0];
        assertEquals("/persons/{name}", selfLink.getHref());
        assertEquals("self", selfLink.getRel());
        assertEquals("application/json", selfLink.getEnctype());
        assertEquals("GET", selfLink.getMethod());

        LinkDescriptionObject petLink = links[1];
        assertEquals("/persons/{name}/pet", petLink.getHref());
        assertEquals("pet", petLink.getRel());
        assertEquals("application/json", petLink.getEnctype());
        assertEquals("GET", petLink.getMethod());
        assertEquals(petSchema, petLink.getTargetSchema());
    }

    public void testSimpleHyperWithoutDefaultSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        HyperSchemaFactoryWrapper personVisitor = new HyperSchemaFactoryWrapper(mapper);

        mapper.acceptJsonFormatVisitor(Person.class, personVisitor);
        JsonSchema personSchema = personVisitor.finalSchema();

        HyperSchemaFactoryWrapper petVisitor = new HyperSchemaFactoryWrapper(mapper);
        mapper.acceptJsonFormatVisitor(Pet.class, petVisitor);
        JsonSchema petSchema = petVisitor.finalSchema();

        assertTrue("schema should be an objectSchema.", personSchema.isObjectSchema());
        LinkDescriptionObject[] links = personSchema.asObjectSchema().getLinks();
        assertNotNull(links);
        assertEquals(links.length, 2);
        LinkDescriptionObject selfLink = links[0];
        assertEquals("/persons/{name}", selfLink.getHref());
        assertEquals("self", selfLink.getRel());
        assertEquals(null, selfLink.getEnctype());
        assertEquals(null, selfLink.getMethod());

        LinkDescriptionObject petLink = links[1];
        assertEquals("/persons/{name}/pet", petLink.getHref());
        assertEquals("pet", petLink.getRel());
        assertEquals(null, petLink.getEnctype());
        assertEquals(null, petLink.getMethod());
        assertEquals(petSchema, petLink.getTargetSchema());
    }

}
