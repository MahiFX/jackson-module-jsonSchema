package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer.PolymorphicObjectSerializer;

import java.lang.reflect.Type;

/**
 * Convenience class that wraps JSON Schema generation functionality.
 *
 * @author tsaloranta
 */
public class JsonSchemaGenerator {

    protected final ObjectMapper _mapper;

    private final WrapperFactory _wrapperFactory;

    public JsonSchemaGenerator(ObjectMapper mapper) {
        this(mapper, null);
    }

    public JsonSchemaGenerator(ObjectMapper mapper, WrapperFactory wrapperFactory) {
        _mapper = mapper;
        _wrapperFactory = (wrapperFactory == null) ? new WrapperFactory() : wrapperFactory;
    }

    public JsonSchema generateSchema(Type type) throws JsonMappingException {
        return generateSchema(_mapper.constructType(type));
    }

    public JsonSchema generateSchema(JavaType type) throws JsonMappingException {
        ObjectMapper mapperToUser = _mapper.copy().setSerializerFactory(BeanSerializerFactory.instance.withAdditionalSerializers(new PolymorphicObjectSerializer()));
        SchemaFactoryWrapper visitor = _wrapperFactory.getWrapper(mapperToUser, null);
        mapperToUser.acceptJsonFormatVisitor(type, visitor);
        return visitor.finalSchema();
    }
}
