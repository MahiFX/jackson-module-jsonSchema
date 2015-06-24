package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchemaV4.JsonSchema;
import com.fasterxml.jackson.module.jsonSchemaV4.types.ObjectSchema;

import java.util.ArrayList;
import java.util.List;

public class AnyVisitor extends JsonAnyFormatVisitor.Base
        implements JsonSchemaProducer {

    public static JsonFormatTypes[] FORMAT_TYPES_EXCEPT_ANY = allFormatTypesExceptAny();
    protected final JsonSchema schema;

    public AnyVisitor(JsonSchema schema) {
        this.schema = schema;
    }

    /*
    /*********************************************************************
    /* JsonSchemaProducer
    /*********************************************************************
     */

    @Override
    public JsonSchema getSchema() {
        return schema;
    }

    /*
    /*********************************************************************
    /* AnyVisitor: no additional methods...
    /*********************************************************************
     */

    private static JsonFormatTypes[] allFormatTypesExceptAny(){
        List<JsonFormatTypes> format = new ArrayList<JsonFormatTypes>();
        for(JsonFormatTypes type : JsonFormatTypes.values()){
            if(type!=JsonFormatTypes.ANY){
                format.add(type);
            }
        }
        return format.toArray(new JsonFormatTypes[0]);

    }
}
