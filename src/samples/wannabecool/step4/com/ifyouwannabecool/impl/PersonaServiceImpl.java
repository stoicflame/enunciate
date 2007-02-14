package com.ifyouwannabecool.impl;

import com.ifyouwannabecool.api.PersonaService;
import com.ifyouwannabecool.api.PermissionDeniedException;
import com.ifyouwannabecool.domain.persona.Persona;

import javax.jws.WebService;
import java.util.Collection;
import java.util.ArrayList;

import net.sf.enunciate.rest.annotations.RESTEndpoint;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "com.ifyouwannabecool.api.PersonaService"
)
@RESTEndpoint
public class PersonaServiceImpl implements PersonaService {


  public Persona readPersona(String personaId) {
    Persona persona = new Persona();
    //...load the persona from the db, etc...
    return persona;
  }

  public Collection<Persona> readPersonas(String... personaIds) {
    ArrayList<Persona> personas = new ArrayList<Persona>();
    //... do the load, etc.
    return personas;
  }

  public void storePersona(Persona persona) {
    //... store the persona in the db...
  }

  public boolean deletePersona(String personaId) throws PermissionDeniedException {
    //..do the delete, throw permission denied as necessary...
    return true;
  }
}
