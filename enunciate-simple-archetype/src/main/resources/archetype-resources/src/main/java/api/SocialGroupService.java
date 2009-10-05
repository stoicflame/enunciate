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

import ${package}.domain.link.SocialGroup;

import javax.jws.WebService;
import javax.ws.rs.*;
import java.util.Collection;

/**
 * The linkage service is used to service the data for creating links between personas.  This
 * includes links and social groups.
 * 
 * @author Ryan Heaton
 */
@WebService
public interface SocialGroupService {

  /**
   * Read the specified social group.
   *
   * @param groupId The id of the group.
   * @return The social group.
   */
  @Path ("/{groupId}")
  @GET
  SocialGroup readGroup(@PathParam ("groupId") String groupId);

  /**
   * Create a social group.
   *
   * @param groupLeader The id of the group leader.
   * @param memberIds The ids of the members of the group.
   * @param exclusive Whether the group is exclusive.
   * @return The group that was created.
   */
  @POST
  SocialGroup createSocialGroup(@QueryParam("leader") String groupLeader,
                                @QueryParam("member") Collection<String> memberIds,
                                @QueryParam ("exclusive") boolean exclusive);

  /**
   * Adds a persona to a social group.
   *
   * @param groupId The id of the group to add the persona to.
   * @param personaId The id of the persona to add to the group.
   * @return Whether the persona was successfully added.
   * @throws ExclusiveGroupException If the group is exclusive.
   */
  @Path("/{groupId}")
  @POST
  boolean addToSocialGroup(@PathParam ("groupId") String groupId,
                           @QueryParam ("member") String personaId) throws ExclusiveGroupException;

}
