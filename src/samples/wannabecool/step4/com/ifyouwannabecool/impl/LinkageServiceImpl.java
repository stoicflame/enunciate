package com.ifyouwannabecool.impl;

import com.ifyouwannabecool.api.LinkageService;
import com.ifyouwannabecool.api.PermissionDeniedException;
import com.ifyouwannabecool.api.ExclusiveGroupException;
import com.ifyouwannabecool.domain.link.Link;
import com.ifyouwannabecool.domain.link.SocialGroup;

import javax.jws.WebService;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "com.ifyouwannabecool.api.LinkageService"
)
public class LinkageServiceImpl implements LinkageService {

  public Link createLink(String persona1Id, String persona2Id) throws PermissionDeniedException {
    Link link = new Link();
    //store the link, throw the exception as necessary...
    return link;
  }

  public SocialGroup createSocialGroup(String groupLeader, Collection<String> memberIds, boolean exclusive) {
    SocialGroup socialGroup = new SocialGroup();

    //store the social group...
    return socialGroup;
  }

  public boolean addToSocialGroup(String groupId, String personaId) throws ExclusiveGroupException {
    //add the persona to the social group, throw the exception as necessary.
    return true;
  }

  public SocialGroup[] readGroups(String personaId) {
    SocialGroup[] groups = new SocialGroup[0];
    //read the groups from the db....
    return groups;
  }
}
