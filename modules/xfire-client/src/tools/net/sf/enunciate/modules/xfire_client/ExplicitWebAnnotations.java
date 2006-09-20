package net.sf.enunciate.modules.xfire_client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.annotations.*;
import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

/**
 * WebAnnotations implementation used to explicitly state the annotations on a class.
 *
 * @author Ryan Heaton
 */
public class ExplicitWebAnnotations implements WebAnnotations, ErrorHandler {

  private static final Log LOG = LogFactory.getLog(ExplicitWebAnnotations.class);

  private HashMap class2WebService = new HashMap();
  private HashMap class2SOAPBinding = new HashMap();
  private HashMap class2HandlerChain = new HashMap();
  private HashMap method2WebMethod = new HashMap();
  private HashMap method2WebResult = new HashMap();
  private HashMap method2WebParam = new HashMap();
  private HashSet oneWayMethods = new HashSet();

  /**
   * Associates a WebService annotation with a class.
   *
   * @param clazz      The class.
   * @param annotation The annotation.
   */
  public void put(Class clazz, WebServiceAnnotation annotation) {
    put(clazz.getName(), annotation);
  }

  /**
   * Associates a SOAPBinding annotation with a class.
   *
   * @param clazz      The class.
   * @param annotation The annotation.
   */
  public void put(Class clazz, SOAPBindingAnnotation annotation) {
    put(clazz.getName(), annotation);
  }

  /**
   * Associates a HandlerChain annotation with a class.
   *
   * @param clazz      The class.
   * @param annotation The annotation.
   */
  public void put(Class clazz, HandlerChainAnnotation annotation) {
    put(clazz.getName(), annotation);
  }

  /**
   * Associates a WebMethod annotation with a method.
   *
   * @param method     The method.
   * @param annotation The annotation.
   */
  public void put(Method method, WebMethodAnnotation annotation) {
    put(method.getDeclaringClass().getName(), method.getName(), annotation);
  }

  /**
   * Associates a WebResult annotation with a method.
   *
   * @param method     The method.
   * @param annotation The annotation.
   */
  public void put(Method method, WebResultAnnotation annotation) {
    put(method.getDeclaringClass().getName(), method.getName(), annotation);
  }

  /**
   * Associates a WebParam annotation with a method parameter.
   *
   * @param method     The method.
   * @param param      The parameter.
   * @param annotation The annotation.
   */
  public void put(Method method, int param, WebParamAnnotation annotation) {
    put(method.getDeclaringClass().getName(), method.getName(), param, annotation);
  }

  /**
   * Adds a one-way method.
   *
   * @param method The method.
   */
  public void addOneWayMethod(Method method) {
    addOneWayMethod(method.getDeclaringClass().getName(), method.getName());
  }

  /**
   * Associates a WebService annotation with a class.
   *
   * @param clazz      The name of the class.
   * @param annotation The annotation.
   */
  protected void put(String clazz, WebServiceAnnotation annotation) {
    class2WebService.put(createKey(clazz), annotation);
  }

  /**
   * Associates a SOAPBinding annotation with a class.
   *
   * @param clazz      The name of the class.
   * @param annotation The annotation.
   */
  protected void put(String clazz, SOAPBindingAnnotation annotation) {
    class2SOAPBinding.put(createKey(clazz), annotation);
  }

  /**
   * Associates a HandlerChain annotation with a class.
   *
   * @param clazz      The name of the class.
   * @param annotation The annotation.
   */
  protected void put(String clazz, HandlerChainAnnotation annotation) {
    class2HandlerChain.put(createKey(clazz), annotation);
  }

  /**
   * Associates a WebMethod annotation with a method.
   *
   * @param clazz      The name of the class.
   * @param method     The name of the method.
   * @param annotation The annotation.
   */
  protected void put(String clazz, String method, WebMethodAnnotation annotation) {
    method2WebMethod.put(createKey(clazz, method), annotation);
  }

  /**
   * Associates a WebResult annotation with a method.
   *
   * @param clazz      The name of the class.
   * @param method     The name of the method.
   * @param annotation The annotation.
   */
  protected void put(String clazz, String method, WebResultAnnotation annotation) {
    method2WebResult.put(createKey(clazz, method), annotation);
  }

  /**
   * Associates a WebParam annotation with a method parameter.
   *
   * @param clazz      The name of the class.
   * @param method     The name of the method.
   * @param param      The parameter.
   * @param annotation The annotation.
   */
  protected void put(String clazz, String method, int param, WebParamAnnotation annotation) {
    method2WebParam.put(createKey(clazz, method, param), annotation);
  }

  /**
   * Adds a one-way method.
   *
   * @param method The method.
   */
  protected void addOneWayMethod(String clazz, String method) {
    oneWayMethods.add(createKey(clazz, method));
  }

  /**
   * Writes these annotations to the specified output stream.  This method closes the stream.
   *
   * @param out The output stream.
   * @throws Exception If there was a problem writing it to the output stream.
   */
  public void writeTo(OutputStream out) throws Exception {
    //I have to write my own xml encoding because the annotations aren't serializable...
    PrintWriter writer = new PrintWriter(out);
    writer.println("<explicit-annotations>");
    writeMap(writer, "class2WebService", class2WebService);
    writeMap(writer, "class2SOAPBinding", class2SOAPBinding);
    writeMap(writer, "class2HandlerChain", class2HandlerChain);
    writeMap(writer, "method2WebMethod", method2WebMethod);
    writeMap(writer, "method2WebResult", method2WebResult);
    writeMap(writer, "method2WebParam", method2WebParam);
    writeCollection(writer, "oneWayMethods", oneWayMethods);

    writer.println("</explicit-annotations>");
    writer.flush();
  }

  /**
   * Write one of the maps to the specified writer.
   *
   * @param writer The writer.
   * @param name   The name of the map.
   * @param map    The map to write.
   */
  protected void writeMap(PrintWriter writer, String name, Map map) {
    writer.println("  <" + name + ">");
    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry) it.next();
      Object annotation = entry.getValue();
      if (annotation == null) {
        continue;
      }

      BeanInfo beanInfo;
      try {
        beanInfo = Introspector.getBeanInfo(annotation.getClass());
      }
      catch (Exception e) {
        LOG.error("Unable to get bean info for bean with key " + entry.getKey() + ".", e);
        continue;
      }

      //I can assume the key is a string and the map is a simple, flat bean.
      writer.println("    <annotation target=\"" + entry.getKey() + "\" type=\"" + annotation.getClass().getName() + "\">");
      PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
      for (int i = 0; i < pds.length; i++) {
        PropertyDescriptor pd = pds[i];
        if ((pd.getReadMethod() != null) && (pd.getWriteMethod() != null)) {
          Object value;
          try {
            value = pd.getReadMethod().invoke(annotation, new Object[0]);
          }
          catch (Exception e) {
            LOG.error("Unable to read property value for property " + pd.getName() + " on class " + annotation.getClass().getName(), e);
            continue;
          }

          if (value != null) {
            writer.println("      <property name=\"" + pd.getName() + "\" value=\"" + value + "\"/>");
          }
        }
      }
      writer.println("    </annotation>");
    }
    writer.println("  </" + name + ">");
  }

  /**
   * Writes a flat collection to the specified writer.
   *
   * @param writer     The writer.
   * @param name       The name of the collection.
   * @param collection The collection to write.
   */
  protected void writeCollection(PrintWriter writer, String name, Collection collection) {
    writer.println("  <" + name + ">");
    Iterator it = collection.iterator();
    while (it.hasNext()) {
      writer.println("    <item value=\"" + it.next() + "\"/>");
    }
    writer.println("  </" + name + ">");
  }

  /**
   * Reads the annotations from the specified input stream.
   *
   * @param in The input stream.
   * @return The annotations.
   */
  public static ExplicitWebAnnotations readFrom(InputStream in) throws Exception {
    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    SAXParserFactory.newInstance().newSAXParser().parse(in, annotations.new OutputFileReader());
    return annotations;
  }

  /**
   * How to handle a warning when parsing the config file.
   */
  public void warning(SAXParseException exception) throws SAXException {
    LOG.warn("Warning while parsing the explicit annotations file.", exception);
  }

  /**
   * How to handle an error when parsing the config file.
   */
  public void error(SAXParseException exception) throws SAXException {
    throw exception;
  }

  /**
   * How to handle a fatal error when parsing the config file.
   */
  public void fatalError(SAXParseException exception) throws SAXException {
    throw exception;
  }

  /**
   * Create a key for a class.
   *
   * @param clazz The class for which to create the key.
   * @return The key.
   */
  protected String createKey(Class clazz) {
    return createKey(clazz.getName());
  }

  /**
   * Create a key for a class.
   *
   * @param clazz The name of the class for which to create the key.
   * @return The key.
   */
  protected String createKey(String clazz) {
    return clazz;
  }

  /**
   * Create a key for a method.
   *
   * @param method The method for which to create the key.
   * @return The key.
   */
  protected String createKey(Method method) {
    return createKey(method.getDeclaringClass().getName(), method.getName());
  }

  /**
   * Create a key for a method.
   *
   * @param clazz  The name of the class of the method.
   * @param method The name of method for which to create the key.
   * @return The key.
   */
  protected String createKey(String clazz, String method) {
    return clazz + "." + method;
  }

  /**
   * Create a key for a parameter.
   *
   * @param method The method.
   * @param param  The parameter.
   * @return The key.
   */
  protected String createKey(Method method, int param) {
    return createKey(method.getDeclaringClass().getName(), method.getName(), param);
  }

  /**
   * Create a key for a parameter.
   *
   * @param clazz  The name of the class.
   * @param method The name of the method.
   * @param param  The parameter.
   * @return The key.
   */
  protected String createKey(String clazz, String method, int param) {
    return clazz + "." + method + "." + param;
  }

  // Inherited.
  public boolean hasWebServiceAnnotation(Class clazz) {
    return class2WebService.containsKey(createKey(clazz));
  }

  // Inherited.
  public WebServiceAnnotation getWebServiceAnnotation(Class clazz) {
    return (WebServiceAnnotation) class2WebService.get(createKey(clazz));
  }

  // Inherited.
  public boolean hasWebMethodAnnotation(Method method) {
    return method2WebMethod.containsKey(createKey(method));
  }

  // Inherited.
  public WebMethodAnnotation getWebMethodAnnotation(Method method) {
    return (WebMethodAnnotation) method2WebMethod.get(createKey(method));
  }

  // Inherited.
  public boolean hasWebResultAnnotation(Method method) {
    return method2WebResult.containsKey(createKey(method));
  }

  // Inherited.
  public WebResultAnnotation getWebResultAnnotation(Method method) {
    return (WebResultAnnotation) method2WebResult.get(createKey(method));
  }

  // Inherited.
  public boolean hasWebParamAnnotation(Method method, int parameter) {
    return method2WebParam.containsKey(createKey(method, parameter));
  }

  // Inherited.
  public WebParamAnnotation getWebParamAnnotation(Method method, int parameter) {
    return (WebParamAnnotation) method2WebParam.get(createKey(method, parameter));
  }

  // Inherited.
  public boolean hasOnewayAnnotation(Method method) {
    return oneWayMethods.contains(createKey(method));
  }

  // Inherited.
  public boolean hasSOAPBindingAnnotation(Class clazz) {
    return class2SOAPBinding.containsKey(createKey(clazz));
  }

  // Inherited.
  public SOAPBindingAnnotation getSOAPBindingAnnotation(Class clazz) {
    return (SOAPBindingAnnotation) class2SOAPBinding.get(createKey(clazz));
  }

  // Inherited.
  public boolean hasHandlerChainAnnotation(Class clazz) {
    return class2HandlerChain.containsKey(createKey(clazz));
  }

  // Inherited.
  public HandlerChainAnnotation getHandlerChainAnnotation(Class clazz) {
    return (HandlerChainAnnotation) class2HandlerChain.get(createKey(clazz));
  }

  public Map getServiceProperties(Class clazz) {
    return null;
  }

  /**
   * Pushes an object onto the digester stack.
   */
  private class OutputFileReader extends DefaultHandler {

    private Map currentMap;
    private Set currentSet;
    private Object currentAnnotation;
    private PropertyDescriptor[] currentPds;
    private String currentKey;

    public void startElement(String element, String localName, String qName, Attributes attributes) throws SAXException {
      if ("class2WebService".equals(element)) {
        currentMap = class2WebService;
      }
      else if ("class2SOAPBinding".equals(element)) {
        currentMap = class2SOAPBinding;
      }
      else if ("class2HandlerChain".equals(element)) {
        currentMap = class2HandlerChain;
      }
      else if ("method2WebMethod".equals(element)) {
        currentMap = method2WebMethod;
      }
      else if ("method2WebResult".equals(element)) {
        currentMap = method2WebResult;
      }
      else if ("method2WebParam".equals(element)) {
        currentMap = method2WebParam;
      }
      else if ("oneWayMethods".equals(element)) {
        currentSet = oneWayMethods;
      }
      else if ("annotation".equals(element)) {
        currentKey = attributes.getValue("target");
        String type = attributes.getValue("type");
        try {
          Class clazz = Class.forName(type);
          currentAnnotation = clazz.newInstance();
          currentPds = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        }
        catch (Exception e) {
          throw new SAXException(e);
        }
      }
      else if ("property".equals(element)) {
        String name = attributes.getValue("name");
        String value = attributes.getValue("value");
        for (int i = 0; i < currentPds.length; i++) {
          PropertyDescriptor pd = currentPds[i];
          if (pd.getName().equals(name)) {
            try {
              pd.getWriteMethod().invoke(currentAnnotation, new Object[]{pd.getPropertyType().getConstructor(new Class[]{String.class}).newInstance(new Object[]{value})});
            }
            catch (Exception e) {
              throw new SAXException(e);
            }
          }
        }
      }
      else if ("item".equals(element)) {
        currentSet.add(attributes.getValue("value"));
      }
    }

    public void endElement(String element, String localName, String qName) throws SAXException {
      if ("class2WebService".equals(element)) {
        currentMap = null;
      }
      else if ("class2SOAPBinding".equals(element)) {
        currentMap = null;
      }
      else if ("class2HandlerChain".equals(element)) {
        currentMap = null;
      }
      else if ("method2WebMethod".equals(element)) {
        currentMap = null;
      }
      else if ("method2WebResult".equals(element)) {
        currentMap = null;
      }
      else if ("method2WebParam".equals(element)) {
        currentMap = null;
      }
      else if ("oneWayMethods".equals(element)) {
        currentSet = null;
      }
      else if ("annotation".equals(element)) {
        currentMap.put(currentKey, currentAnnotation);
        currentAnnotation = null;
        currentPds = null;
      }
    }

    public void warning(SAXParseException e) throws SAXException {
      LOG.warn(e);
    }

    public void error(SAXParseException e) throws SAXException {
      throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
      throw e;
    }

  }

}
