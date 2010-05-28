package org.codehaus.enunciate.modules.spring_app;

import org.springframework.context.MessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

/**
 * Just a digest processing filter that is autowired.
 *
 * @author Ryan Heaton
 */
public class EnunciateDigestAuthenticationFilter extends DigestAuthenticationFilter {

  @Autowired (required = false)
  @Override
  public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource) {
    super.setAuthenticationDetailsSource(authenticationDetailsSource);
  }

  @Autowired (required = false)
  @Override
  public void setAuthenticationEntryPoint(DigestAuthenticationEntryPoint authenticationEntryPoint) {
    super.setAuthenticationEntryPoint(authenticationEntryPoint);
  }

  @Autowired (required = false)
  @Override
  public void setMessageSource(MessageSource messageSource) {
    super.setMessageSource(messageSource);
  }

  @Autowired (required = false)
  @Override
  public void setUserCache(UserCache userCache) {
    super.setUserCache(userCache);
  }

  @Autowired (required = false)
  @Override
  public void setUserDetailsService(UserDetailsService userDetailsService) {
    super.setUserDetailsService(userDetailsService);
  }
}
