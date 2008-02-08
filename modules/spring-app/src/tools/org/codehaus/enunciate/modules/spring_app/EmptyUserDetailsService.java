package org.codehaus.enunciate.modules.spring_app;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

/**
 * User details service with no users in it.
 *
 * @author Ryan Heaton
 */
public class EmptyUserDetailsService implements UserDetailsService {

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    throw new UsernameNotFoundException("No user details service is configured for this system.");
  }

}