package org.codehaus.enunciate.modules.spring_app;

import org.springframework.security.ui.digestauth.DigestProcessingFilter;
import org.springframework.security.ui.digestauth.DigestProcessingFilterEntryPoint;
import org.springframework.security.ui.AuthenticationDetailsSource;
import org.springframework.security.providers.dao.UserCache;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.context.MessageSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Just a digest processing filter that is autowired.
 *
 * @author Ryan Heaton
 */
public class EnunciateDigestProcessingFilter extends DigestProcessingFilter {

  @Autowired (required = false)
  @Override
  public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource) {
    super.setAuthenticationDetailsSource(authenticationDetailsSource);
  }

  @Autowired (required = false)
  @Override
  public void setAuthenticationEntryPoint(DigestProcessingFilterEntryPoint authenticationEntryPoint) {
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
