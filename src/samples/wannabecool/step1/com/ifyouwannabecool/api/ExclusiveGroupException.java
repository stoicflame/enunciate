package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.persona.Persona;

/**
 * @author Ryan Heaton
 */
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
