package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.FormatVisitorFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.JsonSchemaFactory;
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

    public static class Builder{
        protected ObjectMapper mapper;

        protected WrapperFactory wrapperFactory;

        protected FormatVisitorFactory visitorFactory;

        protected JsonSchemaFactory schemaProvider;

        public Builder withObjectMapper(ObjectMapper mapper){
            this.mapper =mapper;
            return this;
        }

        public Builder withWrapperFactory(WrapperFactory wrapperFactory){
            this.wrapperFactory =wrapperFactory;
            return this;
        }

        public Builder withFormatVisitorFactory(FormatVisitorFactory visitorFactory){
            this.visitorFactory=visitorFactory;
            return this;
        }

        public Builder withJsonSchemaFactory(JsonSchemaFactory schemaProvider){
            this.schemaProvider=schemaProvider;
            return this;
        }

        public JsonSchemaGenerator build(){
            if(this.mapper ==null){
                mapper = new ObjectMapper();
            }
            mapper.setSerializerFactory(mapper.getSerializerFactory().withAdditionalSerializers(new PolymorphicObjectSerializer()));
            if(this.wrapperFactory ==null){
                this.wrapperFactory = new WrapperFactory();
            }
            if(this.visitorFactory ==null){
                this.visitorFactory = new FormatVisitorFactory();
            }
            if(this.schemaProvider==null){
                this.schemaProvider = new JsonSchemaFactory();
            }
            return new JsonSchemaGenerator(mapper, wrapperFactory,visitorFactory,schemaProvider);

        }

    }
    protected final ObjectMapper mapper;

    protected final WrapperFactory wrapperFactory;

    protected final FormatVisitorFactory visitorFactory;

    protected final JsonSchemaFactory schemaProvider;


    private JsonSchemaGenerator(ObjectMapper mapper, WrapperFactory wrapperFactory,FormatVisitorFactory visitorFactory,JsonSchemaFactory schemaProvider) {
        this.mapper = mapper;
        this.wrapperFactory = wrapperFactory;
        this.visitorFactory=visitorFactory;
        this.schemaProvider=schemaProvider;
    }

    public JsonSchema generateSchema(Type type) throws JsonMappingException {
        return generateSchema(mapper.constructType(type));
    }

    public JsonSchema generateSchema(JavaType type) throws JsonMappingException {
        if(SchemaGenerationContext.get()!=null){
            throw new IllegalStateException("Generation is already in progress in this thread. JsonSchemaGenerator doesn't support recursive calls");
        }
        try {
            SchemaGenerationContext generationContext = new SchemaGenerationContext(visitorFactory,schemaProvider, wrapperFactory, mapper);
            SchemaGenerationContext.set(generationContext);
            SchemaFactoryWrapper visitor = wrapperFactory.getWrapper(null);
            mapper.acceptJsonFormatVisitor(type, visitor);
            return visitor.finalSchema();
        }
        finally {
            SchemaGenerationContext.set(null);
        }
    }

    public String schemaAsString(JavaType type) throws JsonProcessingException {
        return schemaAsString(generateSchema(type),false);
    }

    public String schemaAsString(JavaType type,boolean prettyPrint) throws JsonProcessingException {
        return schemaAsString(generateSchema(type),prettyPrint);
    }


    private String schemaAsString(JsonSchema jsonSchema,boolean prettyPrint) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        if(prettyPrint){
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema);
        }
        else{
            return mapper.writeValueAsString(jsonSchema);
        }

    }
}
