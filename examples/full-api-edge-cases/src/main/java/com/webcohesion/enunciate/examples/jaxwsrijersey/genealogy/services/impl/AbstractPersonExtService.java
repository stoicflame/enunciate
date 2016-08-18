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
package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services.impl;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.PersonExt;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Ryan Heaton
 */
public class AbstractPersonExtService {

  @GET
  @Path ("{id}")
  public PersonExt getPersonExt(@PathParam ("id") String id) {
    return new PersonExt();
  }

  @DELETE
  @Path("{id}")
  public void deletePersonExt(@PathParam("id") String id) {

  }
}
