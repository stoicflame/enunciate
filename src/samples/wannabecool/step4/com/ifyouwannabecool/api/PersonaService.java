package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.persona.Persona;

import javax.jws.WebService;
import java.util.Collection;

import org.codehaus.enunciate.rest.annotations.*;

/**
 * The persona services is used to perform actions on the data associated with a persona.
 *
 * @author Ryan Heaton
 */
@WebService
@RESTEndpoint
public interface PersonaService {

  /**
   * Reads a persona.
   *
   * @param personaId The id of the persona to read.
   * @return The persona.
   */
  @Verb ( VerbType.read )
  @Noun ( "persona" )
  Persona readPersona(@ProperNoun String personaId);

  /**
   * Reads a set of personas.
   *
   * @param personaIds The ids of the personas.
   * @return The personas.
   */
  Collection<Persona> readPersonas(String... personaIds);

  /**
   * Store a persona.
   *
   * @param persona The persona to store.
   */
  void storePersona(Persona persona);

  /**
   * Delete a persona.
   *
   * @param personaId The id of the persona to delete.
   * @return Whether the persona was successfully deleted.
   * @throws PermissionDeniedException If you don't have permission to delete the persona.
   */
  boolean deletePersona(String personaId) throws PermissionDeniedException;

}
