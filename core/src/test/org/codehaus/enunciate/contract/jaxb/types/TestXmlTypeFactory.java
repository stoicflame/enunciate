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
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import junit.framework.Test;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;

/**
 * Tests the XmlTypeFactory.
 * 
 * @author Ryan Heaton
 */
public class TestXmlTypeFactory extends InAPTTestCase {

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

    //todo: beef up these tests
//    TypeDeclaration beanFourDecl = getDeclaration("org.codehaus.enunciate.samples.anotherschema.BeanFour");
//    XmlType beanFourXmlType = XmlTypeFactory.findSpecifiedType(beanFourDecl, beanFourDecl.getPackage());
//    assertEquals("The xml type for bean four should have been specified at the package-level.", "specified-bean-four", beanFourXmlType.getName());
//    assertEquals("The xml type for bean four should have been specified at the package-level.", "http://org.codehaus.enunciate/core/samples/beanfour", beanFourXmlType.getNamespace());

    DeclaredType beanFourType = Context.getCurrentEnvironment().getTypeUtils().getDeclaredType(getDeclaration("org.codehaus.enunciate.samples.anotherschema.BeanFour"));
    XmlType beanFourXmlType = XmlTypeFactory.getXmlType(beanFourType);
    assertEquals("The xml type for bean four should have been specified at the package-level.", "specified-bean-four", beanFourXmlType.getName());
    assertEquals("The xml type for bean four should have been specified at the package-level.", "http://org.codehaus.enunciate/core/samples/beanfour", beanFourXmlType.getNamespace());

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
