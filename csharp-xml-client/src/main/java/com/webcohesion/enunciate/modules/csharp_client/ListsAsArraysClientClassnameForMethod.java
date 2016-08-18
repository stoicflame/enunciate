/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.csharp_client;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import freemarker.template.TemplateModelException;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;

/**
 * Conversion from java types to C# types, converting lists to arrays.
 *
 * @author Ryan Heaton
 * @link http://livedocs.adobe.com/flex/2/docs/wwhelp/wwhimpl/common/html/wwhelp.htm?context=LiveDocs_Parts&file=00001104.html#270405
 */
public class ListsAsArraysClientClassnameForMethod extends ClientClassnameForMethod {

  public ListsAsArraysClientClassnameForMethod(Map<String, String> conversions, EnunciateJaxbContext context) {
    super(conversions, context);
  }


  @Override
  protected String getCollectionTypeConversion(DeclaredType declaredType) throws TemplateModelException {
    List<? extends TypeMirror> actualTypeArguments = declaredType.getTypeArguments();
    if (actualTypeArguments.size() == 1) {
      return convert(actualTypeArguments.iterator().next()) + "[]";
    }
    else {
      return "object[]";
    }
  }

}