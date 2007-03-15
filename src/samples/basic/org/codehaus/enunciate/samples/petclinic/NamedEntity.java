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

/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package org.codehaus.enunciate.samples.petclinic;

/**
 * An entity with an associated name.
 *
 * @author Ryan Heaton
 */
public class NamedEntity extends Entity {

	private String name;

  /**
   * The name of the entity.
   *
   * @param name The name of the entity.
   */
  public void setName(String name) {
		this.name = name;
	}

  /**
   * The name of the entity.
   *
   * @return The name of the entity.
   */
  public String getName() {
		return this.name;
	}

}
