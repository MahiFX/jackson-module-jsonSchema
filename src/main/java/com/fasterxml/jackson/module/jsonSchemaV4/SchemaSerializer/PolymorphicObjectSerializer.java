package com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicJsonFormatVisitorWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.PolymorphicObjectVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicHandlingUtil;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.VisitorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class PolymorphicObjectSerializer extends SimpleSerializers {

    private final ObjectMapper originalMapper;

    public PolymorphicObjectSerializer(ObjectMapper originalMapper){
        this.originalMapper=originalMapper;
    }

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Collection<NamedType> subTypes=getSubTypes(config,beanDesc,type);
        if(!subTypes.isEmpty()){
            return new PolyMorphicBeanSerializer();
        }

        return super.findSerializer(config, type, beanDesc);
    }

    private Collection<NamedType> getSubTypes(SerializationConfig config, BeanDescription beanDesc,JavaType originalType) {

        AnnotatedClass classWithoutSuperType=AnnotatedClass.constructWithoutSuperTypes(beanDesc.getBeanClass(), config.getAnnotationIntrospector(), config);

        Collection<NamedType> namedTypes  = new ArrayList<NamedType>(config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, classWithoutSuperType));
        Iterator<NamedType> it = namedTypes.iterator();
        while(it.hasNext()){
            NamedType namedType = it.next();
            if(namedType.getType() == originalType.getRawClass()){
                it.remove();
                break;
            }
        }
        return namedTypes;
    }

    private class PolyMorphicBeanSerializer extends JsonSerializer

    {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            throw new UnsupportedOperationException("Can't serializer actual JSON object");
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
                throws JsonMappingException {
            //deposit your output format
            if (visitor == null || !(visitor instanceof PolymorphicJsonFormatVisitorWrapper)) {
                return;
            }

            PolymorphicJsonFormatVisitorWrapper polymorhpicVisitor = (PolymorphicJsonFormatVisitorWrapper) visitor;
            PolymorphicObjectVisitor objectVisitor = polymorhpicVisitor.expectPolyMorphicObjectFormat(typeHint);
            if (objectVisitor == null) {
                return;
            }
            objectVisitor.visitPolymorphicObject(typeHint,PolymorphicObjectSerializer.this.originalMapper);
        }
    }
}
