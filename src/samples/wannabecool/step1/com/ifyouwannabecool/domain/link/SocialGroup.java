package com.ifyouwannabecool.domain.link;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class SocialGroup {

  private String id;
  private String[] memberIds;
  private String groupLeaderId;
  private boolean exclusive;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getMemberIds() {
    return memberIds;
  }

  public void setMemberIds(String[] memberIds) {
    this.memberIds = memberIds;
  }

  public String getGroupLeaderId() {
    return groupLeaderId;
  }

  public void setGroupLeaderId(String groupLeaderId) {
    this.groupLeaderId = groupLeaderId;
  }

  public boolean isExclusive() {
    return exclusive;
  }

  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }
}
