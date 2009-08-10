package org.codehaus.enunciate.contract.json;

import net.sf.jelly.apt.freemarker.FreemarkerModel;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;

import com.sun.mirror.declaration.ClassDeclaration;

/**
 * @author Steven Cummings
 */
public final class TestJsonTypeDefinition extends InAPTTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        final EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
        FreemarkerModel.set(model);
    }

    public void testCreateTypeDefinition_Enum()
    {
        final JsonTypeDefinition jsonTypeDefinition = JsonTypeDefinition.createTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.PersonType"));
        assertTrue(jsonTypeDefinition instanceof JsonEnumTypeDefinition);
    }

    public void testCreateTypeDefinition_Object()
    {
        final JsonTypeDefinition jsonTypeDefinition = JsonTypeDefinition.createTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.Name"));
        assertTrue(jsonTypeDefinition instanceof JsonObjectTypeDefinition);
    }

    public void testTypeName_LogicalName()
    {
        final JsonTypeDefinition jsonTypeDefinition = JsonTypeDefinition.createTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.Name"));
        assertEquals("name", jsonTypeDefinition.getTypeName());
    }

    public void testTypeName_NoLogicalName()
    {
        final JsonTypeDefinition jsonTypeDefinition = JsonTypeDefinition.createTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.PersonType"));
        assertEquals("org.codehaus.enunciate.samples.json.PersonType", jsonTypeDefinition.getTypeName());
    }
}
