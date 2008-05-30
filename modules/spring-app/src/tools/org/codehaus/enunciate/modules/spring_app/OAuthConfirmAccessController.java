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
import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.security.oauth.provider.ConsumerDetailsService;
import org.springframework.security.oauth.provider.token.OAuthProviderToken;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenServices;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;

/**
 * Controller for displaying the OAuth "confirm access" page. 
 *
 * @author Ryan Heaton
 */
public class OAuthConfirmAccessController extends StaticModelViewController {

  private OAuthProviderTokenServices tokenServices;
  private ConsumerDetailsService consumerDetailsService;

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String token = request.getParameter("requestToken");
    if (token == null) {
      throw new IllegalArgumentException("An request token to authorize must be provided.");
    }

    OAuthProviderToken providerToken = getTokenServices().getToken(token);
    ConsumerDetails consumer = getConsumerDetailsService().loadConsumerByConsumerKey(providerToken.getConsumerKey());

    String callback = request.getParameter("callbackURL");
    TreeMap<String, Object> model = getModel() == null ? new TreeMap<String, Object>() : new TreeMap<String, Object>(getModel());
    model.put("requestToken", token);
    if (callback != null) {
      model.put("callbackURL", callback);
    }

    model.put("consumer", consumer);
    return new ModelAndView(getView(), model);
  }

  public OAuthProviderTokenServices getTokenServices() {
    return tokenServices;
  }

  @Autowired
  public void setTokenServices(OAuthProviderTokenServices tokenServices) {
    this.tokenServices = tokenServices;
  }

  public ConsumerDetailsService getConsumerDetailsService() {
    return consumerDetailsService;
  }

  @Autowired
  public void setConsumerDetailsService(ConsumerDetailsService consumerDetailsService) {
    this.consumerDetailsService = consumerDetailsService;
  }
}
