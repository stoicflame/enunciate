package com.webcohesion.enunciate.rt;

import javax.ws.rs.core.Application;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * @author Ryan Heaton
 */
public class EnunciateApplication extends Application {

  private static Logger LOG = Logger.getLogger(EnunciateApplication.class.getName());
  private static final String JAXB_CONTEXT_RESOLVER_CLASSNAME = "com.webcohesion.enunciate.rt.EnunciateJaxbContextResolver";

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    try {
      Enumeration<URL> contextClassLists = classloader.getResources("/jaxrs-resource-classes.list");
      while (contextClassLists.hasMoreElements()) {
        URL contextClassList = contextClassLists.nextElement();
        BufferedReader reader = new BufferedReader(new InputStreamReader(contextClassList.openStream(), "utf-8"));
        String contextClass = reader.readLine();
        while (contextClass != null) {
          try {
            classes.add(classloader.loadClass(contextClass));
          }
          catch (Throwable e) {
            LOG.warning("Unable to load JAX-RS resource class " + contextClass + " (" + e.getMessage() + ")");
          }
          contextClass = reader.readLine();
        }
      }
    }
    catch (IOException e) {
      LOG.warning("Unable to read all JAX-RS resource classes (" + e.getMessage() + ")");
    }

    try {
      Enumeration<URL> contextClassLists = classloader.getResources("/jaxrs-provider-classes.list");
      while (contextClassLists.hasMoreElements()) {
        URL contextClassList = contextClassLists.nextElement();
        BufferedReader reader = new BufferedReader(new InputStreamReader(contextClassList.openStream(), "utf-8"));
        String contextClass = reader.readLine();
        while (contextClass != null) {
          if (!JAXB_CONTEXT_RESOLVER_CLASSNAME.equals(contextClass)) { //we'll attempt to load the jaxb context resolver as a singleton, in case we don't have jaxb-impl on the classpath.
            try {
              classes.add(classloader.loadClass(contextClass));
            }
            catch (Throwable e) {
              LOG.warning("Unable to load JAX-RS provider class " + contextClass + " (" + e.getMessage() + ")");
            }
          }
          contextClass = reader.readLine();
        }
      }
    }
    catch (IOException e) {
      LOG.warning("Unable to read all JAX-RS provider classes (" + e.getMessage() + ")");
    }

    return classes;
  }

  @Override
  public Set<Object> getSingletons() {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    HashSet<Object> singletons = new HashSet<Object>();

    try {
      singletons.add(classloader.loadClass(JAXB_CONTEXT_RESOLVER_CLASSNAME).newInstance());
    }
    catch (Throwable e) {
      LOG.info(JAXB_CONTEXT_RESOLVER_CLASSNAME + " cannot be instantiated (" + e.getMessage() + ").");
    }

    return singletons;
  }
}
