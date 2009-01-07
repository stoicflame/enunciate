package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.spi.service.ComponentContext;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnunciateSpringComponentProvider extends com.sun.jersey.spi.spring.container.servlet.SpringComponentProvider {

  private final Map<Class, AdvisedResourceFactory> resourceFactories = new HashMap<Class, AdvisedResourceFactory>();
  private List<Object> interceptors;
  private final WebApplicationContext springApplicationContext;

  public EnunciateSpringComponentProvider(WebApplicationContext applicationContext) {
    super((ConfigurableApplicationContext) applicationContext);
    applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    this.springApplicationContext = applicationContext;
  }

  // Inherited.
  public <T> T getInstance(Scope scope, Constructor<T> constructor, Object[] parameters) throws InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    T instance;

    instance = super.getInstance(null, Scope.Undefined, constructor.getDeclaringClass());

    if (instance == null) {
      instance = constructor.newInstance(parameters);
      this.springApplicationContext.getAutowireCapableBeanFactory().autowireBean(instance);
    }

    return (T) getResourceFactory(constructor.getDeclaringClass()).createAdvisedResource(instance);
  }

  // Inherited.
  public <T> T getInstance(ComponentContext cc, Scope scope, Class<T> clazz) throws InstantiationException, IllegalAccessException {
    T instance = super.getInstance(cc, Scope.Undefined, clazz);

    if (instance == null) {
      instance = clazz.newInstance();
      this.springApplicationContext.getAutowireCapableBeanFactory().autowireBean(instance);
    }

    return (T) getResourceFactory(clazz).createAdvisedResource(instance);
  }

  // Inherited.
  public <T> T getInjectableInstance(T instance) {
    while (AopUtils.isAopProxy(instance)) {
      final Advised aopResource = (Advised) instance;
      try {
        instance = (T) aopResource.getTargetSource().getTarget();
      }
      catch (Exception e) {
        throw new RuntimeException("Could not get target object from proxy.", e);
      }
    }

    return instance;
  }

  /**
   * Set the interceptors for this provider.
   *
   * @param interceptors The interceptors.
   */
  @Autowired ( required = false )
  public void setInterceptors(@Qualifier ("enunciate-service-bean-interceptors") List<Object> interceptors) {
    this.interceptors = interceptors;
  }

  protected <T> AdvisedResourceFactory<T> getResourceFactory(Class<T> resourceClass) {
    AdvisedResourceFactory<T> factory;

    synchronized (resourceFactories) {
      factory = resourceFactories.get(resourceClass);
      if (factory == null) {
        factory = new AdvisedResourceFactory<T>(resourceClass);
        if (interceptors != null) {
          for (Object interceptor : interceptors) {
            if (interceptor instanceof Advice) {
              factory.addAdvice((Advice) interceptor);
            }
            else if (interceptor instanceof Advisor) {
              factory.addAdvisor((Advisor) interceptor);
            }
          }
        }
        resourceFactories.put(resourceClass, factory);
      }
    }

    return factory;
  }
}
