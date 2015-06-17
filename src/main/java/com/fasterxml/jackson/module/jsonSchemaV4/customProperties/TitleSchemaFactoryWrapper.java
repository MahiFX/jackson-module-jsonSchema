package com.fasterxml.jackson.module.jsonSchemaV4.customProperties;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.ArrayVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.ObjectVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;

/**
 * Adds a title to every object schema, either root level or nested. Generally
 * useful for writing additional properties to a schema.
 *
 * @author jphelan
 */
public class TitleSchemaFactoryWrapper extends SchemaFactoryWrapper {
    private static class TitleSchemaFactoryWrapperFactory extends WrapperFactory {
        @Override
        public SchemaFactoryWrapper getWrapper(ObjectMapper mapper, SerializerProvider p) {
            SchemaFactoryWrapper wrapper = new TitleSchemaFactoryWrapper(mapper);
            if (p != null) {
                wrapper.setProvider(p);
            }
            return wrapper;
        }

        ;

        @Override
        public SchemaFactoryWrapper getWrapper(ObjectMapper mapper, SerializerProvider p, VisitorContext rvc) {
            SchemaFactoryWrapper wrapper = new TitleSchemaFactoryWrapper(mapper);
            if (p != null) {
                wrapper.setProvider(p);
            }
            wrapper.setVisitorContext(rvc);
            return wrapper;
        }
    }

    ;

    public TitleSchemaFactoryWrapper(ObjectMapper mapper) {
        super(mapper, new TitleSchemaFactoryWrapperFactory());
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
        ObjectVisitor visitor = ((ObjectVisitor) super.expectObjectFormat(convertedType));

        // could add other properties here
        addTitle(visitor.getSchema(), convertedType);

        return visitor;
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType convertedType) {
        ArrayVisitor visitor = ((ArrayVisitor) super.expectArrayFormat(convertedType));

        // could add other properties here
        addTitle(visitor.getSchema(), convertedType);

        return visitor;
    }

    /**
     * Adds writes the type as the title of the schema.
     *
     * @param schema The schema who's title to set.
     * @param type   The type of the object represented by the schema.
     */
    private void addTitle(JsonSchema schema, JavaType type) {
        if (!schema.isSimpleTypeSchema()) {
            throw new RuntimeException("given non simple type schema: " + schema.getType());
        }
        schema.asSimpleTypeSchema().setTitle(type.getGenericSignature());
    }
}
