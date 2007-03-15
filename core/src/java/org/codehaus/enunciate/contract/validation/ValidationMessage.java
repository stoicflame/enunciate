/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.contract.validation;

import com.sun.mirror.util.SourcePosition;

/**
 * @author Ryan Heaton
 */
public class ValidationMessage {

  private SourcePosition position;
  private String text;

  public ValidationMessage(SourcePosition position, String text) {
    this.position = position;
    this.text = text;
  }

  /**
   * The source position of this validation message.
   *
   * @return The source position of this validation message.
   */
  public SourcePosition getPosition() {
    return position;
  }

  /**
   * The text of the message.
   *
   * @return The text of the message.
   */
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return getPosition() + ": " + getText();
  }


}
