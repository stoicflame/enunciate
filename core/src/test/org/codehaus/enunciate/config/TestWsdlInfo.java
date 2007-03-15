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

package org.codehaus.enunciate.config;

import com.sun.mirror.declaration.TypeDeclaration;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestWsdlInfo extends InAPTTestCase {

  /**
   * tests getting the imported namspaces
   */
  public void testGetImportedNamespaces() throws Exception {
    final TypeDeclaration decl = getDeclaration("org.codehaus.enunciate.samples.services.NamespacedWebService");
    EndpointInterface ei1 = new EndpointInterface(decl) {
      @Override
      public Set<String> getReferencedNamespaces() {
        return new HashSet<String>(Arrays.asList("urn:ns1", "urn:ns2", "urn:ns3"));
      }
    };
    EndpointInterface ei2 = new EndpointInterface(decl) {
      @Override
      public Set<String> getReferencedNamespaces() {
        return new HashSet<String>(Arrays.asList("urn:ns3", "urn:ns4", "urn:ns5"));
      }
    };

    WsdlInfo wsdlInfo = new WsdlInfo();
    wsdlInfo.getEndpointInterfaces().add(ei1);
    wsdlInfo.getEndpointInterfaces().add(ei2);
    Set<String> importedNamespaces = wsdlInfo.getImportedNamespaces();
    assertTrue(importedNamespaces.remove("urn:ns1"));
    assertTrue(importedNamespaces.remove("urn:ns2"));
    assertTrue(importedNamespaces.remove("urn:ns3"));
    assertTrue(importedNamespaces.remove("urn:ns4"));
    assertTrue(importedNamespaces.remove("urn:ns5"));
    assertTrue(importedNamespaces.remove("http://schemas.xmlsoap.org/wsdl/"));
    assertTrue(importedNamespaces.remove("http://schemas.xmlsoap.org/wsdl/http/"));
    assertTrue(importedNamespaces.remove("http://schemas.xmlsoap.org/wsdl/mime/"));
    assertTrue(importedNamespaces.remove("http://schemas.xmlsoap.org/wsdl/soap/"));
    assertTrue(importedNamespaces.remove("http://schemas.xmlsoap.org/soap/encoding/"));
    assertTrue(importedNamespaces.remove("http://www.w3.org/2001/XMLSchema"));
    assertTrue(importedNamespaces.isEmpty());
  }

  /**
   * tests getting the imported schemas.
   */
  public void testGetImportedSchemas() throws Exception {
    final SchemaInfo schema1 = new SchemaInfo();
    final SchemaInfo schema2 = new SchemaInfo();
    final SchemaInfo schema3 = new SchemaInfo();
    final SchemaInfo schema4 = new SchemaInfo();

    WsdlInfo wsdlInfo = new WsdlInfo() {
      @Override
      public Set<String> getImportedNamespaces() {
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

    final List<SchemaInfo> importedSchemas = wsdlInfo.getImportedSchemas();
    assertTrue(importedSchemas.remove(schema1));
    assertTrue(importedSchemas.remove(schema2));
    assertTrue(importedSchemas.remove(schema3));
    assertTrue(importedSchemas.remove(schema4));
    assertTrue(importedSchemas.isEmpty());
  }

  public static Test suite() {
    return createSuite(TestWsdlInfo.class);
  }
}
