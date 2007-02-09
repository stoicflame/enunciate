package com.ifyouwannabecool.api;

import com.ifyouwannabecool.domain.link.Link;
import com.ifyouwannabecool.domain.link.SocialGroup;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public interface LinkageService {

  Link createLink(String persona1Id, String persona2Id) throws PermissionDeniedException;

  SocialGroup createSocialGroup(String groupName, Collection<String> personaIds, boolean exclusive);

  boolean addToSocialGroup(String groupId, String personaId) throws ExclusiveGroupException;

  SocialGroup[] readGroups(String personaId);
}
