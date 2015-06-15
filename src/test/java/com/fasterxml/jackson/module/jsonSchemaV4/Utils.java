package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSerializer;
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
    public static JsonSchema schema(Type t) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializerFactory(BeanSerializerFactory.instance.withAdditionalSerializers(new PolymorphicObjectSerializer()));
        try {
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();

            mapper.acceptJsonFormatVisitor(mapper.constructType(t), visitor);
            return visitor.finalSchema();
        } catch (JsonMappingException e) {
            //TODO throw and sort out exception
            return null;
        }
    }


    public static String toJson(Object o, Type type) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithType(mapper.constructType(type)).writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T loadJson(InputStream stream, Class<T> type) {
        return loadJson(stream, type, null);
    }

    public static <T> T loadJson(InputStream stream, Class<T> type, TypeResolverBuilder<?> typer) {
        ObjectMapper mapper = new ObjectMapper();
        if (typer != null) {
            mapper.setDefaultTyping(typer);
        }
        JavaType javaType = mapper.constructType(type);
        try {
            return mapper.readValue(stream, javaType);
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail("Got Exception" + e.getMessage());
            return null; //will not get here
        }
    }


    public static com.github.fge.jsonschema.main.JsonSchema createValidatorSchemaForClass(Class<?> clazz) throws Exception {
        final LoadingConfiguration cfg = LoadingConfiguration.newBuilder()
                .dereferencing(Dereferencing.INLINE).freeze();
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.newBuilder()
                .setLoadingConfiguration(cfg).freeze();

        return schemaFactory.getJsonSchema(generateSchemaFrom(clazz));
    }

    public static JsonNode generateSchemaFrom(Class<?> clazz) throws Exception {
        Object jacksonSchema = Utils.schema(clazz);
        String schemaInString = Utils.toJson(jacksonSchema, jacksonSchema.getClass());
        System.out.println(schemaInString);
        return JsonLoader.fromString(schemaInString);

    }
}
