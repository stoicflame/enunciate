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

package org.codehaus.enunciate.bytecode;

import junit.framework.TestCase;
import org.objectweb.asm.Type;

import javax.jws.WebParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class TestEnunciateInstrumentedClassLoader extends TestCase {

  /**
   * tests instrumenting annotations.
   */
  public void testInstrumentingAnnotations() throws Exception {
    final HashMap<MethodKey, String[]> parameterNames = new HashMap<MethodKey, String[]>();
    Class clazz = EIExample.class;
    Method exampleOneMethod = clazz.getMethod("exampleOne", Object.class, String.class, Float.TYPE);
    parameterNames.put(new MethodKey(Type.getInternalName(clazz), "exampleOne", Type.getMethodDescriptor(exampleOneMethod)), new String[]{"param1", "param2", "param3"});
    Method exampleTwoMethod = clazz.getMethod("exampleTwo", String.class, Integer.TYPE);
    parameterNames.put(new MethodKey(Type.getInternalName(clazz), "exampleTwo", Type.getMethodDescriptor(exampleTwoMethod)), new String[]{"param4", "param5"});
    InstrumentationInfo inst = new InstrumentationInfo(parameterNames);
    EnunciateInstrumentedClassLoader cl = new EnunciateInstrumentedClassLoader(Thread.currentThread().getContextClassLoader(), inst);
    clazz = cl.loadClass(clazz.getName());
    exampleOneMethod = clazz.getMethod("exampleOne", Object.class, String.class, Float.TYPE);
    Annotation[] param1Annotations = exampleOneMethod.getParameterAnnotations()[0];
    assertEquals(1, param1Annotations.length);
    assertEquals("param1", ((WebParam) param1Annotations[0]).name());
    Annotation[] param2Annotations = exampleOneMethod.getParameterAnnotations()[1];
    assertEquals(1, param2Annotations.length);
    assertEquals("param2", ((WebParam) param2Annotations[0]).name());
    Annotation[] param3Annotations = exampleOneMethod.getParameterAnnotations()[2];
    assertEquals(1, param3Annotations.length);
    assertEquals("param3", ((WebParam) param3Annotations[0]).name());
    exampleTwoMethod = clazz.getMethod("exampleTwo", String.class, Integer.TYPE);
    Annotation[] param4Annotations = exampleTwoMethod.getParameterAnnotations()[0];
    assertEquals(1, param4Annotations.length);
    assertEquals("hello", ((WebParam) param4Annotations[0]).name());
    Annotation[] param5Annotations = exampleTwoMethod.getParameterAnnotations()[1];
    assertEquals(1, param5Annotations.length);
    assertEquals("param5", ((WebParam) param5Annotations[0]).name());
  }

  public static abstract class EIExample {

    public String exampleOne(Object param1, @WebParam String param2, float param3) {
      return null;
    }

    public abstract String exampleTwo(@WebParam(name="hello") String param4, int param5);
  }

}
