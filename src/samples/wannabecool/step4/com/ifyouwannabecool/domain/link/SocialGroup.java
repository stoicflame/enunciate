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
