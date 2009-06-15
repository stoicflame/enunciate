package com.ifyouwannabecool.impl;

import com.ifyouwannabecool.api.ExclusiveGroupException;
import com.ifyouwannabecool.api.SocialGroupService;
import com.ifyouwannabecool.domain.link.SocialGroup;

import javax.ws.rs.Path;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@Path ("/group")
public class SocialGroupServiceImpl implements SocialGroupService {

  public SocialGroup createSocialGroup(String groupLeader, Collection<String> memberIds, boolean exclusive) {
    SocialGroup socialGroup = new SocialGroup();

    //store the social group...
    return socialGroup;
  }

  public boolean addToSocialGroup(String groupId, String personaId) throws ExclusiveGroupException {
    //add the persona to the social group, throw the exception as necessary.
    return true;
  }

  public SocialGroup readGroup(String groupId) {
    SocialGroup group = new SocialGroup();
    group.setId(groupId);
    return group;
  }
}
