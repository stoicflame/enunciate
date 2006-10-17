package net.sf.enunciate.modules.xfire_client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.TypeCreator;
import org.codehaus.xfire.aegis.type.TypeMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Type registry that uses an {@link IntrospectingTypeCreator}.
 *
 * @author Ryan Heaton
 */
public class IntrospectingTypeRegistry extends DefaultTypeMappingRegistry {

  private static final Log LOG = LogFactory.getLog(IntrospectingTypeRegistry.class);

  private final ArrayList knownTypes = new ArrayList();

  /**
   * The id of the set of types to register with the default type mappings.
   *
   * @param typeSetId The id of the type set.
   */
  public IntrospectingTypeRegistry(String typeSetId) {
    super(true);

    InputStream typesList = getClass().getResourceAsStream("/" + typeSetId + ".types");
    if (typesList == null) {
      throw new IllegalArgumentException("Unknown type set: " + typeSetId);
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(typesList));
    TypeCreator typeCreator = createTypeCreator();
    try {
      String typeValue = reader.readLine();
      while (typeValue != null) {
        Type type;
        try {
          Class typeClass = Class.forName(typeValue);
          type = typeCreator.createType(typeClass);
        }
        catch (Exception e) {
          throw new IllegalStateException("Problem creating type for known type " + typeValue + ".", e);
        }

        if (type == null) {
          throw new IllegalStateException("Unable to find type for " + typeValue + ".");
        }

        knownTypes.add(type);
        typeValue = reader.readLine();
      }
    }
    catch (IOException e) {
      LOG.error("Error reading type list.  Only the XFire defaults will be registered!", e);
    }

    for (int i = 0; i < getRegisteredEncodingStyleURIs().length; i++) {
      String uri = getRegisteredEncodingStyleURIs()[i];
      TypeMapping typeMapping = getTypeMapping(uri);
      Iterator it = knownTypes.iterator();
      while (it.hasNext()) {
        Type type = (Type) it.next();
        typeMapping.register(type);
      }
    }
  }

  protected TypeCreator createTypeCreator() {
    return new IntrospectingTypeCreator(super.createTypeCreator());
  }

}
