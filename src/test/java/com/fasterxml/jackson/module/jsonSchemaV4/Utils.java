package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.load.Dereferencing;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * Created by zoliszel on 11/06/2015.
 */
public class Utils {

    public static JsonSchema schema(Type t, final ObjectMapper mapper) {
        try {
            /*
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper(mapper);

            mapper.acceptJsonFormatVisitor(mapper.constructType(t), visitor);
            return visitor.finalSchema();
            */
            return new JsonSchemaGenerator(mapper).generateSchema(t);
        } catch (JsonMappingException e) {
            //TODO throw and sort out exception
            return null;
        }
    }

    public static String toJson(Object o, Type type, ObjectMapper mapper) {
        try {
            return mapper.writer().writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T loadJson(InputStream stream, Class<T> type, ObjectMapper mapper) {

        JavaType javaType = mapper.constructType(type);
        try {
            return mapper.readValue(stream, javaType);
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail("Got Exception" + e.getMessage());
            return null; //will not get here
        }
    }


    public static com.github.fge.jsonschema.main.JsonSchema createValidatorSchemaForClass(Class<?> clazz, ObjectMapper mapper) throws Exception {
        final LoadingConfiguration cfg = LoadingConfiguration.newBuilder()
                .dereferencing(Dereferencing.INLINE).freeze();
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.newBuilder()
                .setLoadingConfiguration(cfg).freeze();

        return schemaFactory.getJsonSchema(generateSchemaFrom(clazz, mapper));
    }

    public static JsonNode generateSchemaFrom(Class<?> clazz, ObjectMapper mapper) throws Exception {
        Object jacksonSchema = schema(clazz, mapper);
        String schemaInString = Utils.toJson(jacksonSchema, jacksonSchema.getClass(), mapper);
        System.out.println(schemaInString);
        return JsonLoader.fromString(schemaInString);

    }
}
