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
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;
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
                    addValidationConstraints(getPropertySchema(writer), writer, getSchema());
                }
            }

            @Override
            public void property(BeanProperty writer) throws JsonMappingException {
                super.property(writer);
                if (getSchema() instanceof ObjectSchema) {
                    addValidationConstraints(getPropertySchema(writer), writer, getSchema());
                }
            }
        };
    }

    private void addValidationConstraints(JsonSchema propSchema, BeanProperty prop, JsonSchema parentSchema) {

        if (propSchema.isReferenceSchema() && propSchema.get$ref() != null) { // Some poly morph references get their ref set later
            String definitionKey = schemaGenerationContext.getDefinitionKeyForType(prop.getType());
            Map<String, JsonSchema> definitions = parentSchema.getDefinitions();
            if (definitions == null) {
                definitions = new HashMap<>();
                parentSchema.setDefinitions(definitions);
            }
            JsonSchema refSchema = definitions.get(definitionKey);

            if (refSchema != null) {
                // Some refs like the __NUMBER... have their definitions created at the end
                JsonSchema expectedSchema = refSchema.clone();
                addValidationConstraints(expectedSchema, prop, parentSchema);
                if (!expectedSchema.equals(refSchema)) {
                    String newId = "#" + definitionKey + ":" + prop.getName();
                    String newRef = DEFINITION_PREFIX + newId;
                    expectedSchema.setId(newRef);
                    definitions.put(newId, expectedSchema);
                    propSchema.set$ref(newRef);
                }
            }
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

}
