package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.*;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsExternalTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zoliszel on 30/06/2015.
 */
public class JsonSchemaTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {

    public JsonSchemaTypeResolverBuilder() {
        super(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {

        if (_idType == JsonTypeInfo.Id.NONE) {
            return null;
        }

        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder().build();
        TypeIdResolver idRes = idResolver(config, baseType, validator, subtypes, false, true);

        JavaType defaultType = _defaultImpl != null ? config.getTypeFactory().constructType(_defaultImpl) : null;

        // First, method for converting type info to type id:
        switch (_includeAs) {
            case WRAPPER_ARRAY:
                return new AsArrayTypeDeserializer(baseType, idRes,
                        _typeProperty, _typeIdVisible, defaultType);
            case PROPERTY:
            case EXISTING_PROPERTY: // as per [#528] same class as PROPERTY
                return new JsonSchemaPropertyTypeDeserializer(baseType, idRes,
                        _typeProperty, _typeIdVisible, defaultType, _includeAs);
            case WRAPPER_OBJECT:
                return new AsWrapperTypeDeserializer(baseType, idRes,
                        _typeProperty, _typeIdVisible, defaultType);
            case EXTERNAL_PROPERTY:
                return new AsExternalTypeDeserializer(baseType, idRes,
                        _typeProperty, _typeIdVisible, defaultType);
        }
        throw new IllegalStateException("Do not know how to construct standard type serializer for inclusion type: " + _includeAs);

    }

    private static class JsonSchemaPropertyTypeDeserializer extends AsPropertyTypeDeserializer {

        public JsonSchemaPropertyTypeDeserializer(JavaType bt, TypeIdResolver idRes, String typePropertyName, boolean typeIdVisible, JavaType defaultImpl, JsonTypeInfo.As inclusion) {
            super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl, inclusion);
        }

        public JsonSchemaPropertyTypeDeserializer(AsPropertyTypeDeserializer src, BeanProperty property) {
            super(src, property);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return prop == this._property ? this : new JsonSchemaPropertyTypeDeserializer(this, prop);
        }

        protected Object _deserializeTypedForId(JsonParser jp, DeserializationContext ctxt, TokenBuffer tb, String passedInTypeId) throws IOException {

            if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
                String typeId = jp.getText();
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, typeId);
                if (_typeIdVisible) { // need to merge id back in JSON input?
                    if (tb == null) {
                        tb = new TokenBuffer(null, false);
                    }
                    tb.writeFieldName(jp.getCurrentName());
                    tb.writeString(typeId);
                }
                if (tb != null) { // need to put back skipped properties?
                    jp = JsonParserSequence.createFlattened(false, tb.asParser(jp), jp);
                }
                // Must point to the next value; tb had no current, jp pointed to VALUE_STRING:
                jp.nextToken(); // to skip past String value
                // deserializer should take care of closing END_OBJECT as well
                return deser.deserialize(jp, ctxt);
            } else if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
                if (_typeIdVisible) {
                    if (tb == null) {
                        tb = new TokenBuffer(jp.getCodec(), false);
                    }
                    tb.writeFieldName(jp.getCurrentName());
                }
                Set<String> values = new HashSet<String>();
                while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                    if (_typeIdVisible) {
                        tb.copyCurrentEvent(jp);
                    }
                    jp.nextToken();
                    if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
                        values.add(jp.getText());
                    }
                }
                if (_typeIdVisible) {
                    tb.copyCurrentEvent(jp);
                }
                String typeId = Arrays.toString(values.toArray(new String[0]));
                JsonDeserializer<Object> deser = _findDeserializer(ctxt, typeId);
                if (tb != null) { // need to put back skipped properties?
                    jp = JsonParserSequence.createFlattened(tb.asParser(jp), jp);
                }
                jp.nextToken();
                return deser.deserialize(jp, ctxt);
            }

            throw new IllegalStateException("Can't resolve typeid from token: " + jp.getCurrentToken());

        }
    }
}
