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

package org.codehaus.enunciate.contract.validation;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import junit.framework.Test;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxws.EndpointImplementation;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Ryan Heaton
 */
public class TestDefaultValidator extends InAPTTestCase {

  public void testValidateEndpointInterface() throws Exception {
    final Counter implCounter = new Counter();
    final Counter methodCounter = new Counter();
    DefaultValidator validator = new DefaultValidator() {

      @Override
      public ValidationResult validateEndpointImplementation(EndpointImplementation impl) {
        implCounter.increment();
        return new ValidationResult();
      }

      @Override
      public ValidationResult validateWebMethod(WebMethod webMethod) {
        methodCounter.increment();
        return new ValidationResult();
      }
    };

    //test validation of JSR 181, secion 3.3
    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.services.NotAWebService");
    EndpointInterface ei = new EndpointInterface(declaration);
    assertTrue("A class not annotated with @WebService shouldn't be a valid endpoint interface (jsr 181: 3.3).", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("org.codehaus.enunciate.samples.services.InvalidEIReference");
    ei = new EndpointInterface(declaration);
    assertTrue("An endpoint implementation with an ei reference to another class shouldn't be valid.", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("org.codehaus.enunciate.samples.services.UnknownEIReference");
    ei = new EndpointInterface(declaration);
    assertTrue("An endpoint implementation with an ei reference to something unknown shouldn't be valid.", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("org.codehaus.enunciate.samples.services.InterfaceSpecifiedAsImplementation");
    //if an interface is specified as an implementation, it's still an endpoint interface, just not a valid one.
    ei = new EndpointInterface(declaration);
    assertTrue("An interface declaration shouldn't be allowed to specify another endpoint interface (jsr 181: 3.3).", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("org.codehaus.enunciate.samples.services.WebServiceWithoutUniqueMethodNames");
    //an unknown ei reference is correct, but not valid.
    ei = new EndpointInterface(declaration);
    assertTrue("An endpoint without unique web method names shouldn't be valid.", validator.validateEndpointInterface(ei).hasErrors());

    declaration = getDeclaration("org.codehaus.enunciate.samples.services.EncodedUseWebService");
    ei = new EndpointInterface(declaration);
    assertTrue("An encoded-use web wervice shouldn't be supported.", validator.validateEndpointInterface(ei).hasErrors());

    implCounter.reset();
    methodCounter.reset();
    declaration = getDeclaration("org.codehaus.enunciate.samples.services.NamespacedWebService");
    ei = new EndpointInterface(declaration);
    assertFalse("An encoded-use web wervice shouldn't be supported.", validator.validateEndpointInterface(ei).hasErrors());
    assertEquals(1, implCounter.getCount());
    assertEquals(1, methodCounter.getCount());
  }

  public void testValidateEndpointImplementation() throws Exception {
    DefaultValidator validator = new DefaultValidator();

    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.services.NoNamespaceWebService"));

    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.NotAWebService");
    EndpointImplementation impl = new EndpointImplementation(declaration, ei);
    assertTrue("A class not annotated with @WebService shouldn't be seen as an endpoint implementation.", validator.validateEndpointImplementation(impl).hasErrors());

    declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.InvalidEIReference");
    impl = new EndpointImplementation(declaration, ei);
    assertTrue("A class referencing an ei should be required to implement it.", validator.validateEndpointImplementation(impl).hasErrors());

    declaration = (EnumDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.EnumBeanOne");
    impl = new EndpointImplementation(declaration, ei);
    assertTrue("An enum declaration should not be a valid endpoint implementation.", validator.validateEndpointImplementation(impl).hasErrors());

    declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.NoNamespaceWebServiceImpl");
    impl = new EndpointImplementation(declaration, ei);
    assertFalse(validator.validateEndpointImplementation(impl).hasErrors());
  }

  /**
   * tests validating the REST API.
   */
  public void testValidateRESTAPI() throws Exception {
    //todo: implement.
  }

  public void testValidateWebMethod() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.services.WebMethodExamples")) {
      @Override
      public boolean isWebMethod(MethodDeclaration method) {
        return true;
      }
    };

    WebMethod privateMethod = null;
    WebMethod protectedMethod = null;
    WebMethod excludedMethod = null;
    WebMethod encodedMethod = null;
    WebMethod nonVoidOneWayMethod = null;
    WebMethod exceptionThrowingOneWayMethod = null;
    WebMethod headerCollectionParam = null;
    WebMethod headerCollectionReturn = null;
    WebMethod rpcBareMethod = null;
    WebMethod docBare2ParamMethod = null;
    WebMethod docBare2OutputMethod = null;
    WebMethod docBareWithHeadersMethod = null;
    WebMethod docBareVoidMethod = null;
    WebMethod docBareVoid2OutputMethod = null;
    WebMethod rpcCollectionParam = null;
    WebMethod invalidInOutParameter = null;
    Collection<WebMethod> webMethods = ei.getWebMethods();
    for (WebMethod webMethod : webMethods) {
      if ("privateMethod".equals(webMethod.getSimpleName())) {
        privateMethod = webMethod;
      }
      else if ("protectedMethod".equals(webMethod.getSimpleName())) {
        protectedMethod = webMethod;
      }
      else if ("excludedMethod".equals(webMethod.getSimpleName())) {
        excludedMethod = webMethod;
      }
      else if ("encodedMethod".equals(webMethod.getSimpleName())) {
        encodedMethod = webMethod;
      }
      else if ("nonVoidOneWayMethod".equals(webMethod.getSimpleName())) {
        nonVoidOneWayMethod = webMethod;
      }
      else if ("exceptionThrowingOneWayMethod".equals(webMethod.getSimpleName())) {
        exceptionThrowingOneWayMethod = webMethod;
      }
      else if ("headerCollectionParam".equals(webMethod.getSimpleName())) {
        headerCollectionParam = webMethod;
      }
      else if ("headerCollectionReturn".equals(webMethod.getSimpleName())) {
        headerCollectionReturn = webMethod;
      }
      else if ("rpcBareMethod".equals(webMethod.getSimpleName())) {
        rpcBareMethod = webMethod;
      }
      else if ("docBare2ParamMethod".equals(webMethod.getSimpleName())) {
        docBare2ParamMethod = webMethod;
      }
      else if ("docBare2OutputMethod".equals(webMethod.getSimpleName())) {
        docBare2OutputMethod = webMethod;
      }
      else if ("docBareWithHeadersMethod".equals(webMethod.getSimpleName())) {
        docBareWithHeadersMethod = webMethod;
      }
      else if ("docBareVoidMethod".equals(webMethod.getSimpleName())) {
        docBareVoidMethod = webMethod;
      }
      else if ("rpcCollectionParam".equals(webMethod.getSimpleName())) {
        rpcCollectionParam = webMethod;
      }
      else if ("docBareVoid2OutputMethod".equals(webMethod.getSimpleName())) {
        docBareVoid2OutputMethod = webMethod;
      }
      else if ("invalidInOutParameter".equals(webMethod.getSimpleName())) {
        invalidInOutParameter = webMethod;
      }
    }

    DefaultValidator validator = new DefaultValidator();
    assertTrue("A private method shouldn't be a web method.", validator.validateWebMethod(privateMethod).hasErrors());
    assertTrue("A protected method shouldn't be a web method.", validator.validateWebMethod(protectedMethod).hasErrors());
    assertTrue("An excluded method shouldn't be a web method.", validator.validateWebMethod(excludedMethod).hasErrors());
    assertTrue("An encoded method shouldn't be valid.", validator.validateWebMethod(encodedMethod).hasErrors());
    assertTrue("A one-way non-void web method shouldn't be valid.", validator.validateWebMethod(nonVoidOneWayMethod).hasErrors());
    assertTrue("An exception-throwing one-way method shouldn't be valid.", validator.validateWebMethod(exceptionThrowingOneWayMethod).hasErrors());
    assertTrue("An rpc/bare method shouldn't be valid.", validator.validateWebMethod(rpcBareMethod).hasErrors());
    assertTrue("A method with a collection or an array as a header parameter should be warned.", validator.validateWebMethod(headerCollectionParam).hasWarnings());
    assertTrue("A method with a collection or an array as a header return should be warned.", validator.validateWebMethod(headerCollectionReturn).hasWarnings());
    assertTrue("A doc/bare method shouldn't be valid if it has 2 params.", validator.validateWebMethod(docBare2ParamMethod).hasErrors());
    assertTrue("A doc/bare method shouldn't be valid if it has 2 outputs.", validator.validateWebMethod(docBare2OutputMethod).hasErrors());
    assertFalse("A doc/bare method should be allowed to have headers.", validator.validateWebMethod(docBareWithHeadersMethod).hasErrors());
    assertFalse("A doc/bare void method should be valid.", validator.validateWebMethod(docBareVoidMethod).hasErrors());
    assertTrue("A doc/bare method shouldn't be valid if it has 2 outputs.", validator.validateWebMethod(docBareVoid2OutputMethod).hasErrors());
    assertTrue("An rpc method with a collection or array parameter should be warned.", validator.validateWebMethod(rpcCollectionParam).hasWarnings());
    assertTrue("An INOUT parameter must be a holder.", validator.validateWebMethod(invalidInOutParameter).hasErrors());
  }

  /**
   * test validating a complex type.
   */
  public void testValidateComplexType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    final Counter typeDefCounter = new Counter();
    DefaultValidator validator = new DefaultValidator() {
      @Override
      public ValidationResult validateTypeDefinition(TypeDefinition typeDef) {
        typeDefCounter.increment();
        return new ValidationResult();
      }
    };

    ComplexTypeDefinition complexType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.SimpleTypeComplexContentBean"));
    model.add(complexType);

    complexType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ComplexTypeWithValueAndElements"));
    assertTrue("A complex type definition should not be valid if it has both elements and a value.", validator.validateComplexType(complexType).hasErrors());
    assertEquals(1, typeDefCounter.getCount());
  }

  /**
   * test validating a simple type.
   */
  public void testValidateSimpleType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    final Counter typeDefCounter = new Counter();
    DefaultValidator validator = new DefaultValidator() {
      @Override
      public ValidationResult validateTypeDefinition(TypeDefinition typeDef) {
        typeDefCounter.increment();
        return new ValidationResult();
      }
    };

    ComplexTypeDefinition complexType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.SimpleTypeComplexContentBean"));
    model.add(complexType);
    SimpleTypeDefinition simpleType = new SimpleTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.SimpleTypeThatExtendsComplexType"));
    assertTrue("A simple type definition should not be valid if it exends a complex type.", validator.validateSimpleType(simpleType).hasErrors());
    assertEquals(1, typeDefCounter.getCount());

    simpleType = new SimpleTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.SimpleTypeWithoutAValue"));
    assertTrue("A simple type definition should not be valid if it doesn't have an xml value.", validator.validateSimpleType(simpleType).hasErrors());
  }

  /**
   * tests validating an enum type.
   */
  public void testValidateEnumType() throws Exception {
    final Counter simpleTypeCounter = new Counter();
    DefaultValidator validator = new DefaultValidator() {
      // Inherited.
      @Override
      public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
        simpleTypeCounter.increment();
        return new ValidationResult();
      }
    };

    EnumTypeDefinition enumType = new EnumTypeDefinition((EnumDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.EnumBeanOne"));
    assertFalse(validator.validateEnumType(enumType).hasErrors());
    assertEquals(1, simpleTypeCounter.getCount());
  }

  /**
   * test validating a type definition
   */
  public void testValidateTypeDefinition() throws Exception {
    ClassDeclaration elementBeanOneDecl = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne");
    ComplexTypeDefinition elementBeanOneType = new ComplexTypeDefinition(elementBeanOneDecl);
    RootElementDeclaration elementBeanOne = new RootElementDeclaration(elementBeanOneDecl, elementBeanOneType);

    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(elementBeanOneType);
    model.add(elementBeanOne);
    FreemarkerModel.set(model);

    final Counter packageCounter = new Counter();
    final Counter attributeCounter = new Counter();
    final Counter valueCounter = new Counter();
    final Counter elementCounter = new Counter();
    final Counter elementRefCounter = new Counter();
    final Counter xmlIdCounter = new Counter();

    DefaultValidator validator = new DefaultValidator() {
      @Override
      public ValidationResult validatePackage(Schema schema) {
        packageCounter.increment();
        return new ValidationResult();
      }

      @Override
      public ValidationResult validateAttribute(Attribute attribute) {
        attributeCounter.increment();
        return new ValidationResult();
      }

      @Override
      public ValidationResult validateElement(Element element) {
        elementCounter.increment();
        return new ValidationResult();
      }

      @Override
      public ValidationResult validateElementRef(ElementRef elementRef) {
        elementRefCounter.increment();
        return new ValidationResult();
      }

      @Override
      public ValidationResult validateValue(Value value) {
        valueCounter.increment();
        return new ValidationResult();
      }

      @Override
      public ValidationResult validateXmlID(Accessor accessor) {
        xmlIdCounter.increment();
        return new ValidationResult();
      }
    };

    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.schema.ComplexTypeWithValueAndElements");
    ComplexTypeDefinition invalidNestedType = new ComplexTypeDefinition((ClassDeclaration) declaration.getNestedTypes().iterator().next());
    assertTrue("A non-static nested type should not be a valid type definition.", validator.validateTypeDefinition(invalidNestedType).hasErrors());

    declaration = getDeclaration("org.codehaus.enunciate.samples.schema.ExtendedFullTypeDefBeanOne");
    ComplexTypeDefinition validNestedType = new ComplexTypeDefinition((ClassDeclaration) declaration.getNestedTypes().iterator().next());
    resetCounters(packageCounter, attributeCounter, valueCounter, elementCounter, elementRefCounter, xmlIdCounter);
    assertFalse("A public static nested type should be a valid type definition.", validator.validateTypeDefinition(validNestedType).hasErrors());
    assertEquals(1, packageCounter.getCount());
    assertEquals(0, attributeCounter.getCount());
    assertEquals(0, valueCounter.getCount());
    assertEquals(1, elementCounter.getCount());
    assertEquals(0, elementRefCounter.getCount());
    assertEquals(0, xmlIdCounter.getCount());

    //test out the factory methods...
    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.InvalidFactoryMethodBean"));
    assertTrue("A non-static factory method should not be valid.", validator.validateTypeDefinition(typeDef).hasErrors());
    typeDef = new ComplexTypeDefinition((ClassDeclaration) typeDef.getNestedTypes().iterator().next());
    assertTrue("An unknown factory method should not be valid.", validator.validateTypeDefinition(typeDef).hasErrors());
    typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.FactoryMethodBean"));
    assertFalse("A factory method bean should be valid", validator.validateTypeDefinition(typeDef).hasErrors());

    typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.InvalidConstructorBean"));
    assertTrue("A public, no-arg constructor should have been required.", validator.validateTypeDefinition(typeDef).hasErrors());

    resetCounters(packageCounter, attributeCounter, valueCounter, elementCounter, elementRefCounter, xmlIdCounter);
    typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.SimpleTypeSimpleContentBean"));
    assertFalse(validator.validateTypeDefinition(typeDef).hasErrors());
    assertEquals(1, packageCounter.getCount());
    assertEquals(0, attributeCounter.getCount());
    assertEquals(1, valueCounter.getCount());
    assertEquals(0, elementCounter.getCount());
    assertEquals(0, elementRefCounter.getCount());
    assertEquals(0, xmlIdCounter.getCount());

    resetCounters(packageCounter, attributeCounter, valueCounter, elementCounter, elementRefCounter, xmlIdCounter);
    typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.SimpleTypeComplexContentBean"));
    assertFalse(validator.validateTypeDefinition(typeDef).hasErrors());
    assertEquals(1, packageCounter.getCount());
    assertEquals(2, attributeCounter.getCount());
    assertEquals(1, valueCounter.getCount());
    assertEquals(0, elementCounter.getCount());
    assertEquals(0, elementRefCounter.getCount());
    assertEquals(0, xmlIdCounter.getCount());

    resetCounters(packageCounter, attributeCounter, valueCounter, elementCounter, elementRefCounter, xmlIdCounter);
    typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementRefBeanOne"));
    assertFalse(validator.validateTypeDefinition(typeDef).hasErrors());
    assertEquals(1, packageCounter.getCount());
    assertEquals(0, attributeCounter.getCount());
    assertEquals(0, valueCounter.getCount());
    assertEquals(0, elementCounter.getCount());
    assertEquals(3, elementRefCounter.getCount());
    assertEquals(0, xmlIdCounter.getCount());

    resetCounters(packageCounter, attributeCounter, valueCounter, elementCounter, elementRefCounter, xmlIdCounter);
    typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne"));
    assertFalse(validator.validateTypeDefinition(typeDef).hasErrors());
    assertEquals(1, packageCounter.getCount());
    assertEquals(0, attributeCounter.getCount());
    assertEquals(0, valueCounter.getCount());
    assertEquals(12, elementCounter.getCount());
    assertEquals(0, elementRefCounter.getCount());
    assertEquals(0, xmlIdCounter.getCount());

    resetCounters(packageCounter, attributeCounter, valueCounter, elementCounter, elementRefCounter, xmlIdCounter);
    typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.XMLIDBean"));
    assertFalse(validator.validateTypeDefinition(typeDef).hasErrors());
    assertEquals(1, packageCounter.getCount());
    assertEquals(0, attributeCounter.getCount());
    assertEquals(1, valueCounter.getCount());
    assertEquals(0, elementCounter.getCount());
    assertEquals(0, elementRefCounter.getCount());
    assertEquals(1, xmlIdCounter.getCount());

    //todo: test that child elements, wrapper elements, and attributes of the same name aren't allowed. 
  }

  /**
   * tests validating a package.
   */
  public void testValidatePackage() throws Exception {
    //todo: care enough to test this one?
  }

  /**
   * tests validating an attribute.
   */
  public void testValidateAttribute() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne")));
    FreemarkerModel.set(model);

    final Counter accessorCounter = new Counter();
    DefaultValidator validator = new DefaultValidator() {
      @Override
      public ValidationResult validateAccessor(Accessor accessor) {
        accessorCounter.increment();
        return new ValidationResult();
      }
    };

    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.AttributeBeanWithComplexType"));
    Attribute attribute = new Attribute(typeDef.getProperties().iterator().next(), typeDef);
    assertTrue("An attribute shouldn't not have a complex type definition.", validator.validateAttribute(attribute).hasErrors());
    assertEquals(1, accessorCounter.getCount());

    //todo: test that attributes of a different namespace then their type defs aren't allowed if the form is qualified.
    //todo: test that attributes NOT of the default namespace aren't allowed if their form is unqualified.
  }

  /**
   * tests validating an value.
   */
  public void testValidateValue() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne")));
    FreemarkerModel.set(model);

    final Counter accessorCounter = new Counter();
    DefaultValidator validator = new DefaultValidator() {
      @Override
      public ValidationResult validateAccessor(Accessor accessor) {
        accessorCounter.increment();
        return new ValidationResult();
      }
    };

    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ValueBeanWithComplexType"));
    Value value = new Value(typeDef.getProperties().iterator().next(), typeDef);
    assertTrue("An value shouldn't not have a complex type definition.", validator.validateValue(value).hasErrors());
    assertEquals(1, accessorCounter.getCount());
  }

  /**
   * tests validating an element.
   */
  public void testValidateElement() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    final Counter accessorCounter = new Counter();
    DefaultValidator validator = new DefaultValidator() {
      @Override
      public ValidationResult validateAccessor(Accessor accessor) {
        accessorCounter.increment();
        return new ValidationResult();
      }
    };

    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.InvalidElementBean"));
    Iterator<PropertyDeclaration> propIt = typeDef.getProperties().iterator();
    PropertyDeclaration property1 = propIt.next();
    Element element = new Element(property1, typeDef);
    assertTrue("An typed collection element shouldn't have multiple XmlElements.", validator.validateElement(element).hasErrors());
    assertEquals(1, accessorCounter.getCount());

    //todo: test that elements of a different namespace then their type defs aren't allowed if the form is qualified.
    //todo: test that elements NOT of the default namespace aren't allowed if their form is unqualified.
    //todo: test especially that the items of @XmlElements that have no name specified cause this validation error to trigger...
  }

  /**
   * tests validating an element ref.
   */
  public void testValidateElementRef() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ElementBeanOne")));
    FreemarkerModel.set(model);

    final Counter accessorCounter = new Counter();
    DefaultValidator validator = new DefaultValidator() {
      @Override
      public ValidationResult validateAccessor(Accessor accessor) {
        accessorCounter.increment();
        return new ValidationResult();
      }
    };

    ComplexTypeDefinition typeDef = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.InvalidElementRefBean"));
    Iterator<PropertyDeclaration> propIt = typeDef.getProperties().iterator();
    PropertyDeclaration property1 = propIt.next();
    ElementRef ref = new ElementRef(property1, typeDef);
    assertTrue(validator.validateElementRef(ref).hasErrors());
    assertEquals(1, accessorCounter.getCount());
  }

  /**
   * test validating an accessor
   */
  public void testValidateAccessor() throws Exception {
    //todo: care enough to implement?
  }

  /**
   * tests validating an xml id.
   */
  public void testValidateXmlID() throws Exception {
    //todo: care enough to implement?
  }

  public static void resetCounters(Counter... counters) {
    for (Counter counter : counters) {
      counter.reset();
    }
  }

  public static Test suite() {
    return createSuite(TestDefaultValidator.class);
  }
}
