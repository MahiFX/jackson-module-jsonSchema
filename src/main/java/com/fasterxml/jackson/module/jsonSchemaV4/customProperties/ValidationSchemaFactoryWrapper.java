package com.fasterxml.jackson.module.jsonSchemaV4.customProperties;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.ObjectVisitorDecorator;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.types.*;
import com.fasterxml.jackson.module.jsonSchemaV4.validation.AnnotationConstraintResolver;
import com.fasterxml.jackson.module.jsonSchemaV4.validation.ValidationConstraintResolver;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.module.jsonSchemaV4.factories.utils.PolymorphicSchemaUtil.DEFINITION_PREFIX;

/**
 * @author cponomaryov
 */
public class ValidationSchemaFactoryWrapper extends SchemaFactoryWrapper {

    private ValidationConstraintResolver constraintResolver;

    public static class ValidationSchemaFactoryWrapperFactory extends WrapperFactory {
        private ValidationConstraintResolver constraintResolver;

        public ValidationSchemaFactoryWrapperFactory(ValidationConstraintResolver constraintResolver) {
            this.constraintResolver = constraintResolver;
        }

        public ValidationSchemaFactoryWrapperFactory() {
            this(new AnnotationConstraintResolver());
        }

        @Override
        public SchemaFactoryWrapper getWrapper(SerializerProvider provider) {
            ValidationSchemaFactoryWrapper schemaFactoryWrapper = new ValidationSchemaFactoryWrapper(constraintResolver);
            schemaFactoryWrapper.setProvider(provider);
            return schemaFactoryWrapper;

        }
    }


    private ValidationSchemaFactoryWrapper(ValidationConstraintResolver constraintResolver) {
        super();
        this.constraintResolver = constraintResolver;
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType) {
        return new ObjectVisitorDecorator(super.expectObjectFormat(convertedType)) {
            private JsonSchema getPropertySchema(BeanProperty writer) {
                return ((ObjectSchema) getSchema()).getProperties().get(writer.getName());
            }

            @Override
            public void optionalProperty(BeanProperty writer) throws JsonMappingException {
                super.optionalProperty(writer);
                if (getSchema() instanceof ObjectSchema) {
                    JsonSchema propertySchema = getPropertySchema(writer);
                    addValidationConstraints(propertySchema, writer, getSchema(), propertySchema.getDefinitions());
                }
            }

            @Override
            public void property(BeanProperty writer) throws JsonMappingException {
                super.property(writer);
                if (getSchema() instanceof ObjectSchema) {
                    JsonSchema propertySchema = getPropertySchema(writer);
                    addValidationConstraints(propertySchema, writer, getSchema(), propertySchema.getDefinitions());
                }
            }
        };
    }

    private void addValidationConstraints(JsonSchema propSchema, BeanProperty prop, JsonSchema parentSchema, Map<String, JsonSchema> definitions) {

        if (propSchema.isReferenceSchema() && propSchema.get$ref() != null) { // Some poly morph references get their ref set later
            String definitionKey = propSchema.get$ref().replace("#/definitions/", "");
            JsonSchema refSchema = null;
            if (definitions != null) {
                refSchema = definitions.get(definitionKey);
            }
            if (refSchema == null) {
                // Check parent schema for the definition
                definitions = parentSchema.getDefinitions();
                if (definitions == null) {
                    definitions = new HashMap<>();
                    parentSchema.setDefinitions(definitions);
                }
                refSchema = definitions.get(definitionKey);
            }
            if (refSchema != null) {
                // Some refs like the __NUMBER... have their definitions created at the end
                JsonSchema expectedSchema = refSchema.clone();
                addValidationConstraints(expectedSchema, prop, parentSchema, definitions);
                if (!expectedSchema.equals(refSchema)) {
                    String newId = schemaGenerationContext.getIdForType(prop.getType()) + ":" + prop.getName();
                    String newDefKey = definitionKey + ":" + prop.getName();
                    String newRef = DEFINITION_PREFIX + newDefKey;
                    expectedSchema.setId(newId);
                    definitions.put(newDefKey, expectedSchema);
                    propSchema.set$ref(newRef);
                }
            }
        } else if (propSchema.isPolymorhpicObjectSchema()) {
            PolymorphicObjectSchema polymorphicObjectSchema = propSchema.asPolymorphicObjectSchema();
            addValidationConstraints(propSchema, prop, polymorphicObjectSchema.getAnyOf(), definitions);
            addValidationConstraints(propSchema, prop, polymorphicObjectSchema.getOneOf(), definitions);
            addValidationConstraints(propSchema, prop, polymorphicObjectSchema.getAllOf(), definitions);
        } else if (propSchema.isArraySchema()) {
            ArraySchema arraySchema = propSchema.asArraySchema();
            arraySchema.setMaxItems(constraintResolver.getArrayMaxItems(prop));
            arraySchema.setMinItems(constraintResolver.getArrayMinItems(prop));
        } else if (propSchema.isNumberSchema()) {
            NumberSchema numberSchema = propSchema.asNumberSchema();
            numberSchema.setMaximum(constraintResolver.getNumberMaximum(prop));
            numberSchema.setMinimum(constraintResolver.getNumberMinimum(prop));
        } else if (propSchema.isStringSchema()) {
            StringSchema stringSchema = propSchema.asStringSchema();
            stringSchema.setMaxLength(constraintResolver.getStringMaxLength(prop));
            stringSchema.setMinLength(constraintResolver.getStringMinLength(prop));
            stringSchema.setPattern(constraintResolver.getStringPattern(prop));
        }
    }

    private void addValidationConstraints(JsonSchema propSchema, BeanProperty prop, JsonSchema[] schemas, Map<String, JsonSchema> definitions) {
        if (schemas != null) {
            for (JsonSchema subType : schemas) {
                addValidationConstraints(subType, prop, propSchema, definitions);
            }
        }
    }

}
