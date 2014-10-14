package org.codehaus.enunciate.template.freemarker;

import com.sun.mirror.declaration.ClassDeclaration;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class TestClientClassnameFor extends InAPTTestCase {

  /**
   * tests a self-referencing class.
   */
  public void testSelfReferencingClass() throws Exception {
    HashMap<String, String> conversions = new HashMap<String, String>();
    conversions.put("org.codehaus.enunciate.samples.schema", "org.codehaus.enunciate.samples.client.schema");
    ClientClassnameForMethod meth = new ClientClassnameForMethod(conversions) {
      @Override
      protected Object unwrap(Object first) throws TemplateModelException {
        return first;
      }
    };
    meth.setJdk15(true);

    ClassDeclaration decl = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.SelfReferencingClass");
    ComplexTypeDefinition ct = new ComplexTypeDefinition(decl);
    assertTrue(String.valueOf(meth.exec(Arrays.asList(ct))).startsWith("org.codehaus.enunciate.samples.client.schema.SelfReferencingClass"));

    decl = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.SelfReferencingPropertyBean");
    ct = new ComplexTypeDefinition(decl);
    assertTrue(String.valueOf(meth.exec(Arrays.asList(ct.getElements().first()))).startsWith("org.codehaus.enunciate.samples.client.schema.SelfReferencingClass"));
  }
}
