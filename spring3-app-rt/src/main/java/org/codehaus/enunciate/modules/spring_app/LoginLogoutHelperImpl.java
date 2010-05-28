/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.spring_app;

import org.springframework.beans.factory.annotation.Autowired;
import org.codehaus.enunciate.webapp.HTTPRequestContext;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;

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
    catch (RuntimeException e) {
      SecurityContextHolder.getContext().setAuthentication(null);

      if ((httpContext != null) && (rememberMeServices != null)) {
        rememberMeServices.loginFail(httpRequest, httpResponse);
      }

      throw e;
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

  @Autowired (required = false)
  public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource) {
    this.authenticationDetailsSource = authenticationDetailsSource;
  }

  public AuthenticationManager getAuthenticationManager() {
    return authenticationManager;
  }

  @Autowired (required = false)
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  public RememberMeServices getRememberMeServices() {
    return rememberMeServices;
  }

  @Autowired (required = false)
  public void setRememberMeServices(RememberMeServices rememberMeServices) {
    this.rememberMeServices = rememberMeServices;
  }

  public List<LogoutHandler> getLogoutHandlers() {
    return logoutHandlers;
  }

  @Autowired (required = false)
  public void setLogoutHandlers(List<LogoutHandler> logoutHandlers) {
    this.logoutHandlers = logoutHandlers;
  }
}
