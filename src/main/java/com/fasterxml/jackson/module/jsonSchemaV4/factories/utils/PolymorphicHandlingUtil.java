package com.fasterxml.jackson.module.jsonSchemaV4.factories.utils;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.factories.VisitorContext;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ReferenceSchema;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zoliszel on 16/06/2015.
 */
public class PolymorphicHandlingUtil {

    public interface PolymorphiSchemaDefinition {
        public Map<String, JsonSchema> getDefinitions();

        public ReferenceSchema[] getReferences();

    }

    protected final VisitorContext visitorContext;

    protected final SerializerProvider provider;

    protected final ObjectMapper mapper;

    protected final VisitorUtils visitorUtils;

    protected final JavaType originalType;

    protected BeanDescription beanDescription;

    private Collection<NamedType> subTypes = Collections.emptyList();

    public PolymorphicHandlingUtil(VisitorContext visitorContext, SerializerProvider provider, ObjectMapper mapper, JavaType originalType) {
        this.visitorContext = visitorContext;
        this.provider = provider;
        this.mapper = mapper;
        this.visitorUtils = new VisitorUtils(mapper, visitorContext, provider);
        this.originalType = originalType;
        processType(originalType);
    }


    public static Collection<NamedType> extractSubTypes(Class<?> clazz, SerializationConfig config) {
        AnnotatedClass classWithoutSuperType = AnnotatedClass.constructWithoutSuperTypes(clazz, config.getAnnotationIntrospector(), config);
        Collection<NamedType> namedTypes = null;
        if (config.getSubtypeResolver() != null) {
            namedTypes = new ArrayList<NamedType>(config.getSubtypeResolver().collectAndResolveSubtypesByClass(config, classWithoutSuperType));
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

    private void processType(JavaType originalType) {
        if (provider == null) {
            return;
        }

        if (provider.getConfig() == null) {
            return;
        }
        beanDescription = provider.getConfig().introspectClassAnnotations(originalType);

        if (beanDescription == null) {
            return;
        }

        //AnnotatedClass classWithoutSuperType=AnnotatedClass.constructWithoutSuperTypes(beanDescription.getBeanClass(),provider.getAnnotationIntrospector(),provider.getConfig());

        Collection<NamedType> namedTypes = extractSubTypes(beanDescription.getBeanClass(), provider.getConfig());
        /*
        if (provider.getConfig().getSubtypeResolver() != null) {
            namedTypes = new ArrayList<NamedType>(provider.getConfig().getSubtypeResolver().collectAndResolveSubtypesByClass(provider.getConfig(), classWithoutSuperType));
        }



        Iterator<NamedType> it = namedTypes.iterator();
        while(it.hasNext()){
            NamedType namedType = it.next();
            if(Modifier.isAbstract(namedType.getType().getModifiers())){
                //remove abstract classes, should have full type information in subclasses
                it.remove();
            }
        }

        */
        if (!namedTypes.isEmpty()) {
            subTypes = namedTypes;
        }
    }


    public PolymorphiSchemaDefinition extractPolymophicTypes() {
        if (!isPolymorphic()) {
            throw new IllegalArgumentException("Argument is not visitPolymorphicObject (no JsonSubtype annotation (" + originalType.getRawClass().getSimpleName() + ")");
        }
        //Class<?>[] polymorphicTypes = extractPolymorphicTypes(clazz).toArray(new Class<?>[0]);
        final ReferenceSchema[] references = new ReferenceSchema[subTypes.size()];
        JsonSchema[] subSchemas = new JsonSchema[subTypes.size()];
        final Map<String, JsonSchema> definitions = new HashMap<String, JsonSchema>();

        Iterator<NamedType> it = subTypes.iterator();
        for (int i = 0; i < subTypes.size(); ++i) {
            NamedType namedType = it.next();
            if (!namedType.hasName()) {
                throw new IllegalArgumentException("No name associated with class " + namedType.getType().getSimpleName() + " try using @JsonTypeName annotation");
            }
            references[i] = new ReferenceSchema(getDefinitionReference(namedType));

            //here we make a copy of the mapper which is no longer polymorphic "aware". This is needed because all of the subtypes are defined at this point
            //and we do not want subtypes redefine another potential subtypes. i.e. A extends B, B extends C if we create a schema for A than all subtypes are known upfront (A,B,C)
            //hence the schema of B do not need to define that it can be B or C
            subSchemas[i] = visitorUtils.schema(namedType.getType(), mapper.copy().setSerializerFactory(BeanSerializerFactory.instance));
            definitions.put(namedType.getName(), subSchemas[i]);
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
        return subTypes.size() > 1;
    }

    protected String getDefinitionReference(NamedType namedType) {


        if (visitorContext != null) {
            return visitorContext.javaTypeToUrn(mapper.constructType(namedType.getType()));
        } else {
            if (!namedType.hasName()) {
                throw new IllegalArgumentException("Class " + namedType.getClass().getSimpleName() + " has no JSON name. Try using @JsonTypeName annotation");
            }
            return "#/definitions/" + namedType.getName();
        }

    }
    /*
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
    */

}
