package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.module.jsonSchemaV4.types.NullSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;

/**
 * @author Yoann Rodière
 */
public class TestEquals extends SchemaTestBase {

    public void testEquals() throws Exception {
        ObjectSchema schema1 = new ObjectSchema();
        ObjectSchema schema2 = new ObjectSchema();
        schema2.getProperties().put("property1", new NullSchema());

        assertTrue(!schema1.equals(schema2));
    }

}
