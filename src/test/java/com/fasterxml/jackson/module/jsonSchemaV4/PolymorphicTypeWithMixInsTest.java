package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer.PolymorphicObjectSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;

import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.schema;
import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.toJson;

/**
 * Created by zoliszel on 09/06/2015.
 */

public class PolymorphicTypeWithMixInsTest {
    private ObjectMapper mapper;

    @Before
    public void setup() {

        mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        mapper.setSerializerFactory(BeanSerializerFactory.instance.withAdditionalSerializers(new PolymorphicObjectSerializer()));
        mapper.addMixIn(JSONSubTypeBaseClassWithMixIns.class, BaseMixIn.class);
        mapper.addMixIn(PersonMixIns.class, PersonMixIn.class);
        mapper.addMixIn(CompanyMixIns.class, CompanyMixIn.class);
        mapper.addMixIn(BigCompanyMixIns.class, BigCompanyMixIn.class);
    }


    @Test
    public void polymorphicSchemaGenerationArray() {
        Type type = JSONSubTypeBaseClassWithMixIns[].class;
        JsonSchema schema = schema(type, mapper);
        String json = toJson(schema, schema.getClass(), new ObjectMapper());
        System.out.println(json);
        Assert.assertNotNull("definitions should have been added", schema.getDefinitions());
        Assert.assertEquals("there should be 4 sub schema", 4, schema.getDefinitions().entrySet().size());
        Assert.assertTrue("Found no CompanyMixIn schema", schema.getDefinitions().containsKey(CompanyMixIn.TYPE_NAME));
        Assert.assertTrue("Found no CompanyMixIn_1 schema", schema.getDefinitions().containsKey(CompanyMixIn.TYPE_NAME + PolymorphicHandlingUtil.POLYMORPHIC_TYPE_NAME_SUFFIX));
        Assert.assertTrue("Found no PersonMixIn schema", schema.getDefinitions().containsKey(PersonMixIn.TYPE_NAME));
        Assert.assertTrue("Found no BigCompanyMixIn schema", schema.getDefinitions().containsKey(BigCompanyMixIn.TYPE_NAME));
        Assert.assertTrue("Array items should be a one of schema", schema.asArraySchema().getItems().asSingleItems().getSchema() instanceof AnyOfSchema);
        Assert.assertTrue("Any OF Schema Should Contain CompanyMixIn Reference", containsReference(((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema()).getAnyOf(), PersonMixIn.TYPE_NAME));
        Assert.assertTrue("Any OF Schema Should Contain PersonMixIn Reference", containsReference(((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema()).getAnyOf(), CompanyMixIn.TYPE_NAME));
        Assert.assertTrue("Any OF Schema Should Contain BigCompanyMixIn Reference", containsReference(((AnyOfSchema) schema.asArraySchema().getItems().asSingleItems().getSchema()).getAnyOf(), BigCompanyMixIn.TYPE_NAME));
        Assert.assertNotNull("Person schema should have required fields", schema.getDefinitions().get(PersonMixIn.TYPE_NAME).asObjectSchema().getRequired());
        Assert.assertTrue("name property is required for person", schema.getDefinitions().get(PersonMixIn.TYPE_NAME).asObjectSchema().getRequired().contains("name"));
        Assert.assertTrue("name property is required for person", schema.getDefinitions().get(PersonMixIn.TYPE_NAME).asObjectSchema().getRequired().contains("dateOfBirth"));
    }

    @Test
    public void polymorphicSchemaGenerationObject() {
        Type type = JSONSubTypeBaseClassWithMixIns.class;
        JsonSchema schema = schema(type, mapper);
        String json = toJson(schema, schema.getClass(), new ObjectMapper());
        System.out.println(json);
        Assert.assertNotNull("definitions should have been added", schema.getDefinitions());
        Assert.assertEquals("there should be 4 sub schema", 4, schema.getDefinitions().entrySet().size());
        Assert.assertTrue("Found no CompanyMixIn schema", schema.getDefinitions().containsKey(CompanyMixIn.TYPE_NAME));
        Assert.assertTrue("Found no CompanyMixIn_1 schema", schema.getDefinitions().containsKey(CompanyMixIn.TYPE_NAME + PolymorphicHandlingUtil.POLYMORPHIC_TYPE_NAME_SUFFIX));
        Assert.assertTrue("Found no PersonMixIn schema", schema.getDefinitions().containsKey(PersonMixIn.TYPE_NAME));
        Assert.assertTrue("Found no BigCompanyMixIn schema", schema.getDefinitions().containsKey(BigCompanyMixIn.TYPE_NAME));
        Assert.assertTrue("Expected polymorphicObject", schema instanceof PolymorphicObjectSchema);
        Assert.assertTrue("PolymorphicSchema should contain CompanyMixIn Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), PersonMixIn.TYPE_NAME));
        Assert.assertTrue("PolymorphicSchema should contain PersonMixIn Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), CompanyMixIn.TYPE_NAME));
        Assert.assertTrue("PolymorphicSchema should contain BigCompanyMixIn Reference", containsReference(((PolymorphicObjectSchema) schema).getAnyOf(), BigCompanyMixIn.TYPE_NAME));
        Assert.assertTrue("name property is required for person", schema.getDefinitions().get(PersonMixIn.TYPE_NAME).asObjectSchema().getRequired().contains("name"));
        Assert.assertTrue("name property is required for person", schema.getDefinitions().get(PersonMixIn.TYPE_NAME).asObjectSchema().getRequired().contains("dateOfBirth"));


    }


    private boolean containsReference(ReferenceSchema[] refSchemas, String name) {
        if (refSchemas == null) {
            return false;
        }
        for (ReferenceSchema refSchema : refSchemas) {
            if (("#/definitions/" + name).equals(refSchema.get$ref())) {
                return true;
            }
            if (refSchema.get$ref().contains(name)) {
                return true;
            }
        }
        return false;
    }

}
