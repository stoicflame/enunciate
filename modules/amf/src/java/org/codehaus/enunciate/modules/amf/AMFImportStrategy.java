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

import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.decorations.declaration.DecoratedMemberDeclaration;
import org.codehaus.enunciate.contract.jaxb.Attribute;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Strategy for looping through the (unique) AMF imports for a given declaration.  If a {@link org.codehaus.enunciate.contract.jaxws.WebMethod},
 * then the imports are those for the associated result event.  If a {@link org.codehaus.enunciate.contract.jaxws.EndpointInterface} then the
 * unique imports include the result events and parameters. If a {@link org.codehaus.enunciate.contract.jaxb.TypeDefinition}, then the unique
 * imports include each of the type's properties.
 *
 * @author Ryan Heaton
 */
public class AMFImportStrategy extends EnunciateTemplateLoopStrategy<String> {

  private String var = "amfImport";
  private DecoratedMemberDeclaration declaration;
  private final ClientClassnameForMethod classnameFor;

  public AMFImportStrategy(ClientClassnameForMethod classnameFor) {
    this.classnameFor = classnameFor;
  }

  protected Iterator<String> getLoop(TemplateModel model) throws TemplateException {
    final Set<String> imports = new HashSet<String>();
    try {
      if (declaration instanceof EndpointInterface) {
        for (WebMethod webMethod : ((EndpointInterface) declaration).getWebMethods()) {
          for (WebParam webParam : webMethod.getWebParameters()) {
            imports.add(classnameFor.convert(webParam));
          }

          WebResult webResult = webMethod.getWebResult();
          if (!webResult.isVoid()) {
            imports.add(classnameFor.convert((ImplicitChildElement) webResult));
          }
        }
      }
      else if (declaration instanceof TypeDefinition) {
        for (Attribute attribute : ((TypeDefinition) declaration).getAttributes()) {
          imports.add(classnameFor.convert(attribute));
        }
        for (Element element : ((TypeDefinition) declaration).getElements()) {
          imports.add(classnameFor.convert(element));
        }
        if (((TypeDefinition) declaration).getValue() != null) {
          imports.add(classnameFor.convert(((TypeDefinition) declaration).getValue()));
        }
      }
      else if (declaration instanceof WebMethod) {
        WebResult webResult = ((WebMethod) declaration).getWebResult();
        if (!webResult.isVoid()) {
          imports.add(classnameFor.convert((ImplicitChildElement) webResult));
        }
      }
      else {
        throw new TemplateException("The declaration must be either an endpoint interface or a web method.");
      }
    }
    catch (TemplateModelException e) {
      throw new TemplateException(e);
    }

    Iterator<String> importsIt = imports.iterator();
    while (importsIt.hasNext()) {
      //clear out any of the primitives and primary actionscript types; they don't need an import.
      String imprt = importsIt.next();
      if (imprt.lastIndexOf('.') < 0) {
        importsIt.remove();
      }
    }

    return imports.iterator();
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, String imprt, int index) throws TemplateException {
    super.setupModelForLoop(model, imprt, index);

    if (this.var != null) {
      getModel().setVariable(this.var, imprt);
    }
  }

  /**
   * The variable into which to store the current import.
   *
   * @return The variable into which to store the current import.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the current import.
   *
   * @param var The variable into which to store the current import.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * The declaration for which to get the unique imports.
   *
   * @return The declaration for which to get the unique imports.
   */
  public DecoratedMemberDeclaration getDeclaration() {
    return declaration;
  }

  /**
   * The declaration for which to get the unique imports.
   *
   * @param declaration The declaration for which to get the unique imports.
   */
  public void setDeclaration(DecoratedMemberDeclaration declaration) {
    this.declaration = declaration;
  }
}
