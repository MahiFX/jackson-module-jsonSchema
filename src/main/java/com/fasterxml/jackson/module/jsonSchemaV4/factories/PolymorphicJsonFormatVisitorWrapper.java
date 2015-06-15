package com.fasterxml.jackson.module.jsonSchemaV4.factories;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;

/**
 * Created by zoliszel on 12/06/2015.
 */
public interface PolymorphicJsonFormatVisitorWrapper extends JsonFormatVisitorWrapper {

    /**
     * @param type Declared type of visited property (or List element) in Java
     */
    public PolymorphicObjectVisitor expectPolyMorphicObjectFormat(JavaType type) throws JsonMappingException;
}
