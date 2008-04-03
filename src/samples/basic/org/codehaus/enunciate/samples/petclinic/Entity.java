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

/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package org.codehaus.enunciate.samples.petclinic;

/**
 * An object identifiable by id.
 *
 * @author Ryan Heaton
 */
public class Entity {

	private Integer id;

  /**
   * The id of the entity.
   *
   * @param id The id of the entity.
   */
  public void setId(Integer id) {
		this.id = id;
	}

  /**
   * The id of the entity.
   *
   * @return The id of the entity.
   */
  public Integer getId() {
		return id;
	}

  /**
   * Whether this entity is new.
   *
   * @return Whether this entity is new.
   */
  public boolean isNew() {
		return (this.id == null);
	}

}
