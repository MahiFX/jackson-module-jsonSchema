package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zoliszel on 12/06/2015.
 */
public class VisitorUtils {

    public VisitorUtils(VisitorContext visitorContext) {
        this.visitorContext = visitorContext;
        mapper = new ObjectMapper();
        visitor = new SchemaFactoryWrapper();
        visitor.setVisitorContext(visitorContext);
    }

    public static boolean isPolymorphic(Class<?> clazz) {
        return clazz.isAnnotationPresent(JsonSubTypes.class);
    }

    protected ObjectMapper mapper;

    protected SchemaFactoryWrapper visitor;

    protected VisitorContext visitorContext;

    public interface PolymorphiSchemaDefinition {
        public Map<String, JsonSchema> getDefinitions();

        public ReferenceSchema[] getReferences();

    }

    public PolymorphiSchemaDefinition extractPolymophicTypes(Class<?> clazz) {
        if (!isPolymorphic(clazz)) {
            throw new IllegalArgumentException("Argument is not polymorphic (no JsonSubtype annotation (" + clazz.getSimpleName() + ")");
        }

        Class<?>[] polymorphicTypes = extractPolymorphicTypes(clazz).toArray(new Class<?>[0]);
        final ReferenceSchema[] references = new ReferenceSchema[polymorphicTypes.length];
        JsonSchema[] subSchemas = new JsonSchema[polymorphicTypes.length];
        final Map<String, JsonSchema> definitions = new HashMap<String, JsonSchema>();

        for (int i = 0; i < polymorphicTypes.length; ++i) {
            references[i] = new ReferenceSchema(getDefinitionReference(polymorphicTypes[i]));
            subSchemas[i] = schema(polymorphicTypes[i]);
            definitions.put(getJsonTypeName(polymorphicTypes[i]), subSchemas[i]);
        }

        return new PolymorphiSchemaDefinition() {
            @Override
            public Map<String, JsonSchema> getDefinitions() {
                return definitions;
            }

            @Override
            public ReferenceSchema[] getReferences() {
                return references;
            }
        };
    }

    protected String getDefinitionReference(Class modelClass) {


        if (visitorContext != null) {
            return visitorContext.javaTypeToUrn(mapper.constructType(modelClass));
        } else {
            return "#/definitions/" + getJsonTypeName(modelClass);
        }

    }

    protected String getJsonTypeName(Class modelClass) {
        if (modelClass.isAnnotationPresent(JsonTypeName.class)) {
            return ((JsonTypeName) modelClass.getAnnotation(JsonTypeName.class)).value();
        }
        return modelClass.getSimpleName();
    }

    protected List<Class<?>> extractPolymorphicTypes(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(JsonSubTypes.class)) {
            return Collections.emptyList();
        }

        JsonSubTypes subTypes = clazz.getAnnotation(JsonSubTypes.class);
        if (subTypes.value() == null || subTypes.value().length == 0) {
            throw new IllegalStateException("Found class (" + clazz.getSimpleName() + ") with empty JsonSubTypes annotation");
        }

        List<Class<?>> javaSubTypes = new ArrayList<Class<?>>();
        for (int i = 0; i < subTypes.value().length; ++i) {
            javaSubTypes.add(subTypes.value()[i].value());
            javaSubTypes.addAll(extractPolymorphicTypes(subTypes.value()[i].value()));

        }
        return javaSubTypes;
    }

    protected JsonSchema schema(Type t) {
        if (visitorContext != null) {
            if (visitorContext != null) {
                String seenSchemaUri = visitorContext.getSeenSchemaUri(mapper.constructType(t));
                if (seenSchemaUri != null) {
                    return new ReferenceSchema(seenSchemaUri);
                }
            }
        }
        try {

            mapper.acceptJsonFormatVisitor(mapper.constructType(t), visitor);
            return visitor.finalSchema();
        } catch (JsonMappingException e) {
            //TODO throw and sort out exception
            return null;
        }
    }
}
