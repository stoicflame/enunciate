package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.persona.Persona;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public interface PersonaService {

  Persona readPersona(String personaId);

  Collection<Persona> readPersonas(String... personaIds);

  void storePersona(Persona persona);

  boolean deletePersona(String personaId) throws PermissionDeniedException;

}
