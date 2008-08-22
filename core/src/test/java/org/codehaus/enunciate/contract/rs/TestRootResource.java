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

package org.codehaus.enunciate.contract.rs;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.ResourceParameter;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;

import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestRootResource extends InAPTTestCase {

  /**
   * tests a basic root resource.
   */
  public void testBasicResource() throws Exception {
    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.rs.RootResource1");
    RootResource root1 = new RootResource((ClassDeclaration) declaration);
    assertEquals(4, root1.getResouceMethods().size());
    boolean getFound = false;
    boolean postFound = false;
    boolean putFound = false;
    boolean deleteFound = false;
    for (ResourceMethod resourceMethod : root1.getResouceMethods()) {
      if ("GET".equals(resourceMethod.getHttpMethod())) {
        getFound = true;
        assertEquals("getOne", resourceMethod.getSimpleName());
      }
      else if ("POST".equals(resourceMethod.getHttpMethod())) {
        postFound = true;
        assertEquals("setOne", resourceMethod.getSimpleName());
      }
      else if ("PUT".equals(resourceMethod.getHttpMethod())) {
        putFound = true;
        assertEquals("putOne", resourceMethod.getSimpleName());
      }
      else if ("DELETE".equals(resourceMethod.getHttpMethod())) {
        deleteFound = true;
        assertEquals("deleteOne", resourceMethod.getSimpleName());
      }
    }
    assertTrue(getFound && postFound && putFound && deleteFound);
    assertEquals(3, root1.getResourceParameters().size());
    Set<String> params = new TreeSet<String>(Arrays.asList("field1", "field2", "property1"));
    for (ResourceParameter resourceParameter : root1.getResourceParameters()) {
      params.remove(resourceParameter.getParameterName());
    }
    assertTrue(params.isEmpty());

  }

  /**
   * tests annotation inheritance
   */
  public void testAnnotationInheritance() throws Exception {
    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.rs.RootResource2Impl");
    RootResource root2 = new RootResource((ClassDeclaration) declaration);
    assertEquals("root2", root2.getPath());
    assertEquals(4, root2.getResouceMethods().size());
    boolean getFound = false;
    boolean postFound = false;
    boolean putFound = false;
    boolean deleteFound = false;
    boolean headFound = false;
    for (ResourceMethod resourceMethod : root2.getResouceMethods()) {
      assertEquals("/root2", resourceMethod.getFullpath());
      if ("GET".equals(resourceMethod.getHttpMethod())) {
        getFound = true;
        assertEquals("getTwo", resourceMethod.getSimpleName());
      }
      else if ("POST".equals(resourceMethod.getHttpMethod())) {
        postFound = true;
        assertEquals("setTwo", resourceMethod.getSimpleName());
      }
      else if ("PUT".equals(resourceMethod.getHttpMethod())) {
        putFound = true;
        assertEquals("putTwo", resourceMethod.getSimpleName());
      }
      else if ("DELETE".equals(resourceMethod.getHttpMethod())) {
        fail();
      }
      else if ("HEAD".equals(resourceMethod.getHttpMethod())) {
        headFound = true;
        assertEquals("deleteTwo", resourceMethod.getSimpleName());
      }
    }
    assertTrue(getFound && postFound && putFound && headFound);
    assertFalse(deleteFound);

  }

  public static Test suite() {
    return createSuite(TestRootResource.class);
  }
}
