package org.codehaus.enunciate.modules.spring_app;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.context.ApplicationContextException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;

import java.util.Map;

/**
 * User details service that looks up a delegate in the application context.  If no user details service
 * is configured in the application context, there will be no users abled to be loaded.
 *
 * @author Ryan Heaton
 */
public class DelegatingUserDetailsService extends ApplicationObjectSupport implements UserDetailsService {

  private UserDetailsService delegate;

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    Map<String, UserDetailsService> userDetailsServiceMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), UserDetailsService.class);
    for (UserDetailsService userDetailsService : userDetailsServiceMap.values()) {
      if (!userDetailsService.equals(this)) {
        if (delegate != null) {
          throw new ApplicationContextException("There are multiple beans of type org.acegisecurity.userdetails.UserDetailsService defined in the context.  Please specify which one to use in the Enunciate configuration file.");
        }
        else {
          delegate = userDetailsService;
        }
      }
    }
  }

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    if (delegate == null) {
      throw new UsernameNotFoundException("No user details service is configured for this system.");
    }
    else {
      return delegate.loadUserByUsername(username);
    }
  }

}