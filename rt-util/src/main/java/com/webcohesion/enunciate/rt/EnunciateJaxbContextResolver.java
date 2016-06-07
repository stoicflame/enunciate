package com.webcohesion.enunciate.rt;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A JAX-RS context resolver for an instance of JAXBContext that attempts to use metadata exported at build-time
 * by Enunciate to provide pretty namespace prefixes and a JAXB context that is aware of all the classes annotated
 * with @XmlRootElement in the project.
 *
 * @author Ryan Heaton
 */
@Provider
public class EnunciateJaxbContextResolver implements ContextResolver<JAXBContext> {

  private static Logger LOG = Logger.getLogger(EnunciateJaxbContextResolver.class.getName());

  private final JAXBContext context;

  public EnunciateJaxbContextResolver() {
    this.context = buildJaxbContext();
  }

  @Override
  public JAXBContext getContext(Class<?> type) {
    return this.context;
  }

  protected JAXBContext buildJaxbContext() {
    List<Class<?>> contextClasses = new ArrayList<Class<?>>();
    ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    boolean hasContextClasses = false;
    boolean hasNamespacePrefixes = false;

    try {
      Enumeration<URL> contextClassLists = classloader.getResources("/jaxb-context-classes.list");
      while (contextClassLists.hasMoreElements()) {
        hasContextClasses = true;
        URL contextClassList = contextClassLists.nextElement();
        BufferedReader reader = new BufferedReader(new InputStreamReader(contextClassList.openStream(), "utf-8"));
        String contextClass = reader.readLine();
        while (contextClass != null) {
          try {
            contextClasses.add(classloader.loadClass(contextClass));
          }
          catch (Throwable e) {
            LOG.warning("Unable to load JAXB context class " + contextClass + " (" + e.getMessage() + ")");
          }
          contextClass = reader.readLine();
        }
      }
    }
    catch (IOException e) {
      LOG.warning("Unable to read all JAXB context classes (" + e.getMessage() + ")");
    }

    Properties namespacePrefixes = new Properties();
    String defaultNs = null;
    try {
      Enumeration<URL> namespacePropertiesList = classloader.getResources("/namespaces.properties");
      while (namespacePropertiesList.hasMoreElements()) {
        hasNamespacePrefixes = true;
        URL namespaceProperties = namespacePropertiesList.nextElement();
        Properties props = new Properties();
        props.load(namespaceProperties.openStream());
        Object defaultNamespace = props.remove("{default}");
        if (defaultNamespace != null && defaultNs == null) {
          defaultNs = defaultNamespace.toString();
        }
        namespacePrefixes.putAll(props);
      }
    }
    catch (IOException e) {
      LOG.warning("Unable to read all namespace properties (" + e.getMessage() + ")");
    }

    if (hasContextClasses || hasNamespacePrefixes) {
      try {
        EnunciateJaxbNamespacePrefixMapper prefixMapper = new EnunciateJaxbNamespacePrefixMapper(defaultNs, namespacePrefixes);
        JAXBContext jaxbContext = JAXBContext.newInstance(contextClasses.toArray(new Class[contextClasses.size()]));
        return new EnunciateJaxbContext(jaxbContext, prefixMapper);
      }
      catch (JAXBException e) {
        LOG.warning("Unable to construct JAXB classes (" + e.getMessage() + ")");
      }
    }

    return null;
  }
}
