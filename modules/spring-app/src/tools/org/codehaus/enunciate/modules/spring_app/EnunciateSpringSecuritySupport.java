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
