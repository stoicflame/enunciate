package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCProxiedComponentProvider;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An enunciate-aware spring component provider factory. This factory is intended to extend the {@link SpringComponentProviderFactory} in such a way so
 * as to apply the enunciate global interceptors to any JAX-RS root resource.
 *
 * @author Ryan Heaton
 */
public class EnunciateSpringComponentProviderFactory extends SpringComponentProviderFactory {

  private final Map<Class, AdvisedResourceFactory> resourceFactories = new HashMap<Class, AdvisedResourceFactory>();
  private List<Object> interceptors;

  public EnunciateSpringComponentProviderFactory(ResourceConfig rc, WebApplicationContext applicationContext) {
    super(rc, (ConfigurableApplicationContext) applicationContext);
    applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Override
  public IoCComponentProvider getComponentProvider(ComponentContext cc, final Class c) {
    final IoCComponentProvider componentProvider = super.getComponentProvider(cc, c);
    if (componentProvider instanceof IoCManagedComponentProvider) {
      //return a managed provider...
      return new IoCManagedComponentProvider() {
        public ComponentScope getScope() {
          return ((IoCManagedComponentProvider) componentProvider).getScope();
        }

        public Object getInjectableInstance(Object o) {
          return EnunciateSpringComponentProviderFactory.this.getInjectableInstance(o);
        }

        public Object getInstance() {
          return getResourceFactory(c).createAdvisedResource(componentProvider.getInstance());
        }
      };
    }
    else if (componentProvider instanceof IoCInstantiatedComponentProvider) {
      //return an instantiated provider...
      return new IoCInstantiatedComponentProvider() {
        public Object getInjectableInstance(Object o) {
          return EnunciateSpringComponentProviderFactory.this.getInjectableInstance(o);
        }

        public Object getInstance() {
          return getResourceFactory(c).createAdvisedResource(componentProvider.getInstance());
        }
      };
    }
    else {
      //just a proxied provider.
      return new IoCProxiedComponentProvider() {
        public Object proxy(Object o) {
          return getResourceFactory(c).createAdvisedResource(o);
        }

        public Object getInstance() {
          //jersey-managed instantiation.
          return null;
        }
      };
    }
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
  @Resource ( name = "service-bean-interceptors" )
  public void setEnunciateInterceptors(List<Object> interceptors) {
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
