package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer.PolymorphicObjectSerializer;
import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;

import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.schema;
import static com.fasterxml.jackson.module.jsonSchemaV4.Utils.toJson;
import static com.fasterxml.jackson.module.jsonSchemaV4.PolymorphicTypeTest.*;

/**
 * Created by zoliszel on 09/06/2015.
 */

public class PolymorphicTypeWithMixInsTest {
    private ObjectMapper mapper;

    @Before
    public void setup() {

        mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
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
        verifyDefinitions(schema);
        Assert.assertTrue("Resulting schema should be an array schema",schema.isArraySchema());
        Assert.assertNotNull("Items should have a reference",schema.asArraySchema().getItems().asSingleItems().getSchema().get$ref());

    }

    private void verifyDefinitions(JsonSchema schema) {
        containsDefinitions(schema, Sets.newHashSet(CompanyMixIn.TYPE_NAME,CompanyMixIn.TYPE_NAME + PolymorphicHandlingUtil.POLYMORPHIC_TYPE_NAME_SUFFIX,PersonMixIn.TYPE_NAME,BigCompanyMixIn.TYPE_NAME,JSONSubTypeBaseClassWithMixIns.class.getSimpleName()));
        JsonSchema jsonSubTypeBaseClassSchema = schema.getDefinitions().get(JSONSubTypeBaseClassWithMixIns.class.getSimpleName());
        Assert.assertTrue("JsonSubTypeBaseClass should be polymorphic", jsonSubTypeBaseClassSchema.isPolymorhpicObjectSchema());
        ReferenceSchema[] refSchema =  jsonSubTypeBaseClassSchema.asPolymorphicObjectSchema().getAnyOf();
        verifyAnyOfContent(refSchema, Sets.newHashSet(PersonMixIn.TYPE_NAME, CompanyMixIn.TYPE_NAME, BigCompanyMixIn.TYPE_NAME));
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
        verifyDefinitions(schema);
        Assert.assertNotNull("Reference should not be null",schema.get$ref());
    }


    /*
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
    */

}
