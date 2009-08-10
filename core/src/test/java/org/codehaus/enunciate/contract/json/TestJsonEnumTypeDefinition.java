package org.codehaus.enunciate.contract.json;

import java.util.Collection;
import java.util.Iterator;

import net.sf.jelly.apt.freemarker.FreemarkerModel;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.json.JsonEnumTypeDefinition.EnumValue;

import com.sun.mirror.declaration.EnumDeclaration;

/**
 * @author Steven Cummings
 */
public final class TestJsonEnumTypeDefinition extends InAPTTestCase
{
    private JsonEnumTypeDefinition enumTypeDefinition;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        final EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
        FreemarkerModel.set(model);
        enumTypeDefinition = new JsonEnumTypeDefinition((EnumDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.PersonType"));
    }

    public void testEnumValues()
    {
        final Collection<EnumValue> enumValues = enumTypeDefinition.getEnumValues();
        assertEquals(2, enumValues.size());
        final Iterator<EnumValue> iterator = enumValues.iterator();
        assertEquals("EMPLOYEE", iterator.next().getName());
        assertEquals("CUSTOMER", iterator.next().getName());
    }

    public void testEnumValue_DescriptionFromDocComment()
    {
        assertEquals("An employee.", enumTypeDefinition.getEnumValues().iterator().next().getDescription().trim());
    }
}
