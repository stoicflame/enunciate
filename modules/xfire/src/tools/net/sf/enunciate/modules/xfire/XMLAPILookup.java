package net.sf.enunciate.modules.xfire;

import java.io.*;
import java.util.HashMap;

/**
 * Class used for looking up the XML API.
 *
 * @author Ryan Heaton
 */
public class XMLAPILookup implements Serializable {

  private final HashMap<String, String> ns2wsdl;
  private final HashMap<String, String> ns2schema;
  private final HashMap<String, String> service2wsdl;

  XMLAPILookup(HashMap<String, String> ns2wsdl, HashMap<String, String> ns2schema, HashMap<String, String> service2wsdl) {
    this.ns2wsdl = ns2wsdl;
    this.ns2schema = ns2schema;
    this.service2wsdl = service2wsdl;
  }

  /**
   * Load the lookup from the specified stream.
   *
   * @param in The stream from which to load the lookup.
   * @return the lookup, if any was found.
   */
  public static XMLAPILookup load(InputStream in) {
    try {
      ObjectInputStream oin = new ObjectInputStream(in);
      XMLAPILookup lookup = (XMLAPILookup) oin.readObject();
      oin.close();
      return lookup;
    }
    catch (Exception e) {
      return new XMLAPILookup(new HashMap<String, String>(), new HashMap<String, String>(), new HashMap<String, String>());
    }
  }

  /**
   * Store the lookup to the specified stream.
   *
   * @param out The stream to store to.
   */
  public void store(OutputStream out) throws IOException {
    ObjectOutputStream oout = new ObjectOutputStream(out);
    oout.writeObject(this);
    oout.close();
  }

  /**
   * Get the artifact for the specified service.
   *
   * @param service The service.
   * @return The artifact for the service.
   */
  public String getWsdlResourceForService(String service) {
    return service2wsdl.get(service);
  }

  /**
   * Get the schema resource for the specified namespace.
   *
   * @param namespace The namespace.
   * @return The schema resource for the namespace.
   */
  public String getSchemaResourceForNamespace(String namespace) {
    return ns2schema.get(namespace);
  }

  /**
   * Get the wsdl resource for the specified namespace.
   *
   * @param namespace The namespace.
   * @return The wsdl resource for the namespace.
   */
  public String getWsdlResourceForNamespace(String namespace) {
    return ns2wsdl.get(namespace);
  }

}
