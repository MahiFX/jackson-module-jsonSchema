package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.types.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * The type wraps the json schema specification at :
 * <a href="http://tools.ietf.org/id/draft-zyp-json-schema-03.txt"> Json JsonSchema
 * Draft </a> <blockquote> JSON (JavaScript Object Notation) JsonSchema defines the
 * media type "application/schema+json", a JSON based format for defining the
 * structure of JSON data. JSON JsonSchema provides a contract for what JSON data is
 * required for a given application and how to interact with it. JSON JsonSchema is
 * intended to define validation, documentation, hyperlink navigation, and
 * interaction control of JSON data. </blockquote>
 * <p/>
 * <blockquote> JSON (JavaScript Object Notation) JsonSchema is a JSON media type
 * for defining the structure of JSON data. JSON JsonSchema provides a contract for
 * what JSON data is required for a given application and how to interact with
 * it. JSON JsonSchema is intended to define validation, documentation, hyperlink
 * navigation, and interaction control of JSON data. </blockquote>
 * <p/>
 * An example JSON JsonSchema provided by the JsonSchema draft:
 * <p/>
 * <pre>
 *    {
 * 	  "name":"Product",
 * 	  "properties":{
 * 	    "id":{
 * 	      "type":"number",
 * 	      "description":"Product identifier",
 * 	      "required":true
 *        },
 * 	    "name":{
 * 	      "description":"Name of the product",
 * 	      "type":"string",
 * 	      "required":true
 *        },
 * 	    "price":{
 * 	      "required":true,
 * 	      "type": "number",
 * 	      "minimum":0,
 * 	      "required":true
 *        },
 * 	    "tags":{
 * 	      "type":"array",
 * 	      "items":{
 * 	        "type":"string"
 *          }
 *        }
 *      },
 * 	  "links":[
 *        {
 * 	      "rel":"full",
 * 	      "href":"{id}"
 *        },
 *        {
 * 	      "rel":"comments",
 * 	      "href":"comments/?id={id}"
 *        }
 * 	  ]
 *    }
 * </pre>
 *
 * @author jphelan
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = Id.CUSTOM, include = As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonTypeIdResolver(JsonSchemaIdResolver.class)
@JsonTypeResolver(JsonSchemaTypeResolverBuilder.class)
public abstract class JsonSchema {
    /**
     * This attribute defines the current URI of this schema (this attribute is
     * effectively a "self" link). This URI MAY be relative or absolute. If the
     * URI is relative it is resolved against the current URI of the parent
     * schema it is contained in. If this schema is not contained in any parent
     * schema, the current URI of the parent schema is held to be the URI under
     * which this schema was addressed. If id is missing, the current URI of a
     * schema is defined to be that of the parent schema. The current URI of the
     * schema is also used to construct relative references such as for $ref.
     */
    @JsonProperty
    private String id;

    /**
     * This attribute defines a URI of a schema that contains the full
     * representation of this schema. When a validator encounters this
     * attribute, it SHOULD replace the current schema with the schema
     * referenced by the value's URI (if known and available) and re- validate
     * the instance. This URI MAY be relative or absolute, and relative URIs
     * SHOULD be resolved against the URI of the current schema.
     */
    @JsonProperty
    private String $ref;

    /**
     * This attribute defines a URI of a JSON JsonSchema that is the schema of the
     * current schema. When this attribute is defined, a validator SHOULD use
     * the schema referenced by the value's URI (if known and available) when
     * resolving Hyper JsonSchema (Section 6) links (Section 6.1).
     * <p/>
     * A validator MAY use this attribute's value to determine which version of
     * JSON JsonSchema the current schema is written in, and provide the appropriate
     * validation features and behavior. Therefore, it is RECOMMENDED that all
     * schema authors include this attribute in their schemas to prevent
     * conflicts with future JSON JsonSchema specification changes.
     */
    @JsonProperty
    private String $schema;


    /**
     * This attribute indicates if the instance is not modifiable.
     * This is false by default, making the instance modifiable.
     */
    @JsonProperty
    private Boolean readonly = null;


    /**
     * This attribute defines the type of the json object
     */

    protected JsonSchema.JSONType type;
    /**
     * This attribute is a string that provides a full description of the of
     * purpose the instance property.
     */
    private String description;

    @JsonProperty
    private Map<String, JsonSchema> definitions;

    /**
     * Attempt to return this JsonSchema as an {@link ArraySchema}
     *
     * @return this as an ArraySchema if possible, or null otherwise
     */
    public ArraySchema asArraySchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as a {@link BooleanSchema}
     *
     * @return this as a BooleanSchema if possible, or null otherwise
     */
    public BooleanSchema asBooleanSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as a {@link ContainerTypeSchema}
     *
     * @return this as an ContainerTypeSchema if possible, or null otherwise
     */
    public ContainerTypeSchema asContainerSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as an {@link IntegerSchema}
     *
     * @return this as an IntegerSchema if possible, or null otherwise
     */
    public IntegerSchema asIntegerSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as a {@link NullSchema}
     *
     * @return this as a NullSchema if possible, or null otherwise
     */
    public NullSchema asNullSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as a {@link NumberSchema}
     *
     * @return this as a NumberSchema if possible, or null otherwise
     */
    public NumberSchema asNumberSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as an {@link ObjectSchema}
     *
     * @return this as an ObjectSchema if possible, or null otherwise
     */
    public ObjectSchema asObjectSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as a {@link SimpleTypeSchema}
     *
     * @return this as a SimpleTypeSchema if possible, or null otherwise
     */
    public SimpleTypeSchema asSimpleTypeSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as a {@link StringSchema}
     *
     * @return this as a StringSchema if possible, or null otherwise
     */
    public StringSchema asStringSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as an {@link UnionTypeSchema}
     *
     * @return this as a UnionTypeSchema if possible, or null otherwise
     */
    public UnionTypeSchema asUnionTypeSchema() {
        return null;
    }

    /**
     * Attempt to return this JsonSchema as a {@link ValueTypeSchema}
     *
     * @return this as a ValueTypeSchema if possible, or null otherwise
     */
    public ValueTypeSchema asValueSchemaSchema() {
        return null;
    }


    public String getId() {
        return id;
    }

    public String get$ref() {
        return $ref;
    }

    public String get$schema() {
        return $schema;
    }


    public Boolean getReadonly() {
        return readonly;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, JsonSchema> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Map<String, JsonSchema> definitions) {
        this.definitions = definitions;
    }


    @JsonProperty
    @JsonDeserialize(using = JsonTypesDeserializer.class)
    // @JsonTypeId
    public abstract JsonSchema.JSONType getType();

    public abstract JsonSchema clone();

    public JsonSchema() {
        setType(getType());
    }

    public void setType(JSONType type) {
        this.type = type;
    }

    /**
     * determine if this JsonSchema is an {@link ArraySchema}.
     *
     * @return true if this JsonSchema is an ArraySchema, false otherwise
     */
    @JsonIgnore
    public boolean isArraySchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link BooleanSchema}.
     *
     * @return true if this JsonSchema is an BooleanSchema, false otherwise
     */
    @JsonIgnore
    public boolean isBooleanSchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link ContainerTypeSchema}.
     *
     * @return true if this JsonSchema is an ContainerTypeSchema, false otherwise
     */
    @JsonIgnore
    public boolean isContainerTypeSchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link IntegerSchema}.
     *
     * @return true if this JsonSchema is an IntegerSchema, false otherwise
     */
    @JsonIgnore
    public boolean isIntegerSchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link NullSchema}.
     *
     * @return true if this JsonSchema is an NullSchema, false otherwise
     */
    @JsonIgnore
    public boolean isNullSchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link NumberSchema}.
     *
     * @return true if this JsonSchema is an NumberSchema, false otherwise
     */
    @JsonIgnore
    public boolean isNumberSchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link ObjectSchema}.
     *
     * @return true if this JsonSchema is an ObjectSchema, false otherwise
     */
    @JsonIgnore
    public boolean isObjectSchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link SimpleTypeSchema}.
     *
     * @return true if this JsonSchema is an SimpleTypeSchema, false otherwise
     */
    @JsonIgnore
    public boolean isSimpleTypeSchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link StringSchema}.
     *
     * @return true if this JsonSchema is an StringSchema, false otherwise
     */
    @JsonIgnore
    public boolean isStringSchema() {
        return false;
    }

    /**
     * determine if this JsonSchema is an {@link UnionTypeSchema}.
     *
     * @return true if this JsonSchema is an UnionTypeSchema, false otherwise
     */
    @JsonIgnore
    public boolean isUnionTypeSchema() {
        return false;
    }


    @JsonIgnore
    public boolean isPolymorhpicObjectSchema() {
        return false;
    }

    @JsonIgnore
    public boolean isReferenceSchema() {
        return false;
    }


    /**
     * determine if this JsonSchema is an {@link ValueTypeSchema}.
     *
     * @return true if this JsonSchema is an ValueTypeSchema, false otherwise
     */
    @JsonIgnore
    public boolean isValueTypeSchema() {
        return false;
    }

    public void set$ref(String $ref) {
        this.$ref = $ref;
    }

    public void set$schema(String $schema) {
        this.$schema = $schema;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Override this to add information specific to the property of bean
     * For example, bean validation annotations could be used to specify
     * value constraints in the schema
     *
     * @param beanProperty
     */
    public void enrichWithBeanProperty(BeanProperty beanProperty) {
        setDescription(beanProperty.getMetadata().getDescription());
    }

    /**
     * Create a schema which verifies only that an object is of the given format.
     *
     * @param format the format to expect
     * @return the schema verifying the given format
     */
    public static JsonSchema minimalForFormat(JsonFormatTypes format) {
        if (format != null) {
            switch (format) {
                case ARRAY:
                    return new ArraySchema();
                case OBJECT:
                    return new ObjectSchema();
                case BOOLEAN:
                    return new BooleanSchema();
                case INTEGER:
                    return new IntegerSchema();
                case NUMBER:
                    return new NumberSchema();
                case STRING:
                    return new StringSchema();
                case NULL:
                    return new NullSchema();
                case ANY:
                default:
            }
        }
        return new ObjectSchema();
    }

    protected void cloneSchema(JsonSchema arraySchema) {
        arraySchema.setDefinitions(getDefinitions());
        arraySchema.set$schema(get$schema());
        arraySchema.setDescription(getDescription());
        arraySchema.setId(getId());
        arraySchema.setReadonly(getReadonly());
        arraySchema.set$ref(get$ref());
        arraySchema.setType(getType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof JsonSchema)) return false;
        return _equals((JsonSchema) obj);
    }

    protected boolean _equals(JsonSchema that) {
        return equals(getId(), getId())

                // 27-Apr-2015, tatu: Should not need to check type explicitly
                && equals(getType(), getType())
                && equals(getReadonly(), that.getReadonly())
                && equals(get$ref(), that.get$ref())
                && equals(get$schema(), that.get$schema())
                && equals(getDefinitions(), that.getDefinitions());
    }

    /**
     * A utility method allowing to easily chain calls to equals() on members
     * without taking any risk regarding the ternary operator precedence.
     *
     * @return (object1 = = null ? object2 = = null : object1.equals ( object2))
     */
    protected static boolean equals(Object object1, Object object2) {
        if (object1 == null) {
            return object2 == null;
        }
        return object1.equals(object2);
    }

    protected static <T> boolean arraysEqual(T[] arr1, T[] arr2) {
        if (arr1 == null) {
            return arr2 == null;
        }
        if (arr2 == null) {
            return false;
        }
        int len = arr1.length;
        if (len != arr2.length) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            T ob1 = arr1[i];
            T ob2 = arr2[i];

            if (!equals(ob1, ob2)) {
                return false;
            }
        }
        return true;
    }

    public ReferenceSchema asReferenceSchema() {
        return null;
    }

    public PolymorphicObjectSchema asPolymorphicObjectSchema() {
        return null;
    }


    public static abstract class JSONType {

        public boolean isSingleJSONType() {
            return false;
        }

        public boolean isArrayJSONType() {
            return false;
        }

        public SingleJsonType asSingleJsonType() {
            return null;
        }

        public ArrayJsonType asArrayJsonType() {
            return null;
        }
    }

    public static class SingleJsonType extends JSONType {

        @JsonIgnore
        private JsonFormatTypes formatType;


        public SingleJsonType(JsonFormatTypes formatType) {
            this.formatType = formatType;
        }

        @JsonValue
        public String getFormatType() {
            return formatType.value();
        }

        public void setFormatType(String formatType) {
            this.formatType = JsonFormatTypes.forValue(formatType);
        }

        @Override
        public boolean isSingleJSONType() {
            return true;
        }

        @Override
        public SingleJsonType asSingleJsonType() {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SingleJsonType that = (SingleJsonType) o;

            return formatType == that.formatType;

        }

        @Override
        public int hashCode() {
            return formatType != null ? formatType.hashCode() : 0;
        }

        @Override
        public String toString() {
            return formatType.value();
        }
    }

    public static class ArrayJsonType extends JSONType {

        public ArrayJsonType(JsonFormatTypes[] formatTypes) {
            HashSet<JsonFormatTypes> deDuped = new HashSet<>(Arrays.asList(formatTypes));
            this.formatTypes = deDuped.toArray(new JsonFormatTypes[0]);
            Arrays.sort(this.formatTypes);
        }

        @JsonIgnore
        private JsonFormatTypes[] formatTypes;

        @JsonValue
        public JsonFormatTypes[] getFormatTypes() {
            return formatTypes;
        }

        @Override
        public boolean isArrayJSONType() {
            return true;
        }

        @Override
        public ArrayJsonType asArrayJsonType() {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ArrayJsonType that = (ArrayJsonType) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(formatTypes, that.formatTypes);

        }

        @Override
        public int hashCode() {
            return formatTypes != null ? Arrays.hashCode(formatTypes) : 0;
        }
    }


}
