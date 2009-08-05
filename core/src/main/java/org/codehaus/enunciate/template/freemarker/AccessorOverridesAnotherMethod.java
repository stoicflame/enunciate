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

package org.codehaus.enunciate.template.freemarker;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class AccessorOverridesAnotherMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The accessorOverridesAnother method must have the accessor as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (!(unwrapped instanceof Accessor)) {
      throw new TemplateModelException("The accessorOverridesAnother method must have the accessor as a parameter.");
    }

    return overridesAnother((Accessor) unwrapped);
  }

  public Boolean overridesAnother(Accessor a) {
    String name = a.getSimpleName();
    EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
    TypeDeclaration declaringType = a.getDeclaringType();
    if (declaringType instanceof ClassDeclaration) {
      declaringType = ((ClassDeclaration) declaringType).getSuperclass().getDeclaration();
      while (declaringType instanceof ClassDeclaration && !Object.class.getName().equals(declaringType.getQualifiedName())) {
        TypeDefinition typeDef = model.findTypeDefinition((ClassDeclaration) declaringType);
        if (typeDef != null) {
          ArrayList<Accessor> accessors = new ArrayList<Accessor>();
          accessors.addAll(typeDef.getAttributes());
          accessors.add(typeDef.getValue());
          accessors.addAll(typeDef.getElements());
          for (Accessor accessor : accessors) {
            if (accessor != null && name.equals(accessor.getName())) {
              return Boolean.TRUE;
            }
          }
        }
        declaringType = ((ClassDeclaration) declaringType).getSuperclass().getDeclaration();
      }
    }

    return Boolean.FALSE;
  }

}
