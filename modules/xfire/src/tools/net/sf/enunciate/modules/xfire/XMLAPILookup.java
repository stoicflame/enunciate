package net.sf.enunciate.modules.xfire;

import java.io.*;
import java.util.HashMap;

/**
 * Class used for looking up the XML API.
 *
 * @author Ryan Heaton
 */
public class XMLAPILookup implements Serializable {

  private final HashMap<String, String> ns2artifact;
  private final HashMap<String, String> service2artifact;

  XMLAPILookup(HashMap<String, String> ns2artifact, HashMap<String, String> service2artifact) {
    this.ns2artifact = ns2artifact;
    this.service2artifact = service2artifact;
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
      return new XMLAPILookup(new HashMap<String, String>(), new HashMap<String, String>());
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
  public String getArtifactForService(String service) {
    return service2artifact.get(service);
  }

  /**
   * Get the artifact for the specified namespace.
   *
   * @param namespace The namespace.
   * @return The artifact for the namespace.
   */
  public String getArtifactForNamespace(String namespace) {
    return ns2artifact.get(namespace);
  }


}
