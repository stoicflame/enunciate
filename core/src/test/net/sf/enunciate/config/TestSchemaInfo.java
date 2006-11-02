package net.sf.enunciate.config;

import com.sun.mirror.declaration.ClassDeclaration;
import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class TestSchemaInfo extends InAPTTestCase {

  /**
   * getting the referenced namespaces of the schema info.
   */
  public void testGetReferencedNamespaces() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    final ComplexTypeDefinition beanThree = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.BeanThree"));
    final ComplexTypeDefinition beanFour = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.BeanFour"));
    final ComplexTypeDefinition simpleTypeComplexContentBean = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.SimpleTypeComplexContentBean"));
    final RootElementDeclaration beanThreeElement = new RootElementDeclaration((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.BeanThree"), beanThree);
    final RootElementDeclaration beanFourElement = new RootElementDeclaration((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.BeanFour"), beanFour);

    SchemaInfo schemaInfo = new SchemaInfo() {
      @Override
      public Collection<TypeDefinition> getTypeDefinitions() {
        return Arrays.asList((TypeDefinition) beanThree, beanFour, simpleTypeComplexContentBean);
      }

      @Override
      public Collection<RootElementDeclaration> getGlobalElements() {
        return Arrays.asList(beanThreeElement, beanFourElement);
      }
    };

    Set<String> referencedNamespaces = schemaInfo.getReferencedNamespaces();
    assertTrue(referencedNamespaces.remove("http://net.sf.enunciate/core/samples/another"));
    assertTrue(referencedNamespaces.remove("urn:BeanFour"));
    assertTrue(referencedNamespaces.remove("urn:schema.BeanThree.property1"));
    assertTrue(referencedNamespaces.remove("urn:SimpleTypeComplexContentBean.Property2"));
    assertTrue(referencedNamespaces.remove("urn:SimpleTypeComplexContentBean.Property3"));
    assertEquals(0, referencedNamespaces.size());
  }

  /**
   * Getting the imported schemas.
   */
  public void testGetImportedSchemas() throws Exception {
    final SchemaInfo schema1 = new SchemaInfo();
    final SchemaInfo schema2 = new SchemaInfo();
    final SchemaInfo schema3 = new SchemaInfo();
    final SchemaInfo schema4 = new SchemaInfo();

    SchemaInfo schemaInfo = new SchemaInfo() {
      @Override
      public Set<String> getReferencedNamespaces() {
        return new HashSet<String>(Arrays.asList("urn:ns1", "urn:ns2", "urn:ns3", "urn:ns4", "urn:ns5"));
      }

      @Override
      protected SchemaInfo lookupSchema(String namespace) {
        if (namespace.endsWith("ns1")) {
          return schema1;
        }
        else if (namespace.endsWith("ns2")) {
          return schema2;
        }
        else if (namespace.endsWith("ns3")) {
          return schema3;
        }
        else if (namespace.endsWith("ns4")) {
          return schema4;
        }
        else {
          return null;
        }
      }
    };
    schemaInfo.setNamespace("urn:testGetImportedSchemas");
    List<SchemaInfo> importedSchemas = schemaInfo.getImportedSchemas();
    assertTrue(importedSchemas.remove(schema1));
    assertTrue(importedSchemas.remove(schema2));
    assertTrue(importedSchemas.remove(schema3));
    assertTrue(importedSchemas.remove(schema4));
    assertEquals(1, importedSchemas.size());
    assertEquals("urn:ns5", importedSchemas.get(0).getNamespace());
  }

}
