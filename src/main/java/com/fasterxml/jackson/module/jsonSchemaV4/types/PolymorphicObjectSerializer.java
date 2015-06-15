package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicJsonFormatVisitorWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicObjectVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.VisitorUtils;

import java.io.IOException;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectSerializer extends SimpleSerializers {

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        if (VisitorUtils.isPolymorphic(type.getRawClass())) {
            return new PolyMorphicBeanSerializer();
        }
        return super.findSerializer(config, type, beanDesc);
    }

    private static class PolyMorphicBeanSerializer extends JsonSerializer

    {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            throw new UnsupportedOperationException("Can't serializer actual JSON object");
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
                throws JsonMappingException {
            //deposit your output format
            if (visitor == null || !VisitorUtils.isPolymorphic(typeHint.getRawClass()) || !(visitor instanceof PolymorphicJsonFormatVisitorWrapper)) {
                return;
            }

            PolymorphicJsonFormatVisitorWrapper polymorhpicVisitor = (PolymorphicJsonFormatVisitorWrapper) visitor;
            PolymorphicObjectVisitor objectVisitor = polymorhpicVisitor.expectPolyMorphicObjectFormat(typeHint);
            if (objectVisitor == null) {
                return;
            }
            objectVisitor.polymorphic(typeHint);
        }
    }
}
