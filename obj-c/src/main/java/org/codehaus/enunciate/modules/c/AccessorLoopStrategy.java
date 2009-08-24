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

package org.codehaus.enunciate.modules.c;

import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlClassType;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import org.codehaus.enunciate.template.freemarker.AccessorOverridesAnotherMethod;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Strategy for looping through all accessors (including those of the superclass) for a given type definition.
 *
 * @author Ryan Heaton
 */
public class AccessorLoopStrategy extends EnunciateTemplateLoopStrategy<Accessor> {

  private static final AccessorOverridesAnotherMethod OVERRIDE_CHECK = new AccessorOverridesAnotherMethod();

  private String var = "accessor";
  private TypeDefinition typeDefinition;
  private boolean attributes = true;
  private boolean elements = true;
  private boolean value = true;

  protected Iterator<Accessor> getLoop(TemplateModel model) throws TemplateException {
    TypeDefinition typeDef = getTypeDefinition();
    if (typeDef == null) {
      throw new TemplateException("A type definition must be supplied.");
    }

    List<Accessor> accessors = new ArrayList<Accessor>();
    if (attributes) {
      aggregateAttributes(accessors, typeDef);
    }
    if (value) {
      Value value = findValue(typeDef);
      if (value != null && !OVERRIDE_CHECK.overridesAnother(value)) {
        accessors.add(value);
      }
    }
    if (elements) {
      aggregateElements(accessors, typeDef);
    }
    return accessors.iterator();
  }

  private void aggregateAttributes(List<Accessor> accessors, TypeDefinition typeDef) {
    if (!typeDef.isBaseObject()) {
      XmlType baseType = typeDef.getBaseType();
      if (baseType instanceof XmlClassType) {
        aggregateAttributes(accessors, ((XmlClassType)baseType).getTypeDefinition());
      }
    }

    for (Attribute attribute : typeDef.getAttributes()) {
      if (!OVERRIDE_CHECK.overridesAnother(attribute)) {
        accessors.add(attribute);
      }
    }
  }

  private Value findValue(TypeDefinition typeDef) {
    if (typeDef.getValue() != null) {
      return typeDef.getValue();
    }
    else if (typeDef.isBaseObject()) {
      return null;
    }
    else {
      XmlType baseType = typeDef.getBaseType();
      return baseType instanceof XmlClassType ? findValue(((XmlClassType)baseType).getTypeDefinition()) : null;
    }
  }

  private void aggregateElements(List<Accessor> accessors, TypeDefinition typeDef) {
    if (!typeDef.isBaseObject()) {
      XmlType baseType = typeDef.getBaseType();
      if (baseType instanceof XmlClassType) {
        aggregateElements(accessors, ((XmlClassType)baseType).getTypeDefinition());
      }
    }

    for (Element element : typeDef.getElements()) {
      if (!OVERRIDE_CHECK.overridesAnother(element)) {
        accessors.add(element);
      }
    }
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, Accessor accessor, int index) throws TemplateException {
    super.setupModelForLoop(model, accessor, index);

    if (this.var != null) {
      getModel().setVariable(this.var, accessor);
    }
  }

  public String getVar() {
    return var;
  }

  public void setVar(String var) {
    this.var = var;
  }

  public TypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  public void setTypeDefinition(TypeDefinition typeDefinition) {
    this.typeDefinition = typeDefinition;
  }

  public boolean isAttributes() {
    return attributes;
  }

  public void setAttributes(boolean attributes) {
    this.attributes = attributes;
  }

  public boolean isElements() {
    return elements;
  }

  public void setElements(boolean elements) {
    this.elements = elements;
  }

  public boolean isValue() {
    return value;
  }

  public void setValue(boolean value) {
    this.value = value;
  }
}