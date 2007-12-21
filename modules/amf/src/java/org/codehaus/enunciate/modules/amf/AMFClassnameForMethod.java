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

package org.codehaus.enunciate.modules.amf;

import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.EnumType;
import freemarker.template.TemplateModelException;

import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class AMFClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  public AMFClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    if (typeMirror instanceof PrimitiveType) {
      //convert each primitive type to its wrapper class for reading/wring to ObjectInputStream.
      switch (((PrimitiveType) typeMirror).getKind()) {
        case BOOLEAN:
          return Boolean.class.getName();
        case BYTE:
          return Byte.class.getName();
        case CHAR:
          return Character.class.getName();
        case DOUBLE:
          return Double.class.getName();
        case FLOAT:
          return Float.class.getName();
        case INT:
          return Integer.class.getName();
        case LONG:
          return Long.class.getName();
        case SHORT:
          return Short.class.getName();
        default:
          throw new IllegalArgumentException();
      }
    }
    else if (typeMirror instanceof EnumType) {
      return String.class.getName();
    }

    return super.convert(typeMirror);
  }

}
