package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.persona.Persona;

import javax.jws.WebService;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@WebService
public interface PersonaService {

  Persona readPersona(String personaId);

  Collection<Persona> readPersonas(String... personaIds);

  void storePersona(Persona persona);

  boolean deletePersona(String personaId) throws PermissionDeniedException;

}
