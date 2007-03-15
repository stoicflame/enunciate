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

package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.link.Link;
import com.ifyouwannabecool.domain.link.SocialGroup;

import javax.jws.WebService;
import java.util.Collection;

/**
 * The linkage service is used to service the data for creating links between personas.  This
 * includes links and social groups.
 * 
 * @author Ryan Heaton
 */
@WebService
public interface LinkageService {

  /**
   * Creates a link between two personas.
   *
   * @param persona1Id The id of the first persona.
   * @param persona2Id The id of the second persona.
   * @return The link that was created.
   * @throws PermissionDeniedException If you don't have permission to create the link.
   */
  Link createLink(String persona1Id, String persona2Id) throws PermissionDeniedException;

  /**
   * Create a social group.
   *
   * @param groupLeader The id of the group leader.
   * @param memberIds The ids of the members of the group.
   * @param exclusive Whether the group is exclusive.
   * @return The group that was created.
   */
  SocialGroup createSocialGroup(String groupLeader, Collection<String> memberIds, boolean exclusive);

  /**
   * Adds a persona to a social group.
   *
   * @param groupId The id of the group to add the persona to.
   * @param personaId The id of the persona to add to the group.
   * @return Whether the persona was successfully added.
   * @throws ExclusiveGroupException If the group is exclusive.
   */
  boolean addToSocialGroup(String groupId, String personaId) throws ExclusiveGroupException;

  /**
   * Reads the social groups to which a specified persona belongs.
   *
   * @param personaId The id of the persona.
   * @return The social groups that were read.
   */
  SocialGroup[] readGroups(String personaId);
}
