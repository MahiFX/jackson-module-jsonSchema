package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.google.common.collect.Sets;
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

    @JsonTypeName(JSONSubTypeBaseClassArrayMixIn.TYPE_NAME)
    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
    public interface JSONSubTypeBaseClassArrayMixIn {
        String TYPE_NAME = "JSONSubTypeBaseClassArrayMixIn[]";

    }
    private JsonSchemaGenerator createGeneratorWithTyperFor(JsonTypeInfo.Id id) {
       return createGeneratorWithTyperFor(new ObjectMapper(),id);
    }

    private JsonSchemaGenerator createGeneratorWithTyperFor(ObjectMapper mapper,JsonTypeInfo.Id id) {
        TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        typer = typer.init(id, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
        mapper.setDefaultTyping(typer);

        return new JsonSchemaGenerator.Builder().withObjectMapper(mapper).build();
    }

    private void verifyTypeIsRestrictredForObjectSchema(JsonSchema schema,JsonTypeInfo.Id id,Set<String> typeNames) {
        Set<String> required = schema.asObjectSchema().getRequired();
        Assert.assertTrue("Type info should be required", required != null && required.contains(id.getDefaultPropertyName()));
        Assert.assertTrue("Should have a type property", schema.asObjectSchema().getProperties().containsKey(id.getDefaultPropertyName()));
        Assert.assertEquals("Miss match in allowed types", typeNames, schema.asObjectSchema().getProperties().get(id.getDefaultPropertyName()).asStringSchema().getEnums());
    }


    @Test
    public void testTypeAsProperty() throws Exception {
        JsonSchemaGenerator generator = createGeneratorWithTyperFor(JsonTypeInfo.Id.NAME);
        JsonSchema schema =generator.generateSchema(TypeParameterAsProperty.class);
        System.out.println(toJson(schema, schema.getClass(), new ObjectMapper()));
        verifyTypeIsRestrictredForObjectSchema(schema, JsonTypeInfo.Id.NAME, Sets.newHashSet(TypeParameterAsProperty.NAME));
    }

    @Test
    public void testTypeAsPropertyClass() throws Exception {
        JsonSchemaGenerator generator = createGeneratorWithTyperFor(JsonTypeInfo.Id.CLASS);
        JsonSchema schema =generator.generateSchema(TypeParameterAsClassProperty.class);
        System.out.println(toJson(schema, schema.getClass(), new ObjectMapper()));
        verifyTypeIsRestrictredForObjectSchema(schema, JsonTypeInfo.Id.CLASS, Sets.newHashSet(TypeParameterAsClassProperty.class.getName()));
    }

    @Test
    public void supportForMixIns() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(MixInTest.class, MixIn.class);
        JsonSchemaGenerator generator = createGeneratorWithTyperFor(mapper, JsonTypeInfo.Id.NAME);
        JsonSchema schema =generator.generateSchema(MixInTest.class);
        System.out.println(toJson(schema, schema.getClass(), new ObjectMapper()));
        verifyTypeIsRestrictredForObjectSchema(schema, JsonTypeInfo.Id.NAME, Sets.newHashSet(MixIn.NAME));
    }

    @Test
    public void forPolyMorphicObjectsArraysMixIn() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(JSONSubTypeBaseClass[].class, JSONSubTypeBaseClassArrayMixIn.class);

        JsonSchemaGenerator generator = createGeneratorWithTyperFor(mapper, JsonTypeInfo.Id.NAME);
        JsonSchema schema = generator.generateSchema(JSONSubTypeBaseClass[].class);

        System.out.println(toJson(schema, schema.getClass(), new ObjectMapper()));
       // verifySchemaInDefinitions(schema, JsonTypeInfo.Id.NAME, "Company", Sets.newHashSet("Company"));
        verifySchemaInDefinitions(schema, JsonTypeInfo.Id.NAME, "Company" + PolymorphicSchemaUtil.POLYMORPHIC_TYPE_NAME_SUFFIX, Sets.newHashSet("Company" ));
        verifySchemaInDefinitions(schema, JsonTypeInfo.Id.NAME, "Person", Sets.newHashSet("Person"));
        verifySchemaInDefinitions(schema, JsonTypeInfo.Id.NAME, "BigCompany", Sets.newHashSet("BigCompany"));
        JsonSchema[] arrayItems = schema.asArraySchema().getItems().asArrayItems().getJsonSchemas();
        Assert.assertNotNull("Type information is not encoded for Array type", arrayItems[0].asStringSchema());
        Assert.assertEquals("Type is not restricted", Sets.newHashSet(JSONSubTypeBaseClassArrayMixIn.TYPE_NAME), arrayItems[0].asStringSchema().getEnums());
    }

    @Test
    public void forPolyMorphicObjects() throws Exception {
        JsonSchemaGenerator generator = createGeneratorWithTyperFor(JsonTypeInfo.Id.NAME);
        JsonSchema schema =generator.generateSchema(JSONSubTypeBaseClass.class);
        System.out.println(toJson(schema, schema.getClass(), new ObjectMapper()));
//        verifySchemaInDefinitions(schema,JsonTypeInfo.Id.NAME,"Company",Sets.newHashSet("Company"));
        verifySchemaInDefinitions(schema, JsonTypeInfo.Id.NAME, "Company" + PolymorphicSchemaUtil.POLYMORPHIC_TYPE_NAME_SUFFIX, Sets.newHashSet("Company"));
        verifySchemaInDefinitions(schema,JsonTypeInfo.Id.NAME,"Person",Sets.newHashSet("Person"));
        verifySchemaInDefinitions(schema,JsonTypeInfo.Id.NAME,"BigCompany",Sets.newHashSet("BigCompany"));


    }

    private void verifySchemaInDefinitions(JsonSchema schema,JsonTypeInfo.Id id,String name,Set<String> typeNames) {
        Assert.assertNotNull("definitons should not be empty", schema.getDefinitions());
        Assert.assertTrue("Found no" + name + "schema", schema.getDefinitions().containsKey(name));
        JsonSchema definitionSchema =schema.getDefinitions().get(name);
        ObjectSchema objectSchema = definitionSchema.asObjectSchema();
        Assert.assertNotNull("Properties should not be empty", objectSchema.getProperties());
        Assert.assertTrue("No @Type Property", objectSchema.getProperties().containsKey(id.getDefaultPropertyName()));
        Assert.assertTrue("@Type property is not required", objectSchema.getRequired().contains(id.getDefaultPropertyName()));
        JsonSchema typeRestriction = objectSchema.getProperties().get(id.getDefaultPropertyName());
        Assert.assertTrue("Type restriction should be a string schema",typeRestriction.isStringSchema());
        Assert.assertEquals("Mismatch in allowed types", typeNames, typeRestriction.asStringSchema().getEnums());
    }



    @Test
    public void objectDoesntHaveTypeRestriction() throws Exception {
        JsonSchemaGenerator generator = createGeneratorWithTyperFor(JsonTypeInfo.Id.NAME);
        JsonSchema schema =generator.generateSchema(Object.class);
        String json =toJson(schema, schema.getClass(), new ObjectMapper());
        Assert.assertEquals("{\"type\":[\"string\",\"number\",\"integer\",\"boolean\",\"object\",\"array\",\"null\"]}",json);
    }

    @JsonTypeName(ClassAsWrapperObject.TYPE_NAME)
    @JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
    public static class ClassAsWrapperObject{

        public static final String TYPE_NAME="ClassAsWrapperObject";
        @JsonProperty
        private String someProperty;


        public void setSomeProperty(String someProperty) {
            this.someProperty = someProperty;
        }

        public String getSomeProperty() {
            return someProperty;
        }
    }
    @Test
    public void testWrapperObjectDecoration()throws Exception{
        JsonSchemaGenerator generator = createGeneratorWithTyperFor(JsonTypeInfo.Id.NAME);
        JsonSchema schema =generator.generateSchema(ClassAsWrapperObject.class);

        String json =toJson(schema, schema.getClass(), new ObjectMapper());
        Assert.assertEquals("{\"type\":\"object\",\"properties\":{\"ClassAsWrapperObject\":{\"id\":\"urn:jsonschema:com:fasterxml:jackson:module:jsonSchemaV4:TypeDecorationTest:ClassAsWrapperObject\",\"type\":\"object\",\"properties\":{\"someProperty\":{\"type\":\"string\"}}}},\"required\":[\"ClassAsWrapperObject\"]}",json);

    }
}
