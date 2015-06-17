package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer.PolymorphicObjectSerializer;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class RecursivePolymorhicClassTest {

    private final ObjectMapper schemaMapper = new ObjectMapper();

    @Before
    public void setup() {
        schemaMapper.setSerializerFactory(BeanSerializerFactory.instance.withAdditionalSerializers(new PolymorphicObjectSerializer()));
        //schemaMapper.enableDefaultTyping();

    }

    @JsonTypeName("A")
    @JsonSubTypes({
            @JsonSubTypes.Type(B.class),
            @JsonSubTypes.Type(C.class)
    })
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    interface A {

        A[] someEmptyArray = new A[0];
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonTypeName("B")
    static class B implements A {

        public String someProperty = "default";

        @JsonProperty(value = "badBoy", required = true)
        public A[] thisIsARecursiveProperty;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonTypeName("C")
    static class C implements A {

        @JsonProperty(value = "badBoy2", required = true)
        public A[] thisIsARecursiveProperty;
    }


    @Test
    public void noStackOverFlow() {
        JsonSchema schema = Utils.schema(B.class, schemaMapper);
        System.out.println(Utils.toJson(schema, schema.getClass(), schemaMapper));
    }

    @Test
    public void validateTest() throws Exception {

        B b1 = new B();
        b1.someProperty = "a";
        b1.thisIsARecursiveProperty = new A[0];
        B b2 = new B();
        b2.thisIsARecursiveProperty = new A[0];
        b2.someProperty = "b";
        A[] forB = new B[]{b1, b2};
        B b3 = new B();
        b3.thisIsARecursiveProperty = forB;
        String jsonInText = Utils.toJson(b3, b3.getClass(), schemaMapper);
        System.out.println(jsonInText);
        com.github.fge.jsonschema.main.JsonSchema validatorSchema = Utils.createValidatorSchemaForClass((B.class), schemaMapper);
        ProcessingReport report = validatorSchema.validate(JsonLoader.fromString(jsonInText));
        System.out.println(report.toString());
        Assert.assertTrue("Json Failed validation", report.isSuccess());
    }


}
