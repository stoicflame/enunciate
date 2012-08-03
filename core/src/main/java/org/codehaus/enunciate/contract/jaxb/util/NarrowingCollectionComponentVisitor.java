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

package org.codehaus.enunciate.contract.jaxb.util;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterUtil;
import org.codehaus.enunciate.contract.jaxb.types.*;
import org.codehaus.enunciate.util.MapType;
import org.codehaus.enunciate.util.MapTypeUtil;

import java.util.Iterator;

/**
 * Utility visitor for discovering the xml types of type mirrors.
 *
 * @author Ryan Heaton
 */
class NarrowingCollectionComponentVisitor implements TypeVisitor {

  private TypeMirror result;

  NarrowingCollectionComponentVisitor() {
  }

  public TypeMirror getResult() {
    return result;
  }

  public void visitTypeMirror(TypeMirror typeMirror) {
  }

  public void visitPrimitiveType(PrimitiveType primitiveType) {
  }

  public void visitVoidType(VoidType voidType) {
  }

  public void visitReferenceType(ReferenceType referenceType) {
  }

  public void visitDeclaredType(DeclaredType declaredType) {
  }

  public void visitClassType(ClassType classType) {
  }

  public void visitEnumType(EnumType enumType) {
  }

  public void visitInterfaceType(InterfaceType interfaceType) {
    AdapterType adapterType = AdapterUtil.findAdapterType(interfaceType.getDeclaration());
    if (adapterType == null) {
      //the interface isn't adapted, so we'll narrow it to java.lang.Object.
      AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
      this.result = env.getTypeUtils().getDeclaredType(env.getTypeDeclaration(Object.class.getName()));
    }
  }

  public void visitAnnotationType(AnnotationType annotationType) {
  }

  public void visitArrayType(ArrayType arrayType) {
  }

  public void visitTypeVariable(TypeVariable typeVariable) {
  }

  public void visitWildcardType(WildcardType wildcardType) {
  }

}
