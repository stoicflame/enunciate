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

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;

import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnunciateSpringSecuritySupport extends ApplicationObjectSupport {

  private LoginLogoutHelper loginLogoutHelper = new LoginLogoutHelperImpl();

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    ApplicationContext ctx = getApplicationContext();
    Map<String, LoginLogoutProvider> loginLogoutProviders = BeanFactoryUtils.beansOfTypeIncludingAncestors(ctx, LoginLogoutProvider.class);
    for (LoginLogoutProvider loginLogoutProvider : loginLogoutProviders.values()) {
      loginLogoutProvider.initLoginLogoutHelper(getLoginLogoutHelper());
    }
  }

  public LoginLogoutHelper getLoginLogoutHelper() {
    return loginLogoutHelper;
  }

  public void setLoginLogoutHelper(LoginLogoutHelper loginLogoutHelper) {
    this.loginLogoutHelper = loginLogoutHelper;
  }
}
