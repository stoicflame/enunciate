/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.PersonAdmin;
import com.webcohesion.enunciate.metadata.Facet;

import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * This is the admin service
 */
@Facet ( "http://enunciate.webcohesion.com/samples/full#admin" )
@WebService ( targetNamespace = "http://enunciate.webcohesion.com/samples/full" )
public interface AdminService {

  /**
   * This is the doc for read admin person.
   *
   * @param id The id.
   * @return The admin person.
   */
  @GET
  @Path ( "/admin/person/{id}" )
  @WebResult (name = "adminPerson")
  PersonAdmin readAdminPerson(@WebParam ( name = "adminId" ) @PathParam ( "id" ) String id);
}
