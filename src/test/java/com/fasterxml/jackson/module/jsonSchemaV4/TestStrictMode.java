package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import org.junit.Assert;

/**
 * Created by zoliszel on 25/06/2015.
 */
public class TestStrictMode {

    public static class Test{
        private String prop1;
        private Number number;
        private  Object someObject;

        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }

        public Number getNumber() {
            return number;
        }

        public void setNumber(Number number) {
            this.number = number;
        }

        public Object getSomeObject() {
            return someObject;
        }

        public void setSomeObject(Object someObject) {
            this.someObject = someObject;
        }
    }

    @org.junit.Test
    public void testStrictMode() throws JsonProcessingException {
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withAdditonalProperties(false).build();
        JsonSchema schema = generator.generateSchema(Test.class);
        System.out.print(generator.schemaAsString(schema,true));
        Assert.assertSame("Additional properties are not set", schema.asObjectSchema().getAdditionalProperties(), ObjectSchema.NoAdditionalProperties.instance);
        Assert.assertNotSame("some object should have additonal properties", schema.asObjectSchema().getProperties().get("someObject").asObjectSchema().getProperties(), ObjectSchema.NoAdditionalProperties.instance);
    }
}
