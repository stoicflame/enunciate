package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import org.springframework.aop.framework.ProxyFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Factory for JSR 311 resources that are advised.
 *
 * @author Ryan Heaton
 */
public class AdvisedResourceFactory<T> extends ProxyFactory {

  private final boolean advising;

  private AdvisedResourceFactory() {
    advising = true;
    //no-op for internal use.
  }

  /**
   * Create an advised resource factory for the specified class.
   *
   * @param resourceClass The resource class.
   */
  public AdvisedResourceFactory(Class<T> resourceClass) {
    AbstractResource resource = IntrospectionModeller.createResource(resourceClass);

    if (!resource.isRootResource()) {
      //we only advise root resources.
      advising = false;
    }
    else {
      advising = true;
      Set<Class> definingClasses = getDefiningClasses(resource);
      if (!dynamicProxySupportable(definingClasses)) {
        setProxyTargetClass(true);
      }
      else {
        setInterfaces(definingClasses.toArray(new Class[definingClasses.size()]));
      }
    }
  }

  protected boolean dynamicProxySupportable(Set<Class> definingClasses) {
    for (Class definingClass : definingClasses) {
      if (!definingClass.isInterface()) {
        return false;
      }
    }

    return true;
  }

  public Object createAdvisedResource(T bareResource) {
    if (!advising) {
      //we're not doing any advising, just return the bare resource.
      return bareResource;
    }
    else {
      AdvisedResourceFactory local = new AdvisedResourceFactory();
      local.copyConfigurationFrom(this);
      local.setTarget(bareResource);
      return local.getProxy();
    }
  }

  protected Set<Class> getDefiningClasses(AbstractResource resourceClass) {
    HashSet<Class> definingClasses = new HashSet<Class>();

    for (AbstractResourceMethod resourceMethod : resourceClass.getResourceMethods()) {
      definingClasses.add(new AnnotatedMethod(resourceMethod.getMethod()).getMethod().getDeclaringClass());
    }

    for (AbstractResourceMethod resourceMethod : resourceClass.getSubResourceMethods()) {
      definingClasses.add(new AnnotatedMethod(resourceMethod.getMethod()).getMethod().getDeclaringClass());
    }

    for (AbstractSubResourceLocator locator : resourceClass.getSubResourceLocators()) {
      definingClasses.add(new AnnotatedMethod(locator.getMethod()).getMethod().getDeclaringClass());
    }

    return definingClasses;
  }

}
