package net.sf.enunciate.modules.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

/**
 * Class used for looking up the XML API.
 *
 * @author Ryan Heaton
 */
public class XMLAPILookup {

  private final Properties lookup;

  public XMLAPILookup() {
    this.lookup = new Properties();
    try {
      InputStream stream = getClass().getResourceAsStream("/xml-api.properties");
      if (stream != null) {
        this.lookup.load(stream);
      }
    }
    catch (IOException e) {
      //fall through.
    }
  }

  /**
   * Lookup the artifact by the specified namespace.
   *
   * @param namespace The namespace.
   * @return The artifact.
   */
  public Reader lookup(String namespace) {
    String artifact = (String) lookup.get(namespace);
    if (artifact != null) {
      InputStream stream = getClass().getResourceAsStream(artifact);
      if (stream != null) {
        return new InputStreamReader(stream);
      }
    }

    return null;
  }

  /**
   * Look up the WSDL for the specified service.
   *
   * @param service The name of the service.
   * @return The WSDL for the specified service.
   */
  public Reader lookupWsdl(String service) {
    //the properties are looked up by service name, too.
    return lookup(service);
  }
}
