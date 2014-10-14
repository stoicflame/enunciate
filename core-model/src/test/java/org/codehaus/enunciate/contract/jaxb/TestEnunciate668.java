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
import com.sun.mirror.declaration.TypeDeclaration;
import junit.framework.Test;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.types.MapXmlType;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Ryan Heaton
 * @see <a href="http://jira.codehaus.org/browse/ENUNCIATE-668">ENUNCIATE-668</a>
 */
public class TestEnunciate668 extends InAPTTestCase {

  public void testStaticInnerBaseTypeQName() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    ComplexTypeDefinition container = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ClassForEnunciateIssue668"));
    Collection<TypeDeclaration> nestedTypes = container.getNestedTypes();
    assertEquals(2, nestedTypes.size());
    Iterator<TypeDeclaration> ntit = nestedTypes.iterator();
    ComplexTypeDefinition contained = new ComplexTypeDefinition((ClassDeclaration) ntit.next());
    assertNotNull(contained.getBaseType().getQname());
    contained = new ComplexTypeDefinition((ClassDeclaration) ntit.next());
    assertTrue(contained.getBaseType() instanceof MapXmlType);
  }


  public static Test suite() {
    return createSuite(TestEnunciate668.class);
  }
}
