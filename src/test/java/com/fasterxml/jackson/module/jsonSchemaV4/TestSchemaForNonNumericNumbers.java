package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by zoliszel on 26/06/2015.
 */
public class TestSchemaForNonNumericNumbers {


    private static class ClassWithDoulbe{
        private double myDouble;


        private double myDouble2;
        private double myDouble3;
        public void setMyDouble(double myDouble) {
            this.myDouble = myDouble;
        }

        @JsonProperty
        public double getMyDouble() {
            return myDouble;
        }
        @JsonProperty
        public double getMyDouble2() {
            return myDouble2;
        }
        @JsonProperty
        public double getMyDouble3() {
            return myDouble3;
        }
    }


    @Test
    public void generateSchemaForNonNumericNumbers() throws Exception{

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();

        for(Class<?> clazz: new Class[]{Double.class,Float.class}){
            JsonSchema schema = generator.generateSchema(clazz);
            System.out.println(generator.schemaAsString(schema));

            Assert.assertTrue("schema should be reference schema",schema.isReferenceSchema());
            Assert.assertNotNull("Reference schema should have definitions",schema.getDefinitions());
            verifyNonNumericSchema(schema);
        }
    }

    @Test
    public void verifyMultipleDoublesAreSupported() throws Exception{

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();

        JsonSchema schema = generator.generateSchema(ClassWithDoulbe.class);
        System.out.println(generator.schemaAsString(schema));

        Assert.assertTrue("schema should be object schema",schema.isObjectSchema());
        Assert.assertNotNull("Object schema should have definitions",schema.getDefinitions());
        verifyNonNumericSchema(schema);

    }

    private void verifyNonNumericSchema(JsonSchema schema) {
        Set<String> allowedNonStringValues = new HashSet<String>(Arrays.asList("INF","Infinity","-INF","-Infinity","NaN"));
        JsonSchema referedSchema = schema.getDefinitions().get(PolymorphicHandlingUtil.NUMBER_WITH_NON_NUMERIC_VALUES);
        Assert.assertTrue("Refered schema should be polymorphic", referedSchema.isPolymorhpicObjectSchema());
        PolymorphicObjectSchema objectSchema  =referedSchema.asPolymorphicObjectSchema();
        Assert.assertNotNull("One of should not be empty", objectSchema.getOneOf());
        Assert.assertEquals("One of should have 2 elements",2,objectSchema.getOneOf().length);
        JsonSchema first = objectSchema.getOneOf()[0];
        JsonSchema second = objectSchema.getOneOf()[1];
        Assert.assertTrue("first should be reference schema",first.isReferenceSchema());
        Assert.assertTrue("second should be reference schema",second.isReferenceSchema());

        JsonSchema numberSchema =  schema.getDefinitions().get(PolymorphicHandlingUtil.NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE);
        JsonSchema stringSchema =  schema.getDefinitions().get(PolymorphicHandlingUtil.NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE);
        Assert.assertTrue("First should be number schema",numberSchema.isNumberSchema());
        Assert.assertTrue("Second should be string schema",stringSchema.isStringSchema());
        Set<String> enums = stringSchema.asStringSchema().getEnums();
        Assert.assertNotNull("string schema should be restricted",enums);
        for(String allowed : allowedNonStringValues) {
            Assert.assertTrue("string schema should allow " + allowed, enums.contains(allowed));
        }
    }


    @Test
    public void testNonNumericValuesAreNotAllowedInSchema() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        for(Class<?> clazz: new Class[]{Double.class,Float.class}){
            JsonSchema schema = generator.generateSchema(clazz);
            Assert.assertTrue("schema should be polymorphic",schema.isNumberSchema());

        }
    }

    @Test
    public void validationTesting() throws Exception{

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);

        com.github.fge.jsonschema.main.JsonSchema validatorSchema = Utils.createValidatorSchemaForClass(ClassWithDoulbe.class, mapper);
        System.out.println(new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build().schemaAsString(ClassWithDoulbe.class));
        for(String s : new String[]{"5","5.5","NaN"}){
            String json  = "{\"myDouble\" : %S%}".replace("%S%",s);
            JsonNodeReader READER = new JsonNodeReader(mapper);
            ProcessingReport report = validatorSchema.validate(READER.fromReader(new StringReader(json)));
            Assert.assertTrue("Value is not accepted" + json+ "\n" + report.toString(), report.isSuccess());
        }
    }

    @Test
    public void testIntegersAreNotSpecial() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        for(Class<?> clazz: new Class[]{Integer.class}){
            JsonSchema schema = generator.generateSchema(clazz);
            Assert.assertTrue("schema should be an integer schema",schema instanceof IntegerSchema);

        }
    }
}
