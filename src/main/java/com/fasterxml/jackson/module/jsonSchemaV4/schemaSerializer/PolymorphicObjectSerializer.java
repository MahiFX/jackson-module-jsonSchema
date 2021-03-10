package com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicJsonFormatVisitorWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicObjectVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectSerializer extends SimpleSerializers {

    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        boolean isPolyMorphic = isPolyMorphic(config, type);

        if (isPolyMorphic) {
            SchemaGenerationContext context = SchemaGenerationContext.get();
            if (!context.isVisitedAsPolymorphicType(type)) {
                return new PolyMorphicBeanSerializer();
            }
        }

        return null;
    }

    public static boolean isPolyMorphic(SerializationConfig config, JavaType type) {
        Collection<PolymorphicSchemaUtil.NamedJavaType> subTypes = PolymorphicSchemaUtil.extractSubTypes(type, config, true);
        subTypes = subTypes.stream().filter(namedJavaType -> !namedJavaType.getJavaType().equals(type)).collect(Collectors.toList());

        //for container types there is no point to be polymorphic, the representation will be the same.
        return subTypes.size() > 0 && !type.isContainerType();
    }

    public static class PolyMorphicBeanSerializer extends JsonSerializer {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {
            throw new UnsupportedOperationException("Can't serializer actual JSON object");
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
                throws JsonMappingException {
            if (!(visitor instanceof PolymorphicJsonFormatVisitorWrapper)) {
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
