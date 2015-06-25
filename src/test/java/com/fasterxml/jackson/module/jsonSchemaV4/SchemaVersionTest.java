package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.module.jsonSchemaV4.customProperties.HyperSchemaFactoryWrapper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zoliszel on 25/06/2015.
 */
public class SchemaVersionTest {

    @Test
    public void testRegularSchema()throws Exception{
        JsonSchema schema = new JsonSchemaGenerator.Builder().withIncludeJsonSchemaVersion(true).build().generateSchema(Object.class);
        Assert.assertEquals(JsonSchemaGenerator.SCHEMA_V4,schema.get$schema());
    }

    @Test
    public void testHyperSchema() throws Exception{
        JsonSchema schema = new JsonSchemaGenerator.Builder().withIncludeJsonSchemaVersion(true).withWrapperFactory(new HyperSchemaFactoryWrapper.HyperSchemaFactoryWrapperFactory()).build().generateSchema(Object.class);
        Assert.assertEquals(JsonSchemaGenerator.HYPER_SCHEMA_V4,schema.get$schema());

    }
}

