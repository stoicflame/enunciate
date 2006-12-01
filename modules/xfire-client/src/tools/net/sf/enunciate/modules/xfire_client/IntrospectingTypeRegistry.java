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
import java.util.List;

/**
 * Type registry that uses an {@link IntrospectingTypeCreator}.
 *
 * @author Ryan Heaton
 */
public class IntrospectingTypeRegistry extends DefaultTypeMappingRegistry {

  private static final Log LOG = LogFactory.getLog(IntrospectingTypeRegistry.class);

  /**
   * The id of the set of types to register with the default type mappings.
   *
   * @param typeSetId The id of the type set.
   */
  public IntrospectingTypeRegistry(String typeSetId) {
    this(loadTypeList(typeSetId));
  }

  /**
   * Create a new type registry initialized with the types (list of classes) in the specified list.
   *
   * @param typeSet The list of classes to initialize
   */
  public IntrospectingTypeRegistry(List typeSet) {
    super(true);

    TypeCreator typeCreator = createTypeCreator();
    ArrayList knownTypes = new ArrayList();
    for (int i = 0; i < typeSet.size(); i++) {
      Class typeClass = (Class) typeSet.get(i);
      Type type = typeCreator.createType(typeClass);

      if (type == null) {
        throw new IllegalStateException("Unable to find type for " + typeClass.getName() + ".");
      }

      knownTypes.add(type);
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

  /**
   * Loads the list of types given a type set id.  The types are presumed to be listed, one per line, in
   * a resource at "/[typeSetId].types".
   *
   * @param typeSetId The type set id.
   * @return The list of types.
   */
  public static List loadTypeList(String typeSetId) {
    InputStream typesList = IntrospectingTypeRegistry.class.getResourceAsStream("/" + typeSetId + ".types");
    if (typesList == null) {
      throw new IllegalArgumentException("Unknown type set: " + typeSetId);
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(typesList));
    List typeSet = new ArrayList();
    try {
      String typeValue = reader.readLine();
      while (typeValue != null) {
        try {
          Class typeClass = Class.forName(typeValue);
          typeSet.add(typeClass);
        }
        catch (Exception e) {
          throw new IllegalStateException("Problem creating type for known type " + typeValue + ".", e);
        }

        typeValue = reader.readLine();
      }
    }
    catch (IOException e) {
      LOG.error("Error reading type list.  Only the XFire defaults will be registered!", e);
    }
    return typeSet;
  }

  protected TypeCreator createTypeCreator() {
    return new IntrospectingTypeCreator(super.createTypeCreator());
  }

}
