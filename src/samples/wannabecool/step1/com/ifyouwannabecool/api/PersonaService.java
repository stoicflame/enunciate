package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.persona.Persona;

import javax.ws.rs.*;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public interface PersonaService {

  @Path("/{id}")
  @GET
  Persona readPersona(@PathParam ("id") String personaId);

  @POST
  void storePersona(Persona persona);

  @Path("/{id}")
  @DELETE
  boolean deletePersona(@PathParam("id") String personaId) throws PermissionDeniedException;

}
