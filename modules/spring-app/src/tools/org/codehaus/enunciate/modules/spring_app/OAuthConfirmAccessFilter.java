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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.event.authorization.AuthorizedEvent;
import org.springframework.security.intercept.web.DefaultFilterInvocationDefinitionSource;
import org.springframework.security.intercept.web.FilterInvocation;
import org.springframework.security.intercept.web.FilterSecurityInterceptor;
import org.springframework.security.intercept.web.RequestKey;
import org.springframework.security.oauth.provider.OAuthProviderProcessingFilter;
import org.springframework.security.util.AntUrlPathMatcher;
import org.springframework.security.vote.AffirmativeBased;
import org.springframework.security.vote.AuthenticatedVoter;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Filter that ensures that the user is authorized before attempting to access the confirm access pages.
 *
 * @author Ryan Heaton
 */
public class OAuthConfirmAccessFilter extends FilterSecurityInterceptor implements ApplicationEventPublisher {

  private String confirmAccessUrl = "/oauth/confirm_access";
  private String accessConfirmedUrl = "/oauth/access_confirmed";

  public OAuthConfirmAccessFilter() {
    AffirmativeBased accessDecision = new AffirmativeBased();
    accessDecision.setAllowIfAllAbstainDecisions(false);
    accessDecision.setDecisionVoters(Arrays.asList(new AuthenticatedVoter()));
    setAccessDecisionManager(accessDecision);
    setApplicationEventPublisher(this);
    setObserveOncePerRequest(false);
  }

  /**
   * As an event publisher, listens for any {@link org.springframework.security.event.authorization.AuthorizedEvent}s and ensures that any OAuth processing
   * filters don't get called after the request is authorized.
   *
   * @param event The event.
   */
  public void publishEvent(ApplicationEvent event) {
    if ((event instanceof AuthorizedEvent) && (event.getSource() instanceof FilterInvocation)) {
      ((FilterInvocation) event.getSource()).getHttpRequest().setAttribute(OAuthProviderProcessingFilter.OAUTH_PROCESSING_HANDLED, Boolean.TRUE);
    }
  }

  @Autowired
  @Override
  public void setAuthenticationManager(AuthenticationManager newManager) {
    super.setAuthenticationManager(newManager);
  }

  public void setConfirmAccessUrl(String confirmAccessUrl) {
    this.confirmAccessUrl = confirmAccessUrl;
  }

  public void setAccessConfirmedUrl(String accessConfirmedUrl) {
    this.accessConfirmedUrl = accessConfirmedUrl;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    LinkedHashMap map = new LinkedHashMap();
    map.put(new RequestKey(this.confirmAccessUrl), new ConfigAttributeDefinition(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED));
    map.put(new RequestKey(this.accessConfirmedUrl), new ConfigAttributeDefinition(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED));
    setObjectDefinitionSource(new ConfirmAccessFilterInvocationDefinitionSource(map));
    super.afterPropertiesSet();
  }

  @Override
  public int getOrder() {
    return super.getOrder() + 10;
  }

  public class ConfirmAccessFilterInvocationDefinitionSource extends DefaultFilterInvocationDefinitionSource {

    private ConfirmAccessFilterInvocationDefinitionSource(LinkedHashMap map) {
      super(new AntUrlPathMatcher(true), map);
      setStripQueryStringFromUrls(true);
    }
  }
}
