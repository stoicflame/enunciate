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
import org.codehaus.enunciate.contract.jaxrs.*;
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
    assertEquals(4, root1.getResourceMethods().size());
    boolean getFound = false;
    boolean postFound = false;
    boolean putFound = false;
    boolean deleteFound = false;
    for (ResourceMethod resourceMethod : root1.getResourceMethods()) {
      assertEquals("/root1", resourceMethod.getFullpath());
      assertEquals(3, resourceMethod.getResourceParameters().size());
      if (resourceMethod.getHttpMethods().contains("GET")) {
        getFound = true;
        assertEquals("getOne", resourceMethod.getSimpleName());
      }
      else if (resourceMethod.getHttpMethods().contains("POST")) {
        postFound = true;
        assertEquals("setOne", resourceMethod.getSimpleName());
      }
      else if (resourceMethod.getHttpMethods().contains("PUT")) {
        putFound = true;
        assertEquals("putOne", resourceMethod.getSimpleName());
      }
      else if (resourceMethod.getHttpMethods().contains("DELETE")) {
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
    assertEquals(1, root2.getResourceParameters().size());
    assertEquals(8, root2.getResourceMethods().size());
    boolean getFound = false;
    boolean postFound = false;
    boolean putFound = false;
    boolean headFound = false;
    boolean get2Found = false;
    boolean post2Found = false;
    boolean put2Found = false;
    boolean delete2Found = false;
    for (ResourceMethod resourceMethod : root2.getResourceMethods()) {
      assertEquals(1, resourceMethod.getResourceParameters().size());
      if ("getTwo".equals(resourceMethod.getSimpleName())) {
        getFound = true;
        assertEquals(1, resourceMethod.getHttpMethods().size());
        assertEquals("GET", resourceMethod.getHttpMethods().iterator().next());
        assertEquals("/root2", resourceMethod.getFullpath());
        assertEquals("/root2", resourceMethod.getServletPattern());
      }
      else if ("setTwo".equals(resourceMethod.getSimpleName())) {
        postFound = true;
        assertEquals("POST", resourceMethod.getHttpMethods().iterator().next());
        assertEquals("/root2", resourceMethod.getFullpath());
        assertEquals("/root2", resourceMethod.getServletPattern());
      }
      else if ("putTwo".equals(resourceMethod.getSimpleName())) {
        putFound = true;
        assertEquals(1, resourceMethod.getHttpMethods().size());
        assertEquals("PUT", resourceMethod.getHttpMethods().iterator().next());
        assertEquals("/root2", resourceMethod.getFullpath());
        assertEquals("/root2", resourceMethod.getServletPattern());
      }
      else if ("deleteTwo".equals(resourceMethod.getSimpleName())) {
        headFound = true;
        assertEquals(1, resourceMethod.getHttpMethods().size());
        assertEquals("HEAD", resourceMethod.getHttpMethods().iterator().next());
        assertEquals("/root2", resourceMethod.getFullpath());
        assertEquals("/root2", resourceMethod.getServletPattern());
      }
      if ("getThree".equals(resourceMethod.getSimpleName())) {
        get2Found = true;
        assertEquals(1, resourceMethod.getHttpMethods().size());
        assertEquals("GET", resourceMethod.getHttpMethods().iterator().next());
        assertEquals("/root2/three", resourceMethod.getFullpath());
        assertEquals("/root2/three", resourceMethod.getServletPattern());
      }
      else if ("setThree".equals(resourceMethod.getSimpleName())) {
        post2Found = true;
        assertEquals(1, resourceMethod.getHttpMethods().size());
        assertEquals("POST", resourceMethod.getHttpMethods().iterator().next());
        assertEquals("/root2/three/two/one", resourceMethod.getFullpath());
        assertEquals("/root2/three/two/one", resourceMethod.getServletPattern());
      }
      else if ("putThree".equals(resourceMethod.getSimpleName())) {
        put2Found = true;
        assertEquals(1, resourceMethod.getHttpMethods().size());
        assertEquals("PUT", resourceMethod.getHttpMethods().iterator().next());
        assertEquals("/root2/three/{var}/one", resourceMethod.getFullpath());
        assertEquals("/root2/three/*", resourceMethod.getServletPattern());
      }
      else if ("deleteThree".equals(resourceMethod.getSimpleName())) {
        delete2Found = true;
        assertEquals(1, resourceMethod.getHttpMethods().size());
        assertEquals("DELETE", resourceMethod.getHttpMethods().iterator().next());
        assertEquals("/root2", resourceMethod.getFullpath());
        assertEquals("/root2", resourceMethod.getServletPattern());
      }
    }
    assertTrue(getFound && postFound && putFound && headFound);
    assertTrue(get2Found && post2Found && put2Found && delete2Found);

    assertEquals(1, root2.getResourceLocators().size());

    boolean get3Found = false;
    boolean post3Found = false;
    boolean put3Found = false;
    boolean delete3Found = false;
    SubResourceLocator resourceLocator = root2.getResourceLocators().get(0);
    assertSame(root2, resourceLocator.getParent());
    Resource root1 = resourceLocator.getResource();
    assertEquals(4, root1.getResourceParameters().size());
    for (ResourceMethod resourceMethod : root1.getResourceMethods()) {
      assertEquals(4, resourceMethod.getResourceParameters().size());
      assertEquals("/root2/subpath", resourceMethod.getFullpath());
      if (resourceMethod.getHttpMethods().contains("GET")) {
        get3Found = true;
        assertEquals("getOne", resourceMethod.getSimpleName());
      }
      else if (resourceMethod.getHttpMethods().contains("POST")) {
        post3Found = true;
        assertEquals("setOne", resourceMethod.getSimpleName());
      }
      else if (resourceMethod.getHttpMethods().contains("PUT")) {
        put3Found = true;
        assertEquals("putOne", resourceMethod.getSimpleName());
      }
      else if (resourceMethod.getHttpMethods().contains("DELETE")) {
        delete3Found = true;
        assertEquals("deleteOne", resourceMethod.getSimpleName());
      }
    }
    assertTrue(get3Found && post3Found && put3Found && delete3Found);

  }

  public static Test suite() {
    return createSuite(TestRootResource.class);
  }
}
