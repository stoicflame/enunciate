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

package org.codehaus.enunciate.contract.rest;

import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;

import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;

/**
 * A content type handler.
 *
 * @author Ryan Heaton
 */
public class ContentTypeHandler extends DecoratedClassDeclaration {

  private final TreeSet<String> contentTypes = new TreeSet<String>();

  public ContentTypeHandler(ClassDeclaration delegate) {
    super(delegate);

    org.codehaus.enunciate.rest.annotations.ContentTypeHandler handlerInfo = getAnnotation(org.codehaus.enunciate.rest.annotations.ContentTypeHandler.class);
    if (handlerInfo != null) {
      this.contentTypes.addAll(Arrays.asList(handlerInfo.contentTypes()));
    }
  }

  public Set<String> getSupportedContentTypes() {
    return contentTypes;
  }
}
