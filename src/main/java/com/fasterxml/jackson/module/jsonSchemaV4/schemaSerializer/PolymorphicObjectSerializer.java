package com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicJsonFormatVisitorWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicObjectVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectSerializer extends SimpleSerializers {

    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Collection<NamedType> subTypes = PolymorphicHandlingUtil.extractSubTypes(beanDesc.getBeanClass(), config);
        if (subTypes.size() > 1 && SchemaGenerationContext.get().addVisitedPolymorphicType(type)) {//subtype is inclusive with itself.
            return new PolyMorphicBeanSerializer();
        }

        return null;
    }

    private static class PolyMorphicBeanSerializer extends JsonSerializer {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            throw new UnsupportedOperationException("Can't serializer actual JSON object");
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
                throws JsonMappingException {
            if (visitor == null || !(visitor instanceof PolymorphicJsonFormatVisitorWrapper)) {
                return;
            }

            PolymorphicJsonFormatVisitorWrapper polymorhpicVisitor = (PolymorphicJsonFormatVisitorWrapper) visitor;
            PolymorphicObjectVisitor objectVisitor = polymorhpicVisitor.expectPolyMorphicObjectFormat(typeHint);
            if (objectVisitor == null) {
                return;
            }
            objectVisitor.visitPolymorphicObject(typeHint);
        }
    }
}
