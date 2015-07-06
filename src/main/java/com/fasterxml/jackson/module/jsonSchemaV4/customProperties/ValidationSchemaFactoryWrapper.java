package com.fasterxml.jackson.module.jsonSchemaV4.customProperties;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.ObjectVisitor;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.ObjectVisitorDecorator;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.validation.AnnotationConstraintResolver;
import com.fasterxml.jackson.module.jsonSchemaV4.validation.ValidationConstraintResolver;

/**
 * @author cponomaryov
 */
public class ValidationSchemaFactoryWrapper extends SchemaFactoryWrapper {

    private ValidationConstraintResolver constraintResolver;

    public static class ValidationSchemaFactoryWrapperFactory extends WrapperFactory {
        private ValidationConstraintResolver constraintResolver;

        public ValidationSchemaFactoryWrapperFactory(ValidationConstraintResolver constraintResolver){
            this.constraintResolver=constraintResolver;
        }

        public ValidationSchemaFactoryWrapperFactory(){
            this(new AnnotationConstraintResolver());
        }
        @Override
        public SchemaFactoryWrapper getWrapper(SerializerProvider provider) {
            ValidationSchemaFactoryWrapper schemaFactoryWrapper =new ValidationSchemaFactoryWrapper(constraintResolver);
            schemaFactoryWrapper.setProvider(provider);
            return  schemaFactoryWrapper;

        }
    }


    private  ValidationSchemaFactoryWrapper(ValidationConstraintResolver constraintResolver) {
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
                if(getSchema() instanceof ObjectSchema) {
                    addValidationConstraints(getPropertySchema(writer), writer);
                }
            }

            @Override
            public void property(BeanProperty writer) throws JsonMappingException {
                super.property(writer);
                if(getSchema() instanceof ObjectSchema) {
                    addValidationConstraints(getPropertySchema(writer), writer);
                }
            }
        };
    }

    private JsonSchema addValidationConstraints(JsonSchema schema, BeanProperty prop) {
        if (schema.isArraySchema()) {
            ArraySchema arraySchema = schema.asArraySchema();
            arraySchema.setMaxItems(constraintResolver.getArrayMaxItems(prop));
            arraySchema.setMinItems(constraintResolver.getArrayMinItems(prop));
        } else if (schema.isNumberSchema()) {
            NumberSchema numberSchema = schema.asNumberSchema();
            numberSchema.setMaximum(constraintResolver.getNumberMaximum(prop));
            numberSchema.setMinimum(constraintResolver.getNumberMinimum(prop));
        } else if (schema.isStringSchema()) {
            StringSchema stringSchema = schema.asStringSchema();
            stringSchema.setMaxLength(constraintResolver.getStringMaxLength(prop));
            stringSchema.setMinLength(constraintResolver.getStringMinLength(prop));
            stringSchema.setPattern(constraintResolver.getStringPattern(prop));
        }
        return schema;
    }

}
