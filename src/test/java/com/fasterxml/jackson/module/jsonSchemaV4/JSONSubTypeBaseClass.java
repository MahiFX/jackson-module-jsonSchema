package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * Created by zoliszel on 09/06/2015.
 */
@JsonTypeName("JSONSubTypeBaseClass")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(Person.class),
        @JsonSubTypes.Type(Company.class)
})
public abstract class JSONSubTypeBaseClass {

}

@JsonTypeName(Person.TYPE_NAME)
class Person extends JSONSubTypeBaseClass {

    public static final String TYPE_NAME = "Person";

    public Person() {
    }

    public Person(String name, String dateOfBirth) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    @JsonProperty(required = true)
    @JsonPropertyDescription("This is name")
    public String name;

    @JsonProperty(required = true)
    @JsonPropertyDescription("This is dateOfBirth")
    public String dateOfBirth;
}

@JsonSubTypes({
        @JsonSubTypes.Type(BigCompany.class),
})
@JsonTypeName(Company.TYPE_NAME)
class Company extends JSONSubTypeBaseClass {

    public static final String TYPE_NAME = "Company";

    public Company() {
    }

    public Company(String name) {
        this.name = name;
    }


    @JsonProperty(required = true)
    @JsonPropertyDescription("This the name of the company")
    public String name;

}

@JsonTypeName(BigCompany.TYPE_NAME)
class BigCompany extends Company {

    public static final String TYPE_NAME = "BigCompany";

    public BigCompany() {
    }

    public BigCompany(String name, String director) {
        super(name);
        this.directorName = director;
    }

    @JsonProperty(required = true)
    @JsonPropertyDescription("This the name of the director of the big company")
    public String directorName;
}