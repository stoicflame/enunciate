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

import com.sun.mirror.declaration.ClassDeclaration;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;

/**
 * @author Ryan Heaton
 */
public class TestXFireClientValidator extends InAPTTestCase {

  /**
   * Tests that we don't support an XML list of IDREFs yet.
   */
  public void testValidateXMLListofIDREFs() throws Exception {
    XFireClientValidator validator = new XFireClientValidator();
    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.xfire_client.InvalidComplexType");
    assertTrue("XmlLists of IDREFs shouldn't be supported yet.", validator.validateComplexType(new ComplexTypeDefinition(declaration)).hasErrors());
  }
}
