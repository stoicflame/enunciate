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

import com.sun.mirror.declaration.*;
import junit.framework.Test;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TestAccessor extends InAPTTestCase {

  /**
   * test the contructor of the accessor.
   */
  public void testConstructor() throws Exception {
    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne");
    MethodDeclaration methodDeclaration = declaration.getMethods().iterator().next();
    ConstructorDeclaration constructorDeclaration = declaration.getConstructors().iterator().next();
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition(declaration);

    try {
      new MockAccessor(declaration, typeDef);
      fail("TypeDeclarations should not be legal accessors.");
    }
    catch (IllegalArgumentException e) {
      //fall through.
    }

    try {
      new MockAccessor(methodDeclaration, typeDef);
      fail("MethodDeclarations should not be legal accessors.");
    }
    catch (IllegalArgumentException e) {
      //fall through.
    }

    try {
      new MockAccessor(constructorDeclaration, typeDef);
      fail("ConstructorDeclarations should not be legal accessors.");
    }
    catch (IllegalArgumentException e) {
      //fall through.
    }
  }

  /**
   * tests getting the accessor type.
   */
  public void testGetAccessorType() throws Exception {
    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne");
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition(declaration);
    FieldDeclaration fieldDeclaration = declaration.getFields().iterator().next();
    PropertyDeclaration propertyDeclaration = typeDef.getProperties().iterator().next();

    assertEquals(fieldDeclaration.getType(), new MockAccessor(fieldDeclaration, typeDef).getAccessorType());
    assertEquals(propertyDeclaration.getPropertyType(), new MockAccessor(propertyDeclaration, typeDef).getAccessorType());
  }

  /**
   * tests getting the base xml type of the accessor.
   */
  public void testGetBaseType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    ArrayList<QName> baseTypes = new ArrayList<QName>();
    baseTypes.add(new QName("http://www.w3.org/2001/XMLSchema", "ID"));
    baseTypes.add(new QName("http://www.w3.org/2001/XMLSchema", "IDREF"));
    baseTypes.add(new QName("http://www.w3.org/2001/XMLSchema", "integer"));
    baseTypes.add(new QName("http://www.w3.org/2001/XMLSchema", "int"));
    baseTypes.add(new QName("http://www.w3.org/2001/XMLSchema", "long"));
    baseTypes.add(new QName("http://ws-i.org/profiles/basic/1.1/xsd", "swaRef"));

    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.MultiAccessorTypeBean");
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition(declaration);
    Collection<PropertyDeclaration> properties = typeDef.getProperties();
    Collection<MockAccessor> accessors = new ArrayList<MockAccessor>(properties.size());
    for (PropertyDeclaration property : properties) {
      accessors.add(new MockAccessor(property, typeDef));
    }

    for (MockAccessor accessor : accessors) {
      assertTrue(baseTypes.remove(accessor.getBaseType().getQname()));
    }

    assertTrue(baseTypes.isEmpty());
  }

  /**
   * tests the methods to determine if an accessor is binary.
   */
  public void testBinaryMethods() throws Exception {
    //todo: implement this test.
  }

  private static final class MockAccessor extends Accessor {
    public MockAccessor(MemberDeclaration delegate, TypeDefinition typeDef) {
      super(delegate, typeDef);
    }

    public String getName() {
      return null;
    }

    public String getNamespace() {
      return null;
    }

    @Override
    public String getJsonMemberName() {
      return null;
    }
  }

  public static Test suite() {
    return createSuite(TestAccessor.class);
  }
}
