package org.codehaus.enunciate.modules.amf;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.ServiceException;

/**
 * Spring factory.
 *
 * @author Jeff Vroom
 */
public class SpringFactory implements FlexFactory {
  
  private static final String SOURCE = "source";

  /**
   * This method can be used to initialize the factory itself.  It is called with configuration
   * parameters from the factory tag which defines the id of the factory.
   */
  public void initialize(String id, ConfigMap configMap) {
  }

  /**
   * This method is called when we initialize the definition of an instance
   * which will be looked up by this factory.  It should validate that
   * the properties supplied are valid to define an instance.
   * Any valid properties used for this configuration must be accessed to
   * avoid warnings about unused configuration elements.  If your factory
   * is only used for application scoped components, this method can simply
   * return a factory instance which delegates the creation of the component
   * to the FactoryInstance's lookup method.
   */
  public FactoryInstance createFactoryInstance(String id, ConfigMap properties) {
    SpringFactoryInstance instance = new SpringFactoryInstance(this, id, properties);
    instance.setSource(properties.getPropertyAsString(SOURCE, instance.getId()));
    return instance;
  } // end method createFactoryInstance()

  /**
   * Returns the instance specified by the source
   * and properties arguments.  For the factory, this may mean
   * constructing a new instance, optionally registering it in some other
   * name space such as the session or JNDI, and then returning it
   * or it may mean creating a new instance and returning it.
   * This method is called for each request to operate on the
   * given item by the system so it should be relatively efficient.
   * <p>
   * If your factory does not support the scope property, it
   * report an error if scope is supplied in the properties
   * for this instance.
   */
  public Object lookup(FactoryInstance instance) {
    return instance.lookup();
  }

  static class SpringFactoryInstance extends FactoryInstance {
    
    SpringFactoryInstance(SpringFactory factory, String id, ConfigMap properties) {
      super(factory, id, properties);
    }


    public String toString() {
      return "SpringFactory instance for id=" + getId() + " source=" + getSource() + " scope=" + getScope();
    }

    public Object lookup() {
      ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(flex.messaging.FlexContext.getServletConfig().getServletContext());
      String beanName = getSource();

      try {
        return appContext.getBean(beanName);
      }
      catch (NoSuchBeanDefinitionException nexc) {
        ServiceException e = new ServiceException();
        String msg = "Spring service named '" + beanName + "' does not exist.";
        e.setMessage(msg);
        e.setRootCause(nexc);
        e.setDetails(msg);
        e.setCode("Server.Processing");
        throw e;
      }
      catch (BeansException bexc) {
        ServiceException e = new ServiceException();
        String msg = "Unable to create Spring service named '" + beanName + "' ";
        e.setMessage(msg);
        e.setRootCause(bexc);
        e.setDetails(msg);
        e.setCode("Server.Processing");
        throw e;
      }
    }

  }

}