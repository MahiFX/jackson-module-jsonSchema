package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchemaV4.customProperties.HyperSchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.FormatVisitorFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.schemaSerializer.PolymorphicObjectSerializer;

import java.lang.reflect.Type;

/**
 * Main entrypoint for the V4 generator. Creation is handled through @see JsonSchemaGenerator.Builder()
 *
 * @author zoliszel
 */
public class JsonSchemaGenerator {

    public static final String SCHEMA_V4="http://json-schema.org/draft-04/schema#";

    public static final String HYPER_SCHEMA_V4="http://json-schema.org/draft-04/hyper-schema#";


    /**
     * Builder class to create JsonSchema generator. Usage:
     * JsonSchemaGenerator generator=new JsonSchemaGenerator.Builder().build();
     * generator.generateSchema(SomeClass.class)
     */
    public static class Builder{
        protected ObjectMapper mapper;

        protected WrapperFactory wrapperFactory;

        protected FormatVisitorFactory visitorFactory;

        protected JsonSchemaFactory schemaProvider;

        protected boolean withSchemaType;

        protected boolean withAdditonalProperties=true;

        /**
         * Optional
         * @param Use the ObjectMapper provided. Please note additional serializers will be added to the mapper
         *            during the creation process. 1 Mapper can only be used with one JsonSchemaGenerator
         *            but the JsonSchema generator is reusable(recommended)
         * @return this
         */
        public Builder withObjectMapper(ObjectMapper mapper){
            this.mapper =mapper;
            return this;
        }

        /**
         * Optional
         * @param wrapperFactory to use. By default it will use SchemaFactoryWrapperFactory()
         * @return this
         */
        public Builder withWrapperFactory(WrapperFactory wrapperFactory){
            this.wrapperFactory =wrapperFactory;
            return this;
        }

        /**
         * Optional
         * @param visitorFactory to use.
         * @return
         */
        public Builder withFormatVisitorFactory(FormatVisitorFactory visitorFactory){
            this.visitorFactory=visitorFactory;
            return this;
        }

        /**
         * Optional
         * @param schemaProvider to use
         * @return
         */
        public Builder withJsonSchemaFactory(JsonSchemaFactory schemaProvider){
            this.schemaProvider=schemaProvider;
            return this;
        }

        /**
         * Optional boolean to include the schema version used in the created document. Default to false
         * @param withIncludeJsonSchemaVersion
         * @return
         */
        public Builder withIncludeJsonSchemaVersion(boolean withIncludeJsonSchemaVersion){
            this.withSchemaType=withIncludeJsonSchemaVersion;
            return this;
        }


        /**
         * Optional boolean to say if object types allow additonal properties or not (additionalProperties = false | true).
         * defaults to true
         * @param withAdditonalProperties
         * @return
         */
        public Builder withAdditonalProperties(boolean withAdditonalProperties){
            this.withAdditonalProperties =withAdditonalProperties;
            return this;
        }

        /**
         * @return constructed builder
         */
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
            return new JsonSchemaGenerator(mapper, wrapperFactory,visitorFactory,schemaProvider,withSchemaType, withAdditonalProperties);

        }

    }
    protected final ObjectMapper mapper;

    protected final WrapperFactory wrapperFactory;

    protected final FormatVisitorFactory visitorFactory;

    protected final JsonSchemaFactory schemaProvider;

    protected final boolean withSchemaType;

    protected final boolean withAdditionalProperties;


    private JsonSchemaGenerator(ObjectMapper mapper, WrapperFactory wrapperFactory,FormatVisitorFactory visitorFactory,JsonSchemaFactory schemaProvider,boolean withSchemaType,boolean withAdditionalProperties) {
        this.mapper = mapper;
        this.wrapperFactory = wrapperFactory;
        this.visitorFactory=visitorFactory;
        this.schemaProvider=schemaProvider;
        this.withSchemaType=withSchemaType;
        this.withAdditionalProperties = withAdditionalProperties;
    }

    public JsonSchema generateSchema(Type type) throws JsonMappingException {
        return generateSchema(mapper.constructType(type));
    }

    public JsonSchema generateSchema(JavaType type) throws JsonMappingException {
        if(SchemaGenerationContext.get()!=null){
            throw new IllegalStateException("Generation is already in progress in this thread. JsonSchemaGenerator doesn't support recursive calls");
        }
        try {
            SchemaGenerationContext generationContext = new SchemaGenerationContext(visitorFactory, schemaProvider, wrapperFactory, mapper, withAdditionalProperties);
            SchemaGenerationContext.set(generationContext);
            SchemaFactoryWrapper visitor = wrapperFactory.getWrapper(null);
            mapper.acceptJsonFormatVisitor(type, visitor);
            JsonSchema schema = visitor.finalSchema();
            if (withSchemaType) {
                if (visitor instanceof HyperSchemaFactoryWrapper) {
                    schema.set$schema(HYPER_SCHEMA_V4);
                } else {
                    schema.set$schema(SCHEMA_V4);
                }
            }
            return schema;
        }
        finally {
            SchemaGenerationContext.set(null);
        }
    }

    public String schemaAsString(Type type) throws JsonProcessingException {
        return schemaAsString(generateSchema(type),false);
    }

    public String schemaAsString(Type type,boolean prettyPrint) throws JsonProcessingException {
        return schemaAsString(generateSchema(type),prettyPrint);
    }

    public String schemaAsString(JavaType type) throws JsonProcessingException {
        return schemaAsString(generateSchema(type),false);
    }

    public String schemaAsString(JavaType type,boolean prettyPrint) throws JsonProcessingException {
        return schemaAsString(generateSchema(type),prettyPrint);
    }

    public String schemaAsString(JsonSchema schema) throws JsonProcessingException {
        return schemaAsString(schema,false);
    }


    public String schemaAsString(JsonSchema jsonSchema,boolean prettyPrint) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        if(prettyPrint){
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema);
        }
        else{
            return mapper.writeValueAsString(jsonSchema);
        }

    }
}
