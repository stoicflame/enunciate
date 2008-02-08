package org.codehaus.enunciate.modules.spring_app;

import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;

/**
 * No-op provider.
 *
 * @author Ryan Heaton
 */
public class NoOpProvider implements AuthenticationProvider {

  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    return null;
  }

  public boolean supports(Class authentication) {
    return false;
  }
}
