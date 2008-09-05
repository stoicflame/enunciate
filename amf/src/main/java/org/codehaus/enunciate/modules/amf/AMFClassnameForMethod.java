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

package org.codehaus.enunciate.modules.amf;

import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.type.ArrayType;
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
    if ((typeMirror instanceof ArrayType) && (((ArrayType) typeMirror).getComponentType() instanceof PrimitiveType)) {
      //special case for primitive arrays.
      return super.convert(((ArrayType) typeMirror).getComponentType()) + "[]";
    }
    else if (typeMirror instanceof EnumType) {
      return String.class.getName();
    }

    return super.convert(typeMirror);
  }

}
