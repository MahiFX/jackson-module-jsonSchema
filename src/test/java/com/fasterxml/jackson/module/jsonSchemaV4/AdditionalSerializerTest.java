package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.io.IOException;

/**
 * Created by zoliszel on 24/06/2015.
 */
public class AdditionalSerializerTest {

    public static class MyModule extends SimpleModule {

        public MyModule(JsonSerializer<MyClass> serializer) {
            addSerializer(MyClass.class, serializer);
        }


    }

    @JsonTypeName("MyClass")
    public static class MyClass {
        @JsonProperty
        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }

    @Test
    public void testSerializerIsCalled() throws Exception{
        JsonSerializer<MyClass> mySerializer=mock(JsonSerializer.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MyModule(mySerializer));
        JsonSchemaGenerator generator = new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
        try {
            JsonSchema schema = generator.generateSchema(MyClass.class);
        }
        catch(NullPointerException npe){
            //ignore, our mock serializer will not create anything
        }

        verify(mySerializer).acceptJsonFormatVisitor(any(JsonFormatVisitorWrapper.class),any(JavaType.class));
        //schema.toString();

    }
}
