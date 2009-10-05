#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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

package ${package}.domain.link;

/**
 * A link between two personas.
 *
 * @author Ryan Heaton
 */
public class Link {

  private String persona1Id;
  private String persona2Id;

  /**
   * The id of the first persona.
   *
   * @return The id of the first persona.
   */
  public String getPersona1Id() {
    return persona1Id;
  }

  /**
   * The id of the first persona.
   *
   * @param persona1Id The id of the first persona.
   */
  public void setPersona1Id(String persona1Id) {
    this.persona1Id = persona1Id;
  }

  /**
   * The id of the second persona.
   *
   * @return The id of the second persona.
   */
  public String getPersona2Id() {
    return persona2Id;
  }

  /**
   * The id of the second persona.
   *
   * @param persona2Id The id of the second persona.
   */
  public void setPersona2Id(String persona2Id) {
    this.persona2Id = persona2Id;
  }
  
}
