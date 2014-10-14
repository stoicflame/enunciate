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

package org.codehaus.enunciate.apt;

import com.sun.mirror.declaration.TypeDeclaration;

/**
 * Marker interface for deployment modules that want to listen for each type declaration inspected.
 *
 * @author Ryan Heaton
 */
public interface EnunciateTypeDeclarationListener {

  /**
   * Logic for handling an inspected type declaration.
   *
   * @param typeDeclaration The inspected type declaration.
   */
  void onTypeDeclarationInspected(TypeDeclaration typeDeclaration);
}