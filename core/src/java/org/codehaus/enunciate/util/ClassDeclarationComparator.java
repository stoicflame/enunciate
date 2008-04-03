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

import java.util.Comparator;

/**
 * A comparator for instances of class declaration, comparing by fqn.
 *
 * @author Ryan Heaton
 */
public class ClassDeclarationComparator implements Comparator<ClassDeclaration> {

  public int compare(ClassDeclaration class1, ClassDeclaration class2) {
    return class1.getQualifiedName().compareTo(class2.getQualifiedName());
  }
}
