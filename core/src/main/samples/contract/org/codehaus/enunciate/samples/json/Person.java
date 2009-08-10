package org.codehaus.enunciate.samples.json;

import org.codehaus.enunciate.json.JsonProperty;
import org.codehaus.enunciate.json.JsonType;

@JsonType(name = "person")
public class Person
{
    private Name name;
    private String notAJsonProperty;

    /**
     * The name
     * @return The name
     */
    @JsonProperty
    public Name getName()
    {
        return name;
    }

    public void setName(final Name name)
    {
        this.name = name;
    }

    public String getNotAJsonProperty()
    {
        return notAJsonProperty;
    }

    public void setNotAJsonProperty(final String notAJsonProperty)
    {
        this.notAJsonProperty = notAJsonProperty;
    }

}
