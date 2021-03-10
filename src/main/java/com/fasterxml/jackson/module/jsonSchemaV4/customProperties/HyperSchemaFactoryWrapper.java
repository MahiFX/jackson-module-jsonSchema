package com.fasterxml.jackson.module.jsonSchemaV4.customProperties;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.annotation.JsonHyperSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.annotation.Link;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.ArrayVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.ObjectVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.types.LinkDescriptionObject;
import com.fasterxml.jackson.module.jsonSchemaV4.types.SimpleTypeSchema;

/**
 * Adds a hyperlink to object schema, either root level or nested. Generally
 * useful for writing additional properties to a schema.
 *
 * @author mavarazy
 */
public class HyperSchemaFactoryWrapper extends SchemaFactoryWrapper {

    private boolean ignoreDefaults = true;

    private HyperSchemaFactoryWrapper(){
    }

    public static class HyperSchemaFactoryWrapperFactory extends WrapperFactory {
        private boolean ignoreDefaults = true;

        public HyperSchemaFactoryWrapperFactory(boolean ignoreDefaults){
            this.ignoreDefaults=ignoreDefaults;
        }

        public HyperSchemaFactoryWrapperFactory(){
            this(false);
        }
        @Override
        public SchemaFactoryWrapper getWrapper(SerializerProvider provider) {
            HyperSchemaFactoryWrapper wrapper = new HyperSchemaFactoryWrapper();
            wrapper.setProvider(provider);
            wrapper.setIgnoreDefaults(ignoreDefaults);
            return wrapper;
        }
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
        ObjectVisitor visitor = ((ObjectVisitor) super.expectObjectFormat(convertedType));

        // could add other properties here
        addHyperlinks(visitor.getSchema(), convertedType);

        return visitor;
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType arrayType) {
        ArrayVisitor visitor = ((ArrayVisitor) super.expectArrayFormat(arrayType));

        // could add other properties here
        addHyperlinks(visitor.getSchema(), arrayType);

        return visitor;
    }

    public void setIgnoreDefaults(boolean ignoreDefaults) {
        this.ignoreDefaults = ignoreDefaults;
    }

    /**
     * Adds writes the type as the title of the schema.
     *
     * @param schema The schema who's title to set.
     * @param type   The type of the object represented by the schema.
     */
    private void addHyperlinks(JsonSchema schema, JavaType type) {
        if (!schema.isSimpleTypeSchema()) {
            throw new RuntimeException("given non simple type schema: " + schema.getType());
        }
        Class<?> rawClass = type.getRawClass();
        if (rawClass.isAnnotationPresent(JsonHyperSchema.class)) {
            JsonHyperSchema hyperSchema = rawClass.getAnnotation(JsonHyperSchema.class);
            String pathStart = hyperSchema.pathStart();
            Link[] links = hyperSchema.links();
            LinkDescriptionObject[] linkDescriptionObjects = new LinkDescriptionObject[links.length];
            for (int i = 0; i < links.length; i++) {
                Link link = links[i];
                linkDescriptionObjects[i] = new LinkDescriptionObject()
                        .setHref(pathStart + link.href())
                        .setRel(link.rel())
                        .setMethod(ignoreDefaults && "GET".equals(link.method()) ? null : link.method())
                        .setEnctype(ignoreDefaults && "application/json".equals(link.enctype()) ? null : link.enctype())
                        .setTargetSchema(fetchSchema(link.targetSchema()))
                        .setSchema(fetchSchema(link.schema()))
                        .setMediaType(ignoreDefaults && "application/json".equals(link.mediaType()) ? null : link.mediaType())
                        .setTitle(link.title());
            }
            SimpleTypeSchema simpleTypeSchema = schema.asSimpleTypeSchema();
            simpleTypeSchema.setLinks(linkDescriptionObjects);
            if (pathStart != null && pathStart.length() != 0)
                simpleTypeSchema.setPathStart(pathStart);
        }
    }

    private JsonSchema fetchSchema(Class<?> targetSchema) {
        if (getProvider() instanceof DefaultSerializerProvider && targetSchema != void.class) {
            JavaType targetType = getProvider().constructType(targetSchema);
            try {
                SchemaGenerationContext context = SchemaGenerationContext.get();
                if(context.isVisited(targetType,false)){
                    return context.getReferenceSchemaForVisitedType(targetType);
                }


                HyperSchemaFactoryWrapper targetVisitor = new HyperSchemaFactoryWrapper();


                ((DefaultSerializerProvider) getProvider()).acceptJsonFormatVisitor(targetType, targetVisitor);
                return targetVisitor.finalSchema();
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
