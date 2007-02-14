package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.persona.Persona;

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
