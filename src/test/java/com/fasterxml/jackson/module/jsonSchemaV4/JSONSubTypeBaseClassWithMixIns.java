package com.fasterxml.jackson.module.jsonSchemaV4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * Created by zoliszel on 09/06/2015.
 */

public abstract class JSONSubTypeBaseClassWithMixIns {

}

@JsonTypeName("JSONSubTypeBaseClassWithMixIns")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(PersonMixIns.class),
        @JsonSubTypes.Type(CompanyMixIns.class)
})
interface BaseMixIn {

}


class PersonMixIns extends JSONSubTypeBaseClassWithMixIns {

    public PersonMixIns() {
    }

    protected String name, dateOfBirth;

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getName() {
        return name;
    }
}

@JsonTypeName(PersonMixIn.TYPE_NAME)
interface PersonMixIn {
    String TYPE_NAME = "PersonWithMixIn";

    @JsonProperty(required = true)
    @JsonPropertyDescription("This is name")
    String getName();

    @JsonProperty(required = true)
    @JsonPropertyDescription("This is dateOfBirth")
    String getDateOfBirth();
}


class CompanyMixIns extends JSONSubTypeBaseClassWithMixIns {


    public CompanyMixIns() {
    }

    protected String name;

    public String getName() {
        return name;
    }


}

@JsonSubTypes({
        @JsonSubTypes.Type(BigCompanyMixIns.class),
})
@JsonTypeName(CompanyMixIn.TYPE_NAME)
interface CompanyMixIn {

    String TYPE_NAME = "CompanyWithMixIn";

    @JsonProperty(required = true)
    @JsonPropertyDescription("This the name of the company")
    String getName();

}

class BigCompanyMixIns extends CompanyMixIns {

    protected String directorName;

    public String getDirectorName() {
        return directorName;
    }
}

@JsonTypeName(BigCompanyMixIn.TYPE_NAME)
interface BigCompanyMixIn {

    String TYPE_NAME = "BigCompanyWithMixIn";

    @JsonProperty(required = true)
    @JsonPropertyDescription("This the name of the director of the big company")
    String getDirectorName();
}