#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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

package ${package}.api;

import ${package}.domain.persona.Persona;

import javax.xml.ws.WebFault;

/**
 * Thrown when trying to add someone to a social group that is exclusive.
 * 
 * @author Ryan Heaton
 */
@WebFault
public class ExclusiveGroupException extends Exception {

  private String groupId;
  private Persona groupLeader;

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public Persona getGroupLeader() {
    return groupLeader;
  }

  public void setGroupLeader(Persona groupLeader) {
    this.groupLeader = groupLeader;
  }
}
