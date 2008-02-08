package org.codehaus.enunciate.modules.spring_app.config.security;

/**
 * Reference class used to specify a reference to another bean in the config file.
 *
 * @author Ryan Heaton
 */
public class BeanReference {

  private String beanName;
  private String className;

  public String getBeanName() {
    return beanName;
  }

  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
