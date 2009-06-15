package com.ifyouwannabecool.impl;

import com.ifyouwannabecool.api.PermissionDeniedException;
import com.ifyouwannabecool.api.PersonaService;
import com.ifyouwannabecool.domain.persona.Persona;

import javax.ws.rs.Path;

/**
 * @author Ryan Heaton
 */
@Path ( "/persona" )
public class PersonaServiceImpl implements PersonaService {

  public Persona readPersona(String personaId) {
    Persona persona = new Persona();
    //...load the persona from the db, etc...
    return persona;
  }

  public void storePersona(Persona persona) {
    //... store the persona in the db...
  }

  public boolean deletePersona(String personaId) throws PermissionDeniedException {
    //..do the delete, throw permission denied as necessary...
    return true;
  }
}
