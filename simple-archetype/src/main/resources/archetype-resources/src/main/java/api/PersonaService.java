#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * Copyright 2006 Web Cohesion Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package ${package}.api;

import javax.jws.WebService;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import ${package}.domain.persona.Persona;

/**
 * The persona services is used to perform actions on the data associated with a persona.
 * @author Ryan Heaton
 */
@WebService
public interface PersonaService
{

    /**
     * Reads a persona.
     * @param personaId The id of the persona to read.
     * @return The persona.
     */
    @Path("/{id}")
    @GET
    Persona readPersona(@PathParam("id") String personaId);

    @Path("/{id}.json")
    @GET
    @Produces("application/json")
    Persona readPersonaJson();

    /**
     * Store a persona.
     * @param persona The persona to store.
     */
    @POST
    void storePersona(Persona persona);

    /**
     * Delete a persona.
     * @param personaId The id of the persona to delete.
     * @return Whether the persona was successfully deleted.
     * @throws PermissionDeniedException If you don't have permission to delete the persona.
     */
    @Path("/{id}")
    @DELETE
    boolean deletePersona(@PathParam("id") String personaId) throws PermissionDeniedException;

}
