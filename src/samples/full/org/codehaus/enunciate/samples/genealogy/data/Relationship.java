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

package org.codehaus.enunciate.samples.genealogy.data;

/**
 * A relationship between two people.
 *
 * @author Ryan Heaton
 */
public class Relationship extends Assertion {

  private String id;
  private RelationshipType type;
  private Name sourcePersonName;
  private Name targetPersonName;

  /**
   * The id of the relationship.
   *
   * @return The id of the relationship.
   */
  public String getId() {
    return id;
  }

  /**
   * The id of the relationship.
   *
   * @param id The id of the relationship.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The relationship type.
   *
   * @return The relationship type.
   */
  public RelationshipType getType() {
    return type;
  }

  /**
   * The relationship type.
   *
   * @param type The relationship type.
   */
  public void setType(RelationshipType type) {
    this.type = type;
  }

  /**
   * The name of the source person.
   *
   * @return The name of the source person.
   */
  public Name getSourcePersonName() {
    return sourcePersonName;
  }

  /**
   * The name of the source person.
   *
   * @param sourcePersonName The name of the source person.
   */
  public void setSourcePersonName(Name sourcePersonName) {
    this.sourcePersonName = sourcePersonName;
  }

  /**
   * The name of the target person.
   *
   * @return The name of the target person.
   */
  public Name getTargetPersonName() {
    return targetPersonName;
  }

  /**
   * The name of the target person.
   *
   * @param targetPersonName The name of the target person.
   */
  public void setTargetPersonName(Name targetPersonName) {
    this.targetPersonName = targetPersonName;
  }

}
