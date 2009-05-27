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

package org.codehaus.enunciate.modules.csharp;

import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.TypeMirror;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebResult;

import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * Gets the QName of the request document for a given method.
 *
 * @author Ryan Heaton
 */
public class ResponseDocumentQNameMethod implements TemplateMethodModelEx {

  /**
   * Gets the client-side package for the type, type declaration, package, or their string values.
   *
   * @param list The arguments.
   * @return The string value of the client-side package.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The responseDocumentQName method method must have a web method as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (!(unwrapped instanceof WebMethod)) {
      throw new TemplateModelException("A web method must be provided.");
    }

    WebMethod webMethod = (WebMethod) unwrapped;
    if (webMethod.getSoapBindingStyle() != SOAPBinding.Style.DOCUMENT || webMethod.getSoapUse() != SOAPBinding.Use.LITERAL) {
      throw new TemplateModelException("No response document qname available for a " + webMethod.getSoapBindingStyle() + "/" + webMethod.getSoapUse() + " web method.");
    }
    if (webMethod.getResponseWrapper() != null) {
      return new QName(webMethod.getResponseWrapper().getElementNamespace(), webMethod.getResponseWrapper().getElementName());
    }
    else if (webMethod.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
      WebResult wr = webMethod.getWebResult();
      if (!wr.isHeader()) {
        return new QName(wr.getTargetNamespace(), wr.getElementName());
      }
    }

    return null;
  }

}