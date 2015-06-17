package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.toJson;

/**
 * Created by zoliszel on 16/06/2015.
 */
public class TypeDecorationTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new ObjectMapper();
    }

    @JsonTypeName(TypeParameterAsProperty.NAME)
    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
    public static class TypeParameterAsProperty {

        public static final String NAME = "ParameterizedWithName";

        @JsonProperty()
        public String thisIsAMember;
    }

    @JsonTypeName(TypeParameterAsClassProperty.NAME)
    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS)
    public static class TypeParameterAsClassProperty extends TypeParameterAsProperty {
        public static final String NAME = "TypeParameterAsClassProperty";
    }


    public static class MixInTest {

        public String thisIsAMember;

        public String getThisIsAMember() {
            return thisIsAMember;
        }
    }

    @JsonTypeName(MixIn.NAME)
    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
    public interface MixIn {

        String NAME = "MixInTest";

        String getThisIsAMember();
    }

    @Test
    public void testTypeAsProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        typer = typer.init(JsonTypeInfo.Id.NAME, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);


        SchemaFactoryWrapper wrapper = new SchemaFactoryWrapper(mapper);
        mapper.acceptJsonFormatVisitor(TypeParameterAsProperty.class, wrapper);
        JsonSchema schema = wrapper.finalSchema();
        System.out.println(toJson(schema, schema.getClass(), mapper));
        Set<String> required = schema.asObjectSchema().getRequired();
        Assert.assertTrue("type info should be required", required != null && required.contains(JsonTypeInfo.Id.NAME.getDefaultPropertyName()));
        Assert.assertTrue("should have a type property", schema.asObjectSchema().getProperties().containsKey(JsonTypeInfo.Id.NAME.getDefaultPropertyName()));
        Assert.assertTrue("TypePropertyShouldBeRestricted", schema.asObjectSchema().getProperties().get(JsonTypeInfo.Id.NAME.getDefaultPropertyName()).asStringSchema().getEnums().contains(TypeParameterAsProperty.NAME));
    }

    @Test
    public void testTypeAsPropertyClass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);


        SchemaFactoryWrapper wrapper = new SchemaFactoryWrapper(mapper);
        mapper.acceptJsonFormatVisitor(TypeParameterAsClassProperty.class, wrapper);
        JsonSchema schema = wrapper.finalSchema();
        System.out.println(toJson(schema, schema.getClass(), mapper));
        Set<String> required = schema.asObjectSchema().getRequired();
        Assert.assertTrue("type info should be required", required != null && required.contains(JsonTypeInfo.Id.CLASS.getDefaultPropertyName()));
        Assert.assertTrue("should have a type property", schema.asObjectSchema().getProperties().containsKey(JsonTypeInfo.Id.CLASS.getDefaultPropertyName()));
        Assert.assertTrue("TypePropertyShouldBeRestricted", schema.asObjectSchema().getProperties().get(JsonTypeInfo.Id.CLASS.getDefaultPropertyName()).asStringSchema().getEnums().contains(TypeParameterAsClassProperty.class.getName()));
    }

    @Test
    public void supportForMixIns() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        typer = typer.init(JsonTypeInfo.Id.NAME, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);

        mapper.addMixIn(MixInTest.class, MixIn.class);

        SchemaFactoryWrapper wrapper = new SchemaFactoryWrapper(mapper);
        mapper.acceptJsonFormatVisitor(MixInTest.class, wrapper);
        JsonSchema schema = wrapper.finalSchema();
        System.out.println(toJson(schema, schema.getClass(), mapper));
        Set<String> required = schema.asObjectSchema().getRequired();
        Assert.assertTrue("type info should be required", required != null && required.contains(JsonTypeInfo.Id.NAME.getDefaultPropertyName()));
        Assert.assertTrue("should have a type property", schema.asObjectSchema().getProperties().containsKey(JsonTypeInfo.Id.NAME.getDefaultPropertyName()));
        Assert.assertTrue("TypePropertyShouldBeRestricted", schema.asObjectSchema().getProperties().get(JsonTypeInfo.Id.NAME.getDefaultPropertyName()).asStringSchema().getEnums().contains(MixIn.NAME));
    }
}
