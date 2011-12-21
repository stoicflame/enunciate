package org.codehaus.enunciate.contract.jaxb.adapters;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;

/**
 * @author Ryan Heaton
 */
public class TestAdapterUtil extends InAPTTestCase {
  
  /**
   * see https://jira.codehaus.org/browse/ENUNCIATE-626
   */
  public void testAdaptingSubclassedAdapter() throws Exception {
    TypeDeclaration decl = getDeclaration("org.codehaus.enunciate.samples.anotherschema.BeanWithPropertyAdaptedBySubclass");
    ComplexTypeDefinition ct = new ComplexTypeDefinition((ClassDeclaration) decl);
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);
    model.add(ct);
    Element el = ct.getElements().first();
    assertEquals(KnownXmlType.LONG.getQname(), el.getBaseType().getQname());
//    assertEquals("org.codehaus.enunciate.samples.anotherschema.BeanTwo", ((DeclaredType) el.getAdapterType().getAdaptedType()).getDeclaration().getQualifiedName());
  }
}
