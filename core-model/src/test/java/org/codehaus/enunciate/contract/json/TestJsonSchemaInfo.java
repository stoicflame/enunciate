package org.codehaus.enunciate.contract.json;

import net.sf.jelly.apt.freemarker.FreemarkerModel;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;

import com.sun.mirror.declaration.ClassDeclaration;

/**
 * @author Steven Cummings
 */
public class TestJsonSchemaInfo extends InAPTTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        final EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
        FreemarkerModel.set(model);
    }

    public void testSchemaIdForType_PackageAnnotated()
    {
        final ClassDeclaration classDeclaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.json.Person");
        final String schemaId = JsonSchemaInfo.schemaIdForType(classDeclaration);
        assertEquals("samples.json", schemaId);
    }

    public void testSchemaIdForType_PackageNotAnnotated()
    {
        final ClassDeclaration classDeclaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne");
        final String schemaId = JsonSchemaInfo.schemaIdForType(classDeclaration);
        assertEquals("org.codehaus.enunciate.samples.schema", schemaId);
    }
}
