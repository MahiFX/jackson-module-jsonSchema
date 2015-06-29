package com.fasterxml.jackson.module.jsonSchemaV4.factories.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.module.jsonSchemaV4.SchemaGenerationContext;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.AnyOfSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.NumberSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.PolymorphicObjectSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.StringSchema;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by zoliszel on 16/06/2015.
 */
public class PolymorphicHandlingUtil {

    private static final Set<String>  ALLOWED_NON_NUMERIC_VALUES = new HashSet<String>(Arrays.asList("INF", "Infinity", "-INF", "-Infinity", "NaN"));

    public static final String POLYMORPHIC_TYPE_NAME_SUFFIX = "_1";

    public static final String NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE ="___NUMBER___";

    public static final String NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE ="__ALLOWED_NON_NUMERIC_VALUES___";

    public static final String NUMBER_WITH_NON_NUMERIC_VALUES="___NUMBER_WTIH_NON_NUMERIC_VALUES___";


    private static final String DEFINITION_PREFIX = "#/definitions/";


    private static JsonSchema getNumberSchemaWithAllowedString(){
        NumberSchema numberSchema = new NumberSchema();
        numberSchema.setId(SchemaGenerationContext.javaTypeToUrn(NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE));

        StringSchema allowedStringValues = new StringSchema();
        allowedStringValues.setId(SchemaGenerationContext.javaTypeToUrn(NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE));
        allowedStringValues.setEnums(ALLOWED_NON_NUMERIC_VALUES);

        PolymorphicObjectSchema wrappedSchema = new PolymorphicObjectSchema();
        wrappedSchema.setDefinitions(new HashMap<String, JsonSchema>());
        wrappedSchema.getDefinitions().put(NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE,numberSchema);
        wrappedSchema.getDefinitions().put(NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE,allowedStringValues);

        ReferenceSchema numberReference = new ReferenceSchema(DEFINITION_PREFIX+ NUMBER_WITH_NON_NUMERIC_NUMBER_REFERENCE,numberSchema.getType());
        ReferenceSchema nonNumericValuesSchema =  new ReferenceSchema(DEFINITION_PREFIX+ NUMBER_WITH_NON_NUMERIC_ALLOWED_STRING_VALUES_REFERENCE,allowedStringValues.getType());

        wrappedSchema.setOneOf(new ReferenceSchema[]{numberReference,nonNumericValuesSchema});
        wrappedSchema.setTypes(new JsonFormatTypes[]{JsonFormatTypes.NUMBER,JsonFormatTypes.STRING});
        wrappedSchema.setId(SchemaGenerationContext.javaTypeToUrn(NUMBER_WITH_NON_NUMERIC_VALUES));

        ReferenceSchema referenceSchema = new ReferenceSchema(DEFINITION_PREFIX +NUMBER_WITH_NON_NUMERIC_VALUES,wrappedSchema.getType());
        referenceSchema.setDefinitions(new HashMap<String, JsonSchema>());
        referenceSchema.getDefinitions().put(NUMBER_WITH_NON_NUMERIC_VALUES,wrappedSchema);
        return referenceSchema;
    }

    private final SerializerProvider provider;

    public interface PolymorphiSchemaDefinition {
        Map<String, JsonSchema> getDefinitions();

        ReferenceSchema[] getReferences();
    }

    protected final VisitorUtils visitorUtils;

    protected final JavaType originalType;

    protected BeanDescription beanDescription;

    private Collection<NamedType> subTypes = Collections.emptyList();

    public PolymorphicHandlingUtil(JavaType originalType,SerializerProvider provider) {

        this.visitorUtils = new VisitorUtils(provider);
        this.originalType = originalType;
        this.provider=provider;
        processType(originalType);
    }


    public static Collection<NamedType> extractSubTypes(Class<?> clazz, SerializationConfig config) {
        AnnotatedClass classWithoutSuperType = AnnotatedClass.constructWithoutSuperTypes(clazz, config.getAnnotationIntrospector(), config);
        Collection<NamedType> namedTypes = null;
        if (config.getSubtypeResolver() != null) {

            namedTypes = new ArrayList<NamedType>(config.getSubtypeResolver().collectAndResolveSubtypes(classWithoutSuperType, config, config.getAnnotationIntrospector()));
        }

        Iterator<NamedType> it = namedTypes.iterator();
        while (it.hasNext()) {
            NamedType namedType = it.next();
            if (Modifier.isAbstract(namedType.getType().getModifiers())) {
                //remove abstract classes, should have full type information in subclasses
                it.remove();
            }
        }
        return namedTypes;

    }

    public SerializerProvider getProvider() {
        return provider;
    }

    private void processType(JavaType originalType) {
        if (getProvider() == null) {
            return;
        }

        if (getProvider().getConfig() == null) {
            return;
        }
        beanDescription = getProvider().getConfig().introspectClassAnnotations(originalType);

        if (beanDescription == null) {
            return;
        }

        Collection<NamedType> namedTypes = extractSubTypes(beanDescription.getBeanClass(), getProvider().getConfig());

        if (!namedTypes.isEmpty()) {
            subTypes = namedTypes;
        }
    }


    public PolymorphiSchemaDefinition extractPolymophicTypes() {
        if (!isPolymorphic()) {
            throw new IllegalArgumentException("Argument is not a polymorphic object (no JsonSubtype annotation (" + originalType.getRawClass().getSimpleName() + ")");
        }
        SchemaGenerationContext.get().addSeenSchemaUriForPolymorphic(originalType);
        final ReferenceSchema[] references = new ReferenceSchema[subTypes.size()];
        JsonSchema[] subSchemas = new JsonSchema[subTypes.size()];
        final Map<String, JsonSchema> definitions = new HashMap<String, JsonSchema>();

        Iterator<NamedType> it = subTypes.iterator();
        for (int i = 0; i < subTypes.size(); ++i) {
            NamedType namedType = it.next();
            if (!namedType.hasName()) {
                throw new IllegalArgumentException("No name associated with class " + namedType.getType().getSimpleName() + " try using @JsonTypeName annotation");
            }
            String subSchemaDefinitionName =getDefinitionReference(namedType);
            JsonSchema subSchema = visitorUtils.schema(namedType.getType());
            String subTypeName =namedType.getName();

            if(namedType.getType() == originalType.getRawClass()){
                subSchemaDefinitionName = subSchemaDefinitionName + POLYMORPHIC_TYPE_NAME_SUFFIX;
                subTypeName = subTypeName + POLYMORPHIC_TYPE_NAME_SUFFIX;
            }

            references[i] = new ReferenceSchema(subSchemaDefinitionName,subSchema.getType());

            subSchemas[i] = subSchema;

            definitions.put(subTypeName, subSchemas[i]);
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

    public boolean isPolymorphic() {
        return !SchemaGenerationContext.get().visitedPolymorphicType(originalType) && subTypes.size() > 1;
    }

    protected String getDefinitionReference(NamedType namedType) {
        if (!namedType.hasName()) {
            throw new IllegalArgumentException("Class " + namedType.getClass().getSimpleName() + " has no JSON name. Try using @JsonTypeName annotation");
        }
        return DEFINITION_PREFIX + namedType.getName();
    }

    public static JsonSchema propagateDefinitionsUp(JsonSchema node){
        Map<String,JsonSchema> allDefinitions = extractDefinitions(node,new HashMap<String, JsonSchema>());
        if(!allDefinitions.isEmpty()){
            node.setDefinitions(allDefinitions);
        }
        return node;

    }

    public static JsonSchema wrapNonNumericTypes(JsonSchema originalSchema) {
        if(!originalSchema.isNumberSchema()){
            return originalSchema;
        }

        if(originalSchema instanceof IntegerSchema){
            return originalSchema;
        }
        if(!SchemaGenerationContext.get().getMapper().isEnabled(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS)){
            return originalSchema;
        }

        NumberSchema numberSchema = (NumberSchema)originalSchema;
        if(numberSchema.getEnums()!=null && !numberSchema.getEnums().isEmpty()){
            return numberSchema;
        }

        return getNumberSchemaWithAllowedString();

    }
    private static Map<String,JsonSchema> extractDefinitions(JsonSchema node,Map<String,JsonSchema> definitionsSoFar){
        if(node.isObjectSchema()){
            if(node.asObjectSchema().getProperties()!=null) {
                for (Map.Entry<String, JsonSchema> entry : node.asObjectSchema().getProperties().entrySet()) {
                    mergeDefinitions(definitionsSoFar,extractDefinitions(entry.getValue(), definitionsSoFar));
                }
            }
        }
        else if(node.isArraySchema()){
            if(node.asArraySchema().getItems()!=null){
                if(node.asArraySchema().getItems().isSingleItems()){
                    mergeDefinitions(definitionsSoFar,extractDefinitions(node.asArraySchema().getItems().asSingleItems().getSchema(),definitionsSoFar));
                }
                else if(node.asArraySchema().getItems().isArrayItems()){
                    for(JsonSchema schema : node.asArraySchema().getItems().asArrayItems().getJsonSchemas()){
                        mergeDefinitions(definitionsSoFar,extractDefinitions(schema,definitionsSoFar));
                    }
                }
            }
        }
        if(node.getDefinitions()!=null) {
            for(Map.Entry<String,JsonSchema> entry : node.getDefinitions().entrySet()){
                mergeDefinitions(definitionsSoFar,extractDefinitions(entry.getValue(),definitionsSoFar));

            }
            mergeDefinitions(definitionsSoFar,node.getDefinitions());
            node.setDefinitions(null);
        }
        return definitionsSoFar;
    }

    private static void mergeDefinitions(Map<String,JsonSchema> target, Map<String,JsonSchema> source){
        if(source==null){
            return;
        }
        for(Map.Entry<String,JsonSchema> entry : source.entrySet()){
            if(!(entry.getValue() instanceof ReferenceSchema) && !(entry.getValue() instanceof AnyOfSchema)){
                target.put(entry.getKey(),entry.getValue());
            }
        }
    }

}
