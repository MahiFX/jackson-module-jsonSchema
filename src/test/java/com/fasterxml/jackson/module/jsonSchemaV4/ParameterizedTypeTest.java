package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.fge.jackson.JsonLoader;
import org.junit.Test;

/**
 * Created by zoliszel on 15/06/2015.
 */
public class ParameterizedTypeTest {

    @JsonTypeName("Parameterized")
    public static class Parameterized<T> {

        @JsonProperty("embeded")
        private Parameterized<T> embeded;

        public Parameterized<T> getEmbeded() {
            return embeded;
        }

        public void setEmbeded(Parameterized<T> embeded) {
            this.embeded = embeded;
        }

        @JsonProperty("value")
        private T value;

        public void setValue(T value) {
            this.value = value;
        }


        public T getValue() {
            return value;
        }
    }

    @Test
    public void testSchemaCreationAndValidation() throws Exception {
        com.github.fge.jsonschema.main.JsonSchema validatorSchema = Utils.createValidatorSchemaForClass(Parameterized.class);
        validatorSchema.validate(JsonLoader.fromString("{}"), true);
    }
}
