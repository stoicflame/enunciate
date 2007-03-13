package org.codehaus.enunciate.modules.xml;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxb.ElementComparator;
import org.codehaus.enunciate.contract.jaxb.Attribute;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class TestXMLValidator extends InAPTTestCase {

  /**
   * tests validating an endpoint interface
   */
  public void testValidateEndpointInterface() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.modules.xml.InvalidEndpointInterface");
    XMLValidator validator = new XMLValidator();

    EndpointInterface ei = new EndpointInterface(declaration) {
      @Override
      public Collection<WebMethod> getWebMethods() {
        ArrayList<WebMethod> filtered = new ArrayList<WebMethod>();
        for (WebMethod webMethod : super.getWebMethods()) {
          if ("requestWrapperHasDifferentTargetNS".equals(webMethod.getSimpleName())) {
            filtered.add(webMethod);
          }
        }
        return filtered;
      }
    };
    assertEquals("A request wrapper with a different target namespace that its endpoint interface shouldn't be supported.", 1, validator.validateEndpointInterface(ei).getErrors().size());

    ei = new EndpointInterface(declaration) {
      @Override
      public Collection<WebMethod> getWebMethods() {
        ArrayList<WebMethod> filtered = new ArrayList<WebMethod>();
        for (WebMethod webMethod : super.getWebMethods()) {
          if ("responseWrapperHasDifferentTargetNS".equals(webMethod.getSimpleName())) {
            filtered.add(webMethod);
          }
        }
        return filtered;
      }
    };
    assertEquals("A response wrapper with a different target namespace that its endpoint interface shouldn't be supported.", 1, validator.validateEndpointInterface(ei).getErrors().size());

    ei = new EndpointInterface(declaration) {
      @Override
      public Collection<WebMethod> getWebMethods() {
        ArrayList<WebMethod> filtered = new ArrayList<WebMethod>();
        for (WebMethod webMethod : super.getWebMethods()) {
          if ("webParamHasDifferentTargetNS".equals(webMethod.getSimpleName())) {
            filtered.add(webMethod);
          }
        }
        return filtered;
      }
    };
    assertEquals("A web param with a different target namespace that its endpoint interface shouldn't be supported.", 1, validator.validateEndpointInterface(ei).getErrors().size());

    ei = new EndpointInterface(declaration) {
      @Override
      public Collection<WebMethod> getWebMethods() {
        ArrayList<WebMethod> filtered = new ArrayList<WebMethod>();
        for (WebMethod webMethod : super.getWebMethods()) {
          if ("webResultHasDifferentTargetNS".equals(webMethod.getSimpleName())) {
            filtered.add(webMethod);
          }
        }
        return filtered;
      }
    };
    assertEquals("A web result with a different target namespace that its endpoint interface shouldn't be supported.", 1, validator.validateEndpointInterface(ei).getErrors().size());
  }

  /**
   * tests validating a complex type.
   */
  public void testValidateComplexType() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.modules.xml.InvalidComplexTypeBean");
    XMLValidator validator = new XMLValidator();

    ComplexTypeDefinition complexType = new ComplexTypeDefinition(declaration) {
      @Override
      public SortedSet<Element> getElements() {
        ElementComparator comparator = new ElementComparator(getPropertyOrder(), getAccessorOrder());
        SortedSet<Element> elementAccessors = new TreeSet<Element>(comparator);
        for (Element element : super.getElements()) {
          if ("property1".equals(element.getName())) {
            elementAccessors.add(element);
          }
        }
        return elementAccessors;
      }


      @Override
      public Collection<Attribute> getAttributes() {
        return Collections.emptyList();
      }
    };
    assertEquals("An element should not be supported if its namespace differs from that of its type definition.", 1, validator.validateComplexType(complexType).getErrors().size());

    complexType = new ComplexTypeDefinition(declaration) {
      @Override
      public SortedSet<Element> getElements() {
        ElementComparator comparator = new ElementComparator(getPropertyOrder(), getAccessorOrder());
        SortedSet<Element> elementAccessors = new TreeSet<Element>(comparator);
        for (Element element : super.getElements()) {
          if ("doubles".equals(element.getName())) {
            elementAccessors.add(element);
          }
        }
        return elementAccessors;
      }


      @Override
      public Collection<Attribute> getAttributes() {
        return Collections.emptyList();
      }
    };
    assertEquals("An element wrapper should not be supported if its namespace differs from that of its type definition.", 1, validator.validateComplexType(complexType).getErrors().size());

    complexType = new ComplexTypeDefinition(declaration) {
      @Override
      public SortedSet<Element> getElements() {
        return new TreeSet<Element>();
      }
    };
    assertEquals("An attribute should not be supported if its namespace differs from that of its type definition.", 1, validator.validateComplexType(complexType).getErrors().size());
  }
}
