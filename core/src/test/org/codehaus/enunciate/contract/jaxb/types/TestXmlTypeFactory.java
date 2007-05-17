/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.jaxb.types;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.DeclaredType;
import junit.framework.Test;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.namespace.QName;

/**
 * Tests the XmlTypeFactory.
 *
 * @author Ryan Heaton
 */
public class TestXmlTypeFactory extends InAPTTestCase {

  /**
   * Tests finding the specified type of a given declaration.
   */
  public void testFindSpecifiedType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.AdaptedBeanTwo")));

    ClassDeclaration adaptedBeanExamplesDeclaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.AdaptedBeanExamples");
    ComplexTypeDefinition examples = new ComplexTypeDefinition(adaptedBeanExamplesDeclaration);
    Accessor beanOneField = null;
    Accessor beanThreeField = null;
    Accessor beanFourField = null;
    Accessor stringBufferField = null;
    for (Accessor fieldDeclaration : examples.getElements()) {
      if ("adaptedBeanOne".equals(fieldDeclaration.getSimpleName())) {
        beanOneField = fieldDeclaration;
      }
      else if ("adaptedBeanThree".equals(fieldDeclaration.getSimpleName())) {
        beanThreeField = fieldDeclaration;
      }
      else if ("stringBuffer".equals(fieldDeclaration.getSimpleName())) {
        stringBufferField = fieldDeclaration;
      }
      else if ("beanFour".equals(fieldDeclaration.getSimpleName())) {
        beanFourField = fieldDeclaration;
      }
    }

    assertNotNull(beanFourField);
    XmlType beanFourXmlType = XmlTypeFactory.findExplicitSchemaType(beanFourField);
    assertEquals("The xml type for bean four should have been specified at the package-level.", "specified-bean-four", beanFourXmlType.getName());
    assertEquals("The xml type for bean four should have been specified at the package-level.", "http://org.codehaus.enunciate/core/samples/beanfour", beanFourXmlType.getNamespace());
    assertNull(XmlTypeFactory.findSpecifiedType(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.DifferentPackageReferencingBeanFour")).getElements().first()));

    assertNotNull(beanOneField);
    assertEquals(new QName("http://org.codehaus.enunciate/core/samples/another", "adaptedBeanTwo"), XmlTypeFactory.findSpecifiedType(beanOneField).getQname());
    assertNotNull(beanThreeField);
    assertEquals(KnownXmlType.STRING.getQname(), XmlTypeFactory.findSpecifiedType(beanThreeField).getQname());
    assertNotNull(stringBufferField);
    assertEquals(KnownXmlType.STRING.getQname(), XmlTypeFactory.findSpecifiedType(stringBufferField).getQname());

    EndpointInterface ei = new EndpointInterface(adaptedBeanExamplesDeclaration);
    WebParam beanFourParam = ei.getWebMethods().iterator().next().getWebParameters().iterator().next();
    assertNotNull(beanFourParam);
    assertEquals(new QName("http://org.codehaus.enunciate/core/samples/another", "adaptedBeanTwo"), XmlTypeFactory.findSpecifiedType(beanFourParam).getQname());

    try {
      new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.AdaptedBeanBadExamples"));
      fail("Shouldn't be a valid xml type adapter.");
    }
    catch (ValidationException e) {
      //fall through...
    }

    try {
      new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.AdaptedBeanBadExamples2"));
      fail("Shouldn't be a valid xml type adapter.");
    }
    catch (ValidationException e) {
      //fall through...
    }

  }

  /**
   * Getting the xml type for a specified type.
   */
  public void testGetXmlType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    DeclaredType stringType = Context.getCurrentEnvironment().getTypeUtils().getDeclaredType(getDeclaration("java.lang.String"));
    XmlType stringXmlType = XmlTypeFactory.getXmlType(stringType);
    assertSame(KnownXmlType.STRING, stringXmlType);
    assertSame(stringXmlType, XmlTypeFactory.getXmlType(String.class));

    ClassDeclaration decl = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.BeanThree");
    ComplexTypeDefinition definition = new ComplexTypeDefinition(decl);
    model.add(definition);
    DeclaredType beanThreeType = Context.getCurrentEnvironment().getTypeUtils().getDeclaredType(decl);
    assertNotNull("The xml type for bean three should have been created.", XmlTypeFactory.getXmlType(beanThreeType));
  }

  public static Test suite() {
    return createSuite(TestXmlTypeFactory.class);
  }

}
