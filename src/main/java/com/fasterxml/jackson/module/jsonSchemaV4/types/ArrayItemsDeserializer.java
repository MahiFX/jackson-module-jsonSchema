package com.fasterxml.jackson.module.jsonSchemaV4.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;

import java.io.IOException;

/**
 * Created by zoliszel on 19/06/2015.
 */
public class ArrayItemsDeserializer extends JsonDeserializer<ArraySchema.Items> {
    @Override
    public ArraySchema.Items deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if(jp.getCurrentToken()== JsonToken.VALUE_STRING){
            jp.nextToken();
        }
        TreeNode node = jp.readValueAsTree();

        if (node instanceof ObjectNode) {
            // not clean, but has to do...
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonSchema innerSchema = mapper.treeToValue(node, JsonSchema.class);
            return new ArraySchema.SingleItems(innerSchema);
        }
        if (node instanceof ArrayNode) {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonSchema[] innterSchema = mapper.treeToValue(node, JsonSchema[].class);

            return new ArraySchema.ArrayItems(innterSchema);
        }
        throw new JsonMappingException("ArrayItems can only be an ObjectNode or an ArrayNode. Found: " + node.asToken());

    }
}
