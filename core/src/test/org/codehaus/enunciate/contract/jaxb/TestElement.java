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
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import junit.framework.Test;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TestElement extends InAPTTestCase {

  @Override
  protected void setUp() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
  }

  /**
   * Tests a default, simple, single-valued accessor
   */
  public void testSimpleSingleValuedProperty() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "property4");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("property4", element.getName());
    assertNull("The namespace should be null because the elementFormDefault is unset.", element.getNamespace());
    assertNull(element.getRef());
    assertEquals(property.getPropertyType(), element.getAccessorType());
    assertEquals(KnownXmlType.STRING, element.getBaseType());
    assertFalse(element.isNillable());
    assertFalse(element.isRequired());
    assertEquals(0, element.getMinOccurs());
    assertEquals("1", element.getMaxOccurs());
    assertNull(element.getDefaultValue());
    assertFalse(element.isWrapped());
  }

  /**
   * Tests a byte[] accessor
   */
  public void testByteArrayProperty() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "property6");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("property6", element.getName());
    assertNull("The namespace should be null because the elementFormDefault is unset.", element.getNamespace());
    assertNull(element.getRef());
    assertEquals(property.getPropertyType(), element.getAccessorType());
    assertEquals(KnownXmlType.BASE64_BINARY, element.getBaseType());
    assertFalse(element.isNillable());
    assertFalse(element.isRequired());
    assertTrue(element.isBinaryData());
    assertEquals(0, element.getMinOccurs());
    assertEquals("1", element.getMaxOccurs());
    assertNull(element.getDefaultValue());
    assertFalse(element.isWrapped());

    //now let's pretend its a value...
    Value value = new Value(property, typeDef);
    assertEquals(null, value.getName());
    assertEquals(property.getPropertyType(), value.getAccessorType());
    assertEquals(KnownXmlType.BASE64_BINARY, value.getBaseType());
    assertTrue(value.isBinaryData());
    assertFalse(((DecoratedTypeMirror) value.getAccessorType()).isPrimitive());
  }

  /**
   * Tests a looping accessor, as per the jaxb spec, section 8.9.1.2
   */
  public void testLoopingProperty() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    ((EnunciateFreemarkerModel) FreemarkerModel.get()).add(typeDef);
    PropertyDeclaration property = findProperty(typeDef, "loopingProperty1");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("loopingProperty1", element.getName());
    assertNull("The namespace should be null because the elementFormDefault is unset.", element.getNamespace());
    QName ref = element.getRef();
    assertNotNull(ref);
    assertEquals("element-bean-one", ref.getLocalPart());
    assertEquals("urn:element-bean-one", ref.getNamespaceURI());
  }

  /**
   * Tests a simple, single-valued accessor with different specified values.
   */
  public void testSpecifiedSimpleSingleValuedProperty() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "property3");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("changedname", element.getName());
    assertEquals("urn:changedname", element.getNamespace());
    assertNull(element.getRef());
    assertEquals(property.getPropertyType(), element.getAccessorType());
    assertEquals(KnownXmlType.INT.getQname(), element.getBaseType().getQname());
    assertTrue(element.isNillable());
    assertTrue(element.isRequired());
    assertEquals(1, element.getMinOccurs());
    assertEquals("1", element.getMaxOccurs());
    assertEquals("6", element.getDefaultValue());
    assertFalse(element.isWrapped());
  }

  /**
   * Tests a single-valued accessor with a specified type.
   */
  public void testSpecifiedTypeSingleValuedProperty() throws Exception {
    EnumTypeDefinition enumDef = new EnumTypeDefinition((EnumDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.EnumBeanOne"));
    ((EnunciateFreemarkerModel) FreemarkerModel.get()).add(enumDef);
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "property5");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("property5", element.getName());
    assertNull(element.getNamespace());
    assertNull(element.getRef());
    DeclaredType enumType = Context.getCurrentEnvironment().getTypeUtils().getDeclaredType((TypeDeclaration) enumDef.getDelegate());
    assertTrue(element.getAccessorType().equals(enumType));
    XmlType baseType = element.getBaseType();
    assertEquals(enumDef.getName(), baseType.getName());
    assertEquals(enumDef.getNamespace(), baseType.getNamespace());
    assertFalse(element.isNillable());
    assertFalse(element.isRequired());
    assertEquals(0, element.getMinOccurs());
    assertEquals("1", element.getMaxOccurs());
    assertFalse(element.isWrapped());
  }

  /**
   * Tests a default single-choice collection property
   */
  public void testDefaultSingleChoiceCollectionProperty() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "elementsProperty2");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("elementsProperty2", element.getName());
    assertNull(element.getNamespace());
    assertNull(element.getRef());
    assertEquals(property.getPropertyType(), element.getAccessorType());
    assertEquals(KnownXmlType.DATE_TIME.getQname(), element.getBaseType().getQname());
    assertFalse(element.isNillable());
    assertFalse(element.isRequired());
    assertEquals(0, element.getMinOccurs());
    assertEquals("unbounded", element.getMaxOccurs());
    assertNull(element.getDefaultValue());
    assertFalse(element.isWrapped());
  }

  /**
   * Tests a single-choice collection property
   */
  public void testSingleChoiceCollectionProperty() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "elementsProperty1");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("item", element.getName());
    assertEquals("urn:item", element.getNamespace());
    assertNull(element.getRef());
    assertEquals(property.getPropertyType(), element.getAccessorType());
    assertEquals(KnownXmlType.ANY_TYPE.getQname(), element.getBaseType().getQname());
    assertFalse(element.isNillable());
    assertFalse(element.isRequired());
    assertEquals(0, element.getMinOccurs());
    assertEquals("unbounded", element.getMaxOccurs());
    assertNull(element.getDefaultValue());
    assertFalse(element.isWrapped());
  }

  /**
   * Tests a multi-choice collection property
   */
  public void testMultiChoiceCollectionProperty() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "elementsProperty3");
    Element element = new Element(property, typeDef);
    Collection<? extends Element> choices = element.getChoices();
    assertEquals(2, choices.size());
    for (Element choice : choices) {
      if ("durationItem".equals(choice.getName())) {
        assertEquals("urn:durationItem", choice.getNamespace());
        assertNull(choice.getRef());
        assertEquals(javax.xml.datatype.Duration.class.getName(), ((DeclaredType)choice.getAccessorType()).getDeclaration().getQualifiedName());
        assertEquals(KnownXmlType.DURATION.getQname(), choice.getBaseType().getQname());
        assertFalse(choice.isNillable());
        assertFalse(choice.isRequired());
        assertEquals(0, choice.getMinOccurs());
        assertEquals("1", choice.getMaxOccurs());
        assertNull(choice.getDefaultValue());
        assertFalse(choice.isWrapped());
      }
      else if ("imageItem".equals(choice.getName())) {
        assertEquals("urn:imageItem", choice.getNamespace());
        assertNull(choice.getRef());
        assertEquals(java.awt.Image.class.getName(), ((DeclaredType)choice.getAccessorType()).getDeclaration().getQualifiedName());
        assertEquals(KnownXmlType.BASE64_BINARY.getQname(), choice.getBaseType().getQname());
        assertFalse(choice.isNillable());
        assertFalse(choice.isRequired());
        assertEquals(0, choice.getMinOccurs());
        assertEquals("1", choice.getMaxOccurs());
        assertNull(choice.getDefaultValue());
        assertFalse(choice.isWrapped());
      }
      else {
        fail("Unknown choice: " + choice.getName());
      }
    }
  }

  /**
   * test a default wrapped element.
   */
  public void testDefaultWrappedElement() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "wrappedElementsProperty2");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("wrappedElementsProperty2", element.getName());
    assertNull(element.getNamespace());
    assertNull(element.getRef());
    assertEquals(property.getPropertyType(), element.getAccessorType());
    assertEquals(KnownXmlType.INTEGER.getQname(), element.getBaseType().getQname());
    assertFalse(element.isNillable());
    assertFalse(element.isRequired());
    assertEquals(0, element.getMinOccurs());
    assertEquals("unbounded", element.getMaxOccurs());
    assertNull(element.getDefaultValue());
    assertTrue(element.isWrapped());
    assertEquals("wrappedElementsProperty2", element.getWrapperName());
    assertEquals(typeDef.getNamespace(), element.getWrapperNamespace());
    assertFalse(element.isWrapperNillable());
  }

  /**
   * test a wrapped element.
   */
  public void testWrappedElement() throws Exception {
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    PropertyDeclaration property = findProperty(typeDef, "wrappedElementsProperty1");
    Element element = new Element(property, typeDef);
    assertEquals(1, element.getChoices().size());
    assertSame(element, element.getChoices().iterator().next());
    assertEquals("wrappedElementsProperty1", element.getName());
    assertNull(element.getNamespace());
    assertNull(element.getRef());
    assertEquals(property.getPropertyType(), element.getAccessorType());
    assertEquals(KnownXmlType.DECIMAL.getQname(), element.getBaseType().getQname());
    assertFalse(element.isNillable());
    assertFalse(element.isRequired());
    assertEquals(0, element.getMinOccurs());
    assertEquals("unbounded", element.getMaxOccurs());
    assertNull(element.getDefaultValue());
    assertTrue(element.isWrapped());
    assertEquals("wrapper1", element.getWrapperName());
    assertEquals("urn:wrapper1", element.getWrapperNamespace());
    assertTrue(element.isWrapperNillable());
  }

  protected PropertyDeclaration findProperty(DecoratedTypeDeclaration typeDef, String propertyName) {
    PropertyDeclaration prop = null;
    Collection<PropertyDeclaration> properties = typeDef.getProperties();
    for (PropertyDeclaration property : properties) {
      if (propertyName.equals(property.getSimpleName())) {
        prop = property;
      }
    }
    return prop;
  }

  public static Test suite() {
    return createSuite(TestElement.class);
  }
}
