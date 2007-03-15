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

package org.codehaus.enunciate.contract.jaxb;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeMirror;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import com.sun.mirror.declaration.ClassDeclaration;
import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestSimpleTypeDefinition extends InAPTTestCase {

  /**
   * tests getting the base type.
   */
  public void testBaseType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    SimpleTypeDefinition simpleType = new SimpleTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.SimpleTypeSimpleContentBean"));
    XmlTypeMirror baseType = simpleType.getBaseType();
    assertEquals(KnownXmlType.INT.getQname(), baseType.getQname());
  }

  public static Test suite() {
    return createSuite(TestSimpleTypeDefinition.class);
  }
}
