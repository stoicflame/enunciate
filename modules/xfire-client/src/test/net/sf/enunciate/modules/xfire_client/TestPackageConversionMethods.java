package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.InAPTTestCase;

import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;

import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModelException;

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
    conversions.put("net.sf.enunciate.samples", "red.herring");
    conversions.put("net.sf.enunciate.samples.xfire_client.with.a.nested", "net.sf.enunciate.other.pckg.and.nested");
    ClientPackageForMethod packageForMethod = new ClientPackageForMethod(conversions);
    TypeDeclaration typeDeclaration = getDeclaration("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageClass");
    assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg", packageForMethod.exec(Arrays.asList(wrapper.wrap(typeDeclaration))));
    assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg", packageForMethod.exec(Arrays.asList(wrapper.wrap(typeDeclaration.getPackage()))));
    assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg.plus.extra", packageForMethod.exec(Arrays.asList(wrapper.wrap("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.plus.extra"))));
    assertEquals("red.herring.xfire_client", packageForMethod.exec(Arrays.asList(wrapper.wrap("net.sf.enunciate.samples.xfire_client"))));
    for (FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {
      if ("items".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("java.util", packageForMethod.exec(Arrays.asList(wrapper.wrap(fieldDeclaration.getType()))));
      }
      else if ("type".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg", packageForMethod.exec(Arrays.asList(wrapper.wrap(fieldDeclaration.getType()))));
      }
    }
  }

  /**
   * test the client classname for method.
   */
  public void testClientClassnameFor() throws Exception {
    HashMap<String, String> conversions = new HashMap<String, String>();
    conversions.put("net.sf.enunciate.samples", "red.herring");
    conversions.put("net.sf.enunciate.samples.xfire_client.with.a.nested", "net.sf.enunciate.other.pckg.and.nested");
    ClientClassnameForMethod classnameFor14Method = new ClientClassnameForMethod(conversions, false);
    ClientClassnameForMethod classnameFor15Method = new ClientClassnameForMethod(conversions, true);
    TypeDeclaration classDeclaration = getDeclaration("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageClass");
    assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg.NestedPackageClass", classnameFor14Method.convert(classDeclaration));
    assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg.NestedPackageClass", classnameFor15Method.convert(classDeclaration));
    for (FieldDeclaration fieldDeclaration : classDeclaration.getFields()) {
      if ("items".equals(fieldDeclaration.getSimpleName())) {
        assertEquals(Collection.class.getName(), classnameFor14Method.convert(fieldDeclaration.getType()));
        assertEquals("java.util.Collection<net.sf.enunciate.other.pckg.and.nested.pckg.NestedPackageItem>", classnameFor15Method.convert(fieldDeclaration.getType()));
      }
      else if ("type".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg.NestedPackageEnum", classnameFor14Method.convert(fieldDeclaration.getType()));
        assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg.NestedPackageEnum", classnameFor15Method.convert(fieldDeclaration.getType()));
      }
    }

    classDeclaration = getDeclaration("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageItem");
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
  }

  /**
   * tests the component type for method.
   */
  public void testComponentTypeFor() throws Exception {
    HashMap<String, String> conversions = new HashMap<String, String>();
    conversions.put("net.sf.enunciate.samples", "red.herring");
    conversions.put("net.sf.enunciate.samples.xfire_client.with.a.nested", "net.sf.enunciate.other.pckg.and.nested");
    ComponentTypeForMethod component14For = new ComponentTypeForMethod(conversions, false);
    ComponentTypeForMethod component15For = new ComponentTypeForMethod(conversions, true);
    TypeDeclaration classDeclaration = getDeclaration("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageClass");
    for (FieldDeclaration fieldDeclaration : classDeclaration.getFields()) {
      if ("items".equals(fieldDeclaration.getSimpleName())) {
        assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg.NestedPackageItem", component14For.convert(fieldDeclaration.getType()));
        assertEquals("net.sf.enunciate.other.pckg.and.nested.pckg.NestedPackageItem", component15For.convert(fieldDeclaration.getType()));
      }
      else if ("type".equals(fieldDeclaration.getSimpleName())) {
        try {
          component14For.convert(fieldDeclaration.getType());
          fail("A component type should only be suppored for an array or collection type.");
        }
        catch (TemplateModelException e) {
          //fall through.
        }

        try {
          component15For.convert(fieldDeclaration.getType());
          fail("A component type should only be suppored for an array or collection type.");
        }
        catch (TemplateModelException e) {
          //fall through.
        }
      }
    }

    classDeclaration = getDeclaration("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageItem");
    for (FieldDeclaration fieldDeclaration : classDeclaration.getFields()) {
      if ("property1".equals(fieldDeclaration.getSimpleName())) {
        try {
          component14For.convert(fieldDeclaration.getType());
          fail("A component type should only be suppored for an array or collection type.");
        }
        catch (TemplateModelException e) {
          //fall through.
        }

        try {
          component15For.convert(fieldDeclaration.getType());
          fail("A component type should only be suppored for an array or collection type.");
        }
        catch (TemplateModelException e) {
          //fall through.
        }
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
