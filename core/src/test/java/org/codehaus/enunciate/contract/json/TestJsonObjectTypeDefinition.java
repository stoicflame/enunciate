package org.codehaus.enunciate.contract.json;

import java.util.Map;

import net.sf.jelly.apt.freemarker.FreemarkerModel;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.json.JsonObjectTypeDefinition.JsonPropertyDeclaration;

import com.sun.mirror.declaration.ClassDeclaration;

/**
 * @author Steven Cummings
 */
public class TestJsonObjectTypeDefinition extends InAPTTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        final EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
        FreemarkerModel.set(model);
    }

    public void testProperties_NameOverride()
    {
        final JsonObjectTypeDefinition nameTypeDefinition = new JsonObjectTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.Name"));
        final Map<String, JsonPropertyDeclaration> actualNameProperties = nameTypeDefinition.getJsonPropertiesByName();
        assertProperty("surname", null, String.class.getName(), actualNameProperties.get("surname"));
    }

    public void testProperties_AnnotationOnSetter()
    {
        final JsonObjectTypeDefinition nameTypeDefinition = new JsonObjectTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.Name"));
        final Map<String, JsonPropertyDeclaration> actualNameProperties = nameTypeDefinition.getJsonPropertiesByName();
        assertProperty("middle", null, String.class.getName(), actualNameProperties.get("middle"));
    }

    public void testProperties_JsonIgnored_Excluded()
    {
        final JsonObjectTypeDefinition personTypeDefinition = new JsonObjectTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.Person"));
        final Map<String, JsonPropertyDeclaration> actualPersonProperties = personTypeDefinition.getJsonPropertiesByName();
        assertNull(actualPersonProperties.get("notAJsonProperty"));
    }

    public void testProperties_DescriptionFromDocComment()
    {
        final JsonObjectTypeDefinition personTypeDefinition = new JsonObjectTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.Person"));
        final Map<String, JsonPropertyDeclaration> actualPersonProperties = personTypeDefinition.getJsonPropertiesByName();
        assertProperty("name", "The name", "org.codehaus.enunciate.samples.json.Name", actualPersonProperties.get("name"));
    }

    public void testProperties_AllAnnotatedPresent()
    {
        final JsonObjectTypeDefinition nameTypeDefinition = new JsonObjectTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.Name"));
        final Map<String, JsonPropertyDeclaration> actualNameProperties = nameTypeDefinition.getJsonPropertiesByName();
        assertEquals(3, actualNameProperties.size());
        assertProperty("first", null, String.class.getName(), actualNameProperties.get("first"));
        assertProperty("middle", null, String.class.getName(), actualNameProperties.get("middle"));
        assertProperty("surname", null, String.class.getName(), actualNameProperties.get("surname"));
    }

    private void assertProperty(final String expectedName, final String expectedDocComment, final String expectedTypeName, final JsonPropertyDeclaration actualProperty)
    {
        assertNotNull(actualProperty);
        assertEquals(expectedName, actualProperty.getPropertyName());
        assertEquals(expectedDocComment, actualProperty.getPropertyDescription());
    }
}
