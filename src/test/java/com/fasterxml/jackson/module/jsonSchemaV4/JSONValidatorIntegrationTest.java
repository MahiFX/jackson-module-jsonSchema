package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.load.Dereferencing;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zoliszel on 11/06/2015.
 */
public class JSONValidatorIntegrationTest {

    @Test
    public void JSONValidateTestSuccess() throws Exception {
        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass[].class);
        ProcessingReport report;

        report = schema.validate(JsonLoader.fromResource("/polymorphic.json"), true);
        Assert.assertTrue(report.toString(), report.isSuccess());
    }

    @Test
    public void JSONValidateTestFail() throws Exception {
        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass[].class);

        ProcessingReport report;
        report = schema.validate(JsonLoader.fromResource("/polymorphic_fail.json"), true);
        System.out.println(report);
        Assert.assertFalse(report.toString(), report.isSuccess());
    }

    @Test
    public void javaToJsonValidateTestSuccess() throws Exception {
        JSONSubTypeBaseClass[] elements = new JSONSubTypeBaseClass[]{
                new BigCompany("This is a really big company", "John"), new Person("zoltan", "19820928"), new Company("this is a small company")
        };

        String jsonString = Utils.toJson(elements, elements.getClass());
        System.out.println(jsonString);

        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass[].class);
        ProcessingReport report;
        report = schema.validate(JsonLoader.fromString(jsonString), true);
        Assert.assertTrue(report.toString(), report.isSuccess());
    }

    @Test
    public void javaToJsonValidateTestFail() throws Exception {
        JSONSubTypeBaseClass[] elements = new JSONSubTypeBaseClass[]{
                new BigCompany(), new Person(), new Company()
        };

        String jsonString = Utils.toJson(elements, elements.getClass());
        System.out.println(jsonString);

        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass[].class);
        ProcessingReport report;
        report = schema.validate(JsonLoader.fromString(jsonString), true);
        Assert.assertFalse(report.toString(), report.isSuccess());
    }

    private com.github.fge.jsonschema.main.JsonSchema createValidatorSchemaForClass(Class<?> clazz) throws Exception {
        final LoadingConfiguration cfg = LoadingConfiguration.newBuilder()
                .dereferencing(Dereferencing.INLINE).freeze();
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.newBuilder()
                .setLoadingConfiguration(cfg).freeze();

        return schemaFactory.getJsonSchema(generateSchemaFrom(clazz));
    }

    private JsonNode generateSchemaFrom(Class<?> clazz) throws Exception {
        Object jacksonSchema = Utils.schema(clazz);
        String schemaInString = Utils.toJson(jacksonSchema, jacksonSchema.getClass());
        System.out.println(schemaInString);
        return JsonLoader.fromString(schemaInString);

    }

}
