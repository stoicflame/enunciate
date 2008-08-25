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

package org.codehaus.enunciate.modules.xfire_client;

import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;
import org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod;
import org.codehaus.enunciate.template.freemarker.ComponentTypeForMethod;
import org.codehaus.enunciate.contract.jaxws.WebFault;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class TestPackageConversionMethods extends InAPTTestCase {

  /**
   * Tests the client package for method.
   */
  public void testClientPackageFor() throws Exception {
    BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
    HashMap<String, String> conversions = new HashMap<String, String>();
    conversions.put("org.codehaus.enunciate.samples", "red.herring");
    conversions.put("org.codehaus.enunciate.samples.xfire_client.with.a.nested", "org.codehaus.enunciate.other.pckg.and.nested");
    ClientPackageForMethod packageForMethod = new ClientPackageForMethod(conversions);
    TypeDeclaration typeDeclaration = getDeclaration("org.codehaus.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageClass");
    assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg", packageForMethod.exec(Arrays.asList(wrapper.wrap(typeDeclaration))));
    assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg", packageForMethod.exec(Arrays.asList(wrapper.wrap(typeDeclaration.getPackage()))));
    assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.plus.extra", packageForMethod.exec(Arrays.asList(wrapper.wrap("org.codehaus.enunciate.samples.xfire_client.with.a.nested.pckg.plus.extra"))));
    assertEquals("red.herring.xfire_client", packageForMethod.exec(Arrays.asList(wrapper.wrap("org.codehaus.enunciate.samples.xfire_client"))));
    for (FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {
      if ("items".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("java.util", packageForMethod.exec(Arrays.asList(wrapper.wrap(fieldDeclaration.getType()))));
      }
      else if ("type".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg", packageForMethod.exec(Arrays.asList(wrapper.wrap(fieldDeclaration.getType()))));
      }
    }
  }

  /**
   * test the client classname for method.
   */
  public void testClientClassnameFor() throws Exception {
    HashMap<String, String> conversions = new HashMap<String, String>();
    conversions.put("org.codehaus.enunciate.samples", "red.herring");
    conversions.put("org.codehaus.enunciate.samples.xfire_client.with.a.nested", "org.codehaus.enunciate.other.pckg.and.nested");
    ClientClassnameForMethod classnameFor14Method = new ClientClassnameForMethod(conversions);
    ClientClassnameForMethod classnameFor15Method = new ClientClassnameForMethod(conversions);
    classnameFor15Method.setJdk15(true);
    TypeDeclaration classDeclaration = getDeclaration("org.codehaus.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageClass");
    assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageClass", classnameFor14Method.convert(classDeclaration));
    assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageClass", classnameFor15Method.convert(classDeclaration));
    for (FieldDeclaration fieldDeclaration : classDeclaration.getFields()) {
      if ("items".equals(fieldDeclaration.getSimpleName())) {
        assertEquals(Collection.class.getName(), classnameFor14Method.convert(fieldDeclaration.getType()));
        assertEquals("java.util.Collection<org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageItem>", classnameFor15Method.convert(fieldDeclaration.getType()));
      }
      else if ("type".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageEnum", classnameFor14Method.convert(fieldDeclaration.getType()));
        assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageEnum", classnameFor15Method.convert(fieldDeclaration.getType()));
      }
    }

    classDeclaration = getDeclaration("org.codehaus.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageItem");
    for (FieldDeclaration fieldDeclaration : classDeclaration.getFields()) {
      if ("property1".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("boolean", classnameFor14Method.convert(fieldDeclaration.getType()));
        assertEquals("boolean", classnameFor15Method.convert(fieldDeclaration.getType()));
      }
      else if ("property2".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("int[]", classnameFor14Method.convert(fieldDeclaration.getType()));
        assertEquals("int[]", classnameFor15Method.convert(fieldDeclaration.getType()));
      }
    }

    try {
      classnameFor14Method.convert(classDeclaration.getPackage());
      fail("Converting a package to a classname shouldn't be suppored.");
    }
    catch (UnsupportedOperationException e) {
      //fall through.
    }

    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.xfire_client.BasicFaultTwo"));
    ImplicitChildElement childElement = webFault.getChildElements().iterator().next();
    assertEquals("java.lang.String", classnameFor14Method.convert(childElement));
  }

  /**
   * tests the component type for method.
   */
  public void testComponentTypeFor() throws Exception {
    HashMap<String, String> conversions = new HashMap<String, String>();
    conversions.put("org.codehaus.enunciate.samples", "red.herring");
    conversions.put("org.codehaus.enunciate.samples.xfire_client.with.a.nested", "org.codehaus.enunciate.other.pckg.and.nested");
    ComponentTypeForMethod component14For = new ComponentTypeForMethod(conversions);
    ComponentTypeForMethod component15For = new ComponentTypeForMethod(conversions);
    component15For.setJdk15(true);
    TypeDeclaration classDeclaration = getDeclaration("org.codehaus.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageClass");
    for (FieldDeclaration fieldDeclaration : classDeclaration.getFields()) {
      if ("items".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageItem", component14For.convert(fieldDeclaration.getType()));
        assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageItem", component15For.convert(fieldDeclaration.getType()));
      }
      else if ("type".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageEnum", component14For.convert(fieldDeclaration.getType()));
        assertEquals("org.codehaus.enunciate.other.pckg.and.nested.pckg.NestedPackageEnum", component15For.convert(fieldDeclaration.getType()));
      }
    }

    classDeclaration = getDeclaration("org.codehaus.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageItem");
    for (FieldDeclaration fieldDeclaration : classDeclaration.getFields()) {
      if ("property1".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("boolean", component14For.convert(fieldDeclaration.getType()));
        assertEquals("boolean", component15For.convert(fieldDeclaration.getType()));
      }
      else if ("property2".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("int", component14For.convert(fieldDeclaration.getType()));
        assertEquals("int", component15For.convert(fieldDeclaration.getType()));
      }
    }

    try {
      component14For.convert(classDeclaration.getPackage());
      fail("Converting a package to a classname shouldn't be suppored.");
    }
    catch (UnsupportedOperationException e) {
      //fall through.
    }
  }
}
