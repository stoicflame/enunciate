package net.sf.enunciate.contract.jaxb;

import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import com.sun.mirror.declaration.ClassDeclaration;
import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestSimpleTypeDefinition extends InAPTTestCase {

  /**
   * tests getting the base type.
   */
  public void testBaseType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    SimpleTypeDefinition simpleType = new SimpleTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.SimpleTypeSimpleContentBean"));
    XmlTypeMirror baseType = simpleType.getBaseType();
    assertEquals(KnownXmlType.INT.getQname(), baseType.getQname());
  }

  public static Test suite() {
    return createSuite(TestSimpleTypeDefinition.class);
  }
}
