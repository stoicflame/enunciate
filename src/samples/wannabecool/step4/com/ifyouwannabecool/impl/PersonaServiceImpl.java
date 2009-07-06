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

package com.ifyouwannabecool.impl;

import com.ifyouwannabecool.api.PersonaService;
import com.ifyouwannabecool.api.PermissionDeniedException;
import com.ifyouwannabecool.domain.persona.Persona;

import javax.jws.WebService;
import javax.ws.rs.Path;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "com.ifyouwannabecool.api.PersonaService"
)
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
