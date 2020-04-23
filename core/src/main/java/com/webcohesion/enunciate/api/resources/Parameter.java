/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.HasAnnotations;
import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface Parameter extends HasStyles, HasAnnotations {

  String getName();

  String getDescription();

  String getTypeLabel();

  String getTypeName();

  String getTypeFormat();

  String getDefaultValue();

  String getConstraints();

  Set<String> getConstraintValues();

  JavaDoc getJavaDoc();

  boolean isMultivalued();
}
