package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.link.Link;
import com.ifyouwannabecool.domain.link.SocialGroup;

import javax.jws.WebService;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@WebService
public interface LinkageService {

  Link createLink(String persona1Id, String persona2Id) throws PermissionDeniedException;

  SocialGroup createSocialGroup(String groupLeader, Collection<String> memberIds, boolean exclusive);

  boolean addToSocialGroup(String groupId, String personaId) throws ExclusiveGroupException;

  SocialGroup[] readGroups(String personaId);
}
