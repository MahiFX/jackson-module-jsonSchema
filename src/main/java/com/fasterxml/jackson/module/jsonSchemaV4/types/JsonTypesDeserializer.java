package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

import java.io.IOException;
import java.util.HashSet;

/**
 * Created by zoliszel on 19/06/2015.
 */
public class JsonTypesDeserializer extends JsonDeserializer<JsonSchema.JSONType> {
    @Override
    public JsonSchema.JSONType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if(jp.getCurrentToken()== JsonToken.VALUE_STRING){
            JsonFormatTypes jsonFormatType = JsonFormatTypes.forValue(jp.getText());
            return new JsonSchema.SingleJsonType(jsonFormatType);
        }
        TreeNode node = jp.readValueAsTree();

        if (node instanceof ObjectNode) {
            // not clean, but has to do...
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonFormatTypes jsonFormatType = mapper.treeToValue(node, JsonFormatTypes.class);
            return new JsonSchema.SingleJsonType(jsonFormatType);
        }
        if (node instanceof ArrayNode) {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonFormatTypes[] jsonFormatTypes = mapper.treeToValue(node, JsonFormatTypes[].class);

            return new JsonSchema.ArrayJsonType(jsonFormatTypes);
        }
        throw new JsonMappingException("JSONType can only be an ObjectNode or an ArrayNode. Found: " + node.asToken());

    }
}
