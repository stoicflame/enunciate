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

package org.codehaus.enunciate.util;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

import java.util.Comparator;

/**
 * A comparator for instances of class declaration, comparing by fqn.
 *
 * @author Ryan Heaton
 */
public class TypeDeclarationComparator implements Comparator<TypeDeclaration> {

  public int compare(TypeDeclaration type1, TypeDeclaration type2) {
    return type1.getQualifiedName().compareTo(type2.getQualifiedName());
  }
}
