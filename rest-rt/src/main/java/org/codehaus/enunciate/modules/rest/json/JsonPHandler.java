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

package org.codehaus.enunciate.modules.rest.json;

import java.io.PrintWriter;

/**
 * Handler for writing the JSONP paramter.
 *
 * @author Ryan Heaton
 */
public abstract class JsonPHandler {

  private final String callbackName;

  public JsonPHandler(String callbackName) {
    this.callbackName = callbackName;
  }

  public void writeTo(PrintWriter outStream) throws Exception {
    if ((callbackName != null) && (callbackName.trim().length() > 0)) {
      outStream.print(callbackName);
      outStream.print("(");
    }

    writeBody(outStream);

    if (callbackName != null) {
      outStream.print(")");
    }
  }

  public abstract void writeBody(PrintWriter outStream) throws Exception;
}
