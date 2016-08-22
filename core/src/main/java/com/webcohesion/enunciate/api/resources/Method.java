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
package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.HasStyles;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface Method extends HasStyles {

  Resource getResource();

  String getLabel();

  String getHttpMethod();

  String getSlug();

  String getDescription();

  String getDeprecated();

  String getSince();

  String getVersion();

  boolean isIncludeDefaultParameterValues();

  List<? extends Parameter> getParameters();

  boolean isHasParameterConstraints();

  boolean isHasParameterMultiplicity();

  Entity getRequestEntity();

  List<? extends StatusCode> getResponseCodes();

  Entity getResponseEntity();

  List<? extends StatusCode> getWarnings();

  List<? extends Parameter> getResponseHeaders();

  Set<String> getSecurityRoles();

  Map<String, AnnotationMirror> getAnnotations();

  JavaDoc getJavaDoc();

  Example getExample();
}
