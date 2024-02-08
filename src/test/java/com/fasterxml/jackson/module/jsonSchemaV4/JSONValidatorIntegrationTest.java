package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.createValidatorSchemaForClass;

/**
 * Created by zoliszel on 11/06/2015.
 */
public class JSONValidatorIntegrationTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
      //  mapper.setSerializerFactory(BeanSerializerFactory.instance.withAdditionalSerializers(new PolymorphicObjectSerializer()));
    }

    @Test
    public void JSONValidateTestSuccess() throws Exception {
        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass[].class, mapper);
        ProcessingReport report;

        report = schema.validate(JsonLoader.fromResource("/polymorphic.json"), true);
        Assert.assertTrue(report.toString(), report.isSuccess());
    }

    @Test
    public void JSONValidateTestFail() throws Exception {
        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass[].class, mapper);

        ProcessingReport report;
        report = schema.validate(JsonLoader.fromResource("/polymorphic_fail.json"), true);
        System.out.println(report);
        Assert.assertFalse(report.toString(), report.isSuccess());
    }

    @Test
    public void javaToJsonValidateTestSuccess() throws Exception {
        JSONSubTypeBaseClass[] elements = new JSONSubTypeBaseClass[]{
                new BigCompany("This is a really big company", "John"), new Person("zoltan", "19820928"), new CompanyObfuscated("this is a small company")
        };

        String jsonString = Utils.toJson(elements, elements.getClass(), new ObjectMapper());
        System.out.println(jsonString);

        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass[].class, mapper);
        ProcessingReport report;
        report = schema.validate(JsonLoader.fromString(jsonString), true);
        Assert.assertTrue(report.toString(), report.isSuccess());
    }

    @Test
    public void javaToJsonValidateTestFail() throws Exception {
        JSONSubTypeBaseClass[] elements = new JSONSubTypeBaseClass[]{
                new BigCompany(), new Person(), new CompanyObfuscated()
        };

        String jsonString = Utils.toJson(elements, elements.getClass(), new ObjectMapper());
        System.out.println(jsonString);

        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass[].class, mapper);
        ProcessingReport report;
        report = schema.validate(JsonLoader.fromString(jsonString), true);
        Assert.assertFalse(report.toString(), report.isSuccess());
    }


    @Test
    public void javaToJsonValidateTestObjectSuccess() throws Exception {
        JSONSubTypeBaseClass[] elements = new JSONSubTypeBaseClass[]{
                new BigCompany("This is a really big company", "John"), new Person("zoltan", "19820928"), new CompanyObfuscated("this is a small company")
        };

        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass.class, mapper);

        for (JSONSubTypeBaseClass element : elements) {
            String jsonString = Utils.toJson(element, element.getClass(), new ObjectMapper());
            System.out.println(jsonString);

            ProcessingReport report;
            report = schema.validate(JsonLoader.fromString(jsonString), true);
            Assert.assertTrue(report.toString(), report.isSuccess());
        }
    }

    @Test
    public void javaToJsonValidateTestObjectFail() throws Exception {
        JSONSubTypeBaseClass[] elements = new JSONSubTypeBaseClass[]{
                new BigCompany(), new Person(), new CompanyObfuscated()
        };

        com.github.fge.jsonschema.main.JsonSchema schema = createValidatorSchemaForClass(JSONSubTypeBaseClass.class, mapper);

        for (JSONSubTypeBaseClass element : elements) {
            String jsonString = Utils.toJson(element, element.getClass(), new ObjectMapper());
            System.out.println(jsonString);

            ProcessingReport report;
            report = schema.validate(JsonLoader.fromString(jsonString), true);
            System.out.println(report.toString());
            Assert.assertFalse(report.toString(), report.isSuccess());
        }
    }


}
