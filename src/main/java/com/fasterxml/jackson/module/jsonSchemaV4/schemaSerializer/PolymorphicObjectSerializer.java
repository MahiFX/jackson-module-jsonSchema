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
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicJsonFormatVisitorWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicObjectVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectSerializer extends SimpleSerializers {

    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Collection<PolymorphicSchemaUtil.NamedJavaType> subTypes = PolymorphicSchemaUtil.extractSubTypes(type, config, true);
        subTypes = subTypes.stream().filter(namedJavaType -> !namedJavaType.getJavaType().equals(type)).collect(Collectors.toList());

        //for container types there is no point to be polymorphic, the representation will be the same.
        if (subTypes.size() > 0 && !type.isContainerType()) {
            SchemaGenerationContext context = SchemaGenerationContext.get();
            if (!context.isVisitedAsPolymorphicType(type)) {
                return new PolyMorphicBeanSerializer();
            }
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
