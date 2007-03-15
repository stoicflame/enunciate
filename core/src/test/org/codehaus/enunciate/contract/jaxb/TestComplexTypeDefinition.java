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

import com.sun.mirror.declaration.ClassDeclaration;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeMirror;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestComplexTypeDefinition extends InAPTTestCase {

  /**
   * tests getting the base type.
   */
  public void testGetBaseType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    ComplexTypeDefinition complexContentType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne"));
    assertEquals(KnownXmlType.ANY_TYPE, complexContentType.getBaseType());
    model.add(complexContentType);
    ComplexTypeDefinition exComplexContentType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ExtendedBeanOne"));
    XmlTypeMirror baseType = exComplexContentType.getBaseType();
    assertEquals(complexContentType.getName(), baseType.getName());
    assertEquals(complexContentType.getNamespace(), baseType.getNamespace());
  }

  /**
   * tests getting the content type of the complex type.
   */
  public void testGetContentType() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);
    ComplexTypeDefinition simpleContentType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.anotherschema.SimpleTypeComplexContentBean"));
    assertEquals(ContentType.SIMPLE, simpleContentType.getContentType());
    ComplexTypeDefinition complexContentType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne"));
    model.add(complexContentType);
    assertEquals(ContentType.IMPLIED, complexContentType.getContentType());
    ComplexTypeDefinition exComplexContentType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.ExtendedBeanOne"));
    assertEquals(ContentType.COMPLEX, exComplexContentType.getContentType());
    ComplexTypeDefinition emptyContentType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne")) {
      @Override
      public SortedSet<Element> getElements() {
        return new TreeSet<Element>();
      }
    };
    assertEquals(ContentType.EMPTY, emptyContentType.getContentType());
  }

  public static Test suite() {
    return createSuite(TestComplexTypeDefinition.class);
  }
}
