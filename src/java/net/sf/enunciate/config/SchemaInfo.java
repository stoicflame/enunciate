package net.sf.enunciate.config;

import net.sf.enunciate.contract.jaxb.RootElementDeclaration;
import net.sf.enunciate.contract.jaxb.TypeDefinition;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Configuration information about a schema.
 *
 * @author Ryan Heaton
 */
public class SchemaInfo {

  private String namespace;
  private boolean generate;
  private String file;
  private String location;
  private final Collection<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();
  private final Collection<RootElementDeclaration> globalElements = new ArrayList<RootElementDeclaration>();

  /**
   * The target namespace.
   *
   * @return The target namespace.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * The target namespace.
   *
   * @param namespace The target namespace.
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * Whether or not to generate this schema.
   *
   * @return Whether or not to generate this schema.
   */
  public boolean isGenerate() {
    return generate;
  }

  /**
   * Whether or not to generate this schema.
   *
   * @param generate Whether or not to generate this schema.
   */
  public void setGenerate(boolean generate) {
    this.generate = generate;
  }

  /**
   * The file to which to write this schema.
   *
   * @return The file to which to write this schema.
   */
  public String getFile() {
    return file;
  }

  /**
   * The file to which to write this schema.
   *
   * @param file The file to which to write this schema.
   */
  public void setFile(String file) {
    this.file = file;
  }

  /**
   * The schema location.
   *
   * @return The schema location.
   */
  public String getLocation() {
    return location;
  }

  /**
   * The schema location.
   *
   * @param location The schema location.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * The collection of types defined in this schema.
   *
   * @return The collection of types defined in this schema.
   */
  public Collection<TypeDefinition> getTypeDefinitions() {
    return typeDefinitions;
  }

  /**
   * The collection of global elements defined in this schema.
   *
   * @return The collection of global elements defined in this schema.
   */
  public Collection<RootElementDeclaration> getGlobalElements() {
    return globalElements;
  }

}
