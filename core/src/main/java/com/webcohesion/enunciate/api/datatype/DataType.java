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
package com.webcohesion.enunciate.api.datatype;

import com.webcohesion.enunciate.api.HasAnnotations;
import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface DataType extends HasStyles, HasAnnotations, HasFacets {

  String getLabel();

  String getSlug();

  String getDescription();

  String getDeprecated();

  Namespace getNamespace();

  Syntax getSyntax();

  BaseType getBaseType();

  List<DataTypeReference> getSupertypes();

  Set<DataTypeReference> getInterfaces();

  List<DataTypeReference> getSubtypes();

  String getSince();

  List<String> getSeeAlso();

  String getVersion();

  boolean isAbstract();

  Example getExample();

  List<? extends Value> getValues();

  List<? extends Property> getProperties();

  Map<String, String> getPropertyMetadata();

  JavaDoc getJavaDoc();

  Element getJavaElement();
  
  String getXmlName();
}
