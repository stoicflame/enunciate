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

package org.codehaus.enunciate.contract.jaxb;

import net.sf.jelly.apt.decorations.declaration.DecoratedMemberDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.type.TypeMirror;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementRef;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;

import org.codehaus.enunciate.ClientName;

/**
 * Used to wrap @XmlAnyElement.
 *
 * @author Ryan Heaton
 */
public class AnyElement extends DecoratedMemberDeclaration {

  private final boolean lax;
  private final List<ElementRef> refs;

  public AnyElement(MemberDeclaration delegate, TypeDefinition typeDef) {
    super(delegate);

    XmlAnyElement info = delegate.getAnnotation(XmlAnyElement.class);
    if (info == null) {
      throw new IllegalStateException("No @XmlAnyElement annotation.");
    }
    
    this.lax = info.lax();
    ArrayList<ElementRef> elementRefs = new ArrayList<ElementRef>();
    XmlElementRefs elementRefInfo = delegate.getAnnotation(XmlElementRefs.class);
    if (elementRefInfo != null && elementRefInfo.value() != null) {
      for (XmlElementRef elementRef : elementRefInfo.value()) {
        elementRefs.add(new ElementRef(delegate, typeDef, elementRef));
      }
    }
    refs = Collections.<ElementRef>unmodifiableList(elementRefs);
  }

  /**
   * Whether this is lax.
   *
   * @return Whether this is lax.
   */
  public boolean isLax() {
    return lax;
  }

  /**
   * Whether the any element is a collection.
   *
   * @return Whether the any element is a collection.
   */
  public boolean isCollectionType() {
    DecoratedTypeMirror accessorType;
    Declaration delegate = getDelegate();
    if (delegate instanceof FieldDeclaration) {
      accessorType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(((FieldDeclaration) delegate).getType());
    }
    else {
      accessorType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(((PropertyDeclaration) delegate).getPropertyType());
    }

    return accessorType.isInstanceOf(Collection.class.getName());
  }

  /**
   * The element refs.
   *
   * @return The element refs.
   */
  public List<ElementRef> getElementRefs() {
    return refs;
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = getSimpleName();
    ClientName clientName = getAnnotation(ClientName.class);
    if (clientName != null) {
      clientSimpleName = clientName.value();
    }
    return clientSimpleName;
  }

}
