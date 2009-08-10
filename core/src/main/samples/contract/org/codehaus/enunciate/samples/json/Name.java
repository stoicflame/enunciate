package org.codehaus.enunciate.samples.json;

import org.codehaus.enunciate.json.JsonProperty;
import org.codehaus.enunciate.json.JsonType;

@JsonType(name = "name")
public class Name
{
    private String first;
    private String last;
    private String middle;

    @JsonProperty
    public String getFirst()
    {
        return first;
    }

    public void setFirst(final String first)
    {
        this.first = first;
    }

    @JsonProperty(name = "surname")
    public String getLast()
    {
        return last;
    }

    public void setLast(final String last)
    {
        this.last = last;
    }

    public String getMiddle()
    {
        return middle;
    }

    @JsonProperty
    public void setMiddle(final String middle)
    {
        this.middle = middle;
    }
}
