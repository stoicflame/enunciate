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

}
