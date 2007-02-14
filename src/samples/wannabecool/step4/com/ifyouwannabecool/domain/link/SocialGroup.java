package com.ifyouwannabecool.domain.link;

/**
 * A social group.
 *
 * @author Ryan Heaton
 */
public class SocialGroup {

  private String id;
  private String[] memberIds;
  private String groupLeaderId;
  private boolean exclusive;

  /**
   * The id of the social group.
   *
   * @return The id of the social group.
   */
  public String getId() {
    return id;
  }

  /**
   * The id of the social group.
   *
   * @param id The id of the social group.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * The ids of the members in the social group.
   *
   * @return The ids of the members in the social group.
   */
  public String[] getMemberIds() {
    return memberIds;
  }

  /**
   * The ids of the members in the social group.
   *
   * @param memberIds The ids of the members in the social group.
   */
  public void setMemberIds(String[] memberIds) {
    this.memberIds = memberIds;
  }

  /**
   * The id of the group leader.
   *
   * @return The id of the group leader.
   */
  public String getGroupLeaderId() {
    return groupLeaderId;
  }

  /**
   * The id of the group leader.
   *
   * @param groupLeaderId The id of the group leader.
   */
  public void setGroupLeaderId(String groupLeaderId) {
    this.groupLeaderId = groupLeaderId;
  }

  /**
   * Whether the group is exclusive.
   *
   * @return Whether the group is exclusive.
   */
  public boolean isExclusive() {
    return exclusive;
  }

  /**
   * Whether the group is exclusive.
   *
   * @param exclusive Whether the group is exclusive.
   */
  public void setExclusive(boolean exclusive) {
    this.exclusive = exclusive;
  }
}
