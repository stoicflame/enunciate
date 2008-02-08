package org.codehaus.enunciate.modules.spring_app;

import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.ui.rememberme.RememberMeServices;
import org.acegisecurity.ui.AuthenticationDetailsSource;
import org.acegisecurity.ui.AuthenticationDetailsSourceImpl;
import org.acegisecurity.ui.logout.LogoutHandler;
import org.acegisecurity.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Helper for login/logout methods.
 *
 * @author Ryan Heaton
 */
public class LoginLogoutHelperImpl implements LoginLogoutHelper {

  private AuthenticationDetailsSource authenticationDetailsSource = new AuthenticationDetailsSourceImpl();
  private AuthenticationManager authenticationManager;
  private RememberMeServices rememberMeServices;
  private List<LogoutHandler> logoutHandlers;

  // Inherited.
  public void loginWithUsernameAndPassword(String username, String password) throws AuthenticationException {
    login(new UsernamePasswordAuthenticationToken(username, password));
  }

  // Inherited.
  public void login(Authentication authToken) {
    HTTPRequestContext httpContext = HTTPRequestContext.get();
    HttpServletRequest httpRequest = null;
    HttpServletResponse httpResponse = null;
    if (httpContext != null) {
      httpRequest = httpContext.getRequest();
      httpResponse = httpContext.getResponse();
    }

    if ((httpContext != null) && (authToken instanceof AbstractAuthenticationToken)) {
      ((AbstractAuthenticationToken)authToken).setDetails(authenticationDetailsSource.buildDetails(httpRequest));
    }

    try {
      Authentication authResult = authenticationManager.authenticate(authToken);

      SecurityContextHolder.getContext().setAuthentication(authResult);

      if ((httpContext != null) && (rememberMeServices != null)) {
        rememberMeServices.loginSuccess(httpRequest, httpResponse, authResult);
      }
    }
    finally {
      SecurityContextHolder.getContext().setAuthentication(null);

      if ((httpContext != null) && (rememberMeServices != null)) {
        rememberMeServices.loginFail(httpRequest, httpResponse);
      }
    }
  }

  // Inherited.
  public void logout() {
    HTTPRequestContext httpContext = HTTPRequestContext.get();
    if ((httpContext != null) && (this.logoutHandlers != null)) {
      HttpServletRequest httpRequest = httpContext.getRequest();
      HttpServletResponse httpResponse = httpContext.getResponse();
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      for (LogoutHandler logoutHandler : logoutHandlers) {
        logoutHandler.logout(httpRequest, httpResponse, authentication);
      }
    }
  }

  public AuthenticationDetailsSource getAuthenticationDetailsSource() {
    return authenticationDetailsSource;
  }

  public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource) {
    this.authenticationDetailsSource = authenticationDetailsSource;
  }

  public AuthenticationManager getAuthenticationManager() {
    return authenticationManager;
  }

  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  public RememberMeServices getRememberMeServices() {
    return rememberMeServices;
  }

  public void setRememberMeServices(RememberMeServices rememberMeServices) {
    this.rememberMeServices = rememberMeServices;
  }

  public List<LogoutHandler> getLogoutHandlers() {
    return logoutHandlers;
  }

  public void setLogoutHandlers(List<LogoutHandler> logoutHandlers) {
    this.logoutHandlers = logoutHandlers;
  }
}
