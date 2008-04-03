/*
 * Copyright 2006-2008 Web Cohesion
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

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import junit.framework.Test;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.Validator;

import javax.xml.bind.annotation.XmlAccessType;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TestTypeDefinition extends InAPTTestCase {

  /**
   * tests a basic type definition.
   */
  public void testBasicTypeDefinition() throws Exception {
    ClassDeclaration elementBeanOneDecl = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne");
    ComplexTypeDefinition elementBeanOneType = new ComplexTypeDefinition(elementBeanOneDecl);
    RootElementDeclaration elementBeanOne = new RootElementDeclaration(elementBeanOneDecl, elementBeanOneType);

    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);
    model.add(elementBeanOneType);
    model.add(elementBeanOne);

    TypeDefinition typeDef = new MockTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.FullTypeDefBeanOne"));
    assertEquals(1, typeDef.getAttributes().size());
    assertEquals("property1", typeDef.getAttributes().iterator().next().getSimpleName());

    assertNotNull(typeDef.getValue());
    assertEquals("property2", typeDef.getValue().getSimpleName());

    assertNotNull(typeDef.getXmlID());
    assertEquals(3, typeDef.getElements().size());
    assertTrue(typeDef.getElements().contains(typeDef.getXmlID()));

    assertEquals(XmlAccessType.PROPERTY, typeDef.getAccessType());

    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.schema.UnsupportedTypeDefBean");
    FieldDeclaration mixed = findField(declaration, "mixedProperty");
    FieldDeclaration anyElement = findField(declaration, "anyElementProperty");
    FieldDeclaration anyAttribute = findField(declaration, "anyAttributeProperty");
    assertTrue(typeDef.isUnsupported(mixed));
    assertTrue(typeDef.isUnsupported(anyElement));
    assertTrue(typeDef.isUnsupported(anyAttribute));
  }

  /**
   * tests a extended type definition.
   */
  public void testExtendedTypeDefinition() throws Exception {
    ClassDeclaration elementBeanOneDecl = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne");
    ComplexTypeDefinition elementBeanOneType = new ComplexTypeDefinition(elementBeanOneDecl);
    RootElementDeclaration elementBeanOne = new RootElementDeclaration(elementBeanOneDecl, elementBeanOneType);

    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);
    model.add(elementBeanOneType);
    model.add(elementBeanOne);

    TypeDefinition typeDef = new MockTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ExtendedFullTypeDefBeanOne"));
    assertEquals(XmlAccessType.PROPERTY, typeDef.getAccessType());
  }

  private static class MockTypeDefinition extends TypeDefinition {

    public MockTypeDefinition(ClassDeclaration delegate) {
      super(delegate);
    }

    public ValidationResult accept(Validator validator) {
      return null;
    }

    public XmlType getBaseType() {
      return null;
    }
  }

  protected FieldDeclaration findField(TypeDeclaration typeDef, String propertyName) {
    FieldDeclaration field = null;
    Collection<FieldDeclaration> fields = typeDef.getFields();
    for (FieldDeclaration fieldDeclaration : fields) {
      if (propertyName.equals(fieldDeclaration.getSimpleName())) {
        field = fieldDeclaration;
      }
    }
    return field;
  }

  public static Test suite() {
    return createSuite(TestTypeDefinition.class);
  }
}
