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

package org.codehaus.enunciate.contract.jaxws;

import java.util.Collection;

/**
 * A web message.  This could in include rpc-style parameters, web faults, header parameters, or in the case
 * of a document/literal wrapped method, the complex aggregate of the non-header input or output parameters.
 * <p/>
 * Each web method consists of a set of web messages.
 *
 * @author Ryan Heaton
 */
public interface WebMessage {

  /**
   * The name of this web message.
   *
   * @return The name of this web message.
   */
  String getMessageName();

  /**
   * The documentation for this web message.
   *
   * @return The documentation for this web message.
   */
  String getMessageDocs();

  /**
   * Whether this is an input message.
   *
   * @return Whether this is an input message.
   */
  boolean isInput();

  /**
   * Whether this is an output message.
   *
   * @return Whether this is an output message.
   */
  boolean isOutput();

  /**
   * Whether this message is a header parameter.
   *
   * @return Whether this message is a header parameter.
   */
  boolean isHeader();

  /**
   * Whether this message is a web fault.
   *
   * @return Whether this message is a web fault.
   */
  boolean isFault();

  /**
   * The parts of this complex input/output.
   *
   * @return The parts of this complex input/output.
   */
  Collection<WebMessagePart> getParts();
}
