package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.*;


/**
 * Created by zoliszel on 09/06/2015.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(Person.class),
        @JsonSubTypes.Type(CompanyObfuscated.class)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class JSONSubTypeBaseClass {

    public static final String TYPE = "JSONSubTypeBaseClass";
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
@JsonTypeName(CompanyObfuscated.TYPE_NAME)
class CompanyObfuscated extends JSONSubTypeBaseClass {

    public static final String TYPE_NAME = "Company";

    public CompanyObfuscated() {
    }

    @JsonProperty
    public CompanyObfuscated sisterCompany;

    @JsonProperty
    public CompanyObfuscated[] competitors;


    public CompanyObfuscated(String name) {
        this.nameCompany = name;
    }


    @JsonProperty(required = true)
    @JsonPropertyDescription("This the name of the company")
    public String nameCompany;


}

@JsonTypeName(BigCompany.TYPE_NAME)
class BigCompany extends CompanyObfuscated {

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

@JsonTypeName("Intf")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(@JsonSubTypes.Type(Impl.class))
interface Intf {

    String getFoo();

}


@JsonTypeName("Intf2")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(Impl.class),
        @JsonSubTypes.Type(Impl2.class),
        @JsonSubTypes.Type(Impl3.class),
})
interface Intf2 {

    String getFoo();

}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeName("Impl")
class Impl implements Intf, Intf2 {

    String foo;

    @Override
    public String getFoo() {
        return foo;
    }
}


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeName("Impl2")
class Impl2 implements Intf2 {

    String foo;

    @Override
    public String getFoo() {
        return foo;
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeName("Intf[]")
class IntfArrayMixin {

}


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeName("Intf2[]")
class Intf2ArrayMixin {

}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeName("Impl3")
class Impl3 extends Impl4 implements Intf2 {

    String baz;
    String foo;

    public String getBaz() {
        return baz;
    }

    @Override
    public String getFoo() {
        return foo;
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeName("Impl4")
class Impl4 {

    String bar;

    public String getBar() {
        return bar;
    }
}


