package net.sf.enunciate.util;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;

/**
 * Utilities for looking up the namespace of a java element.
 *
 * @author Ryan Heaton
 */
public class NamespaceUtils {

  private NamespaceUtils() {
  }

  /**
   * Gets the namespace URI for a given type declaration.
   *
   * @param declaration The declaration for which to get the namespace URI.
   * @return The namespace URI for a given type declaration.
   */
  public static String getNamespaceURI(TypeDeclaration declaration) {
    XmlRootElement schemaElementAnnotation = declaration.getAnnotation(XmlRootElement.class);
    if (schemaElementAnnotation != null) {
      String ns = schemaElementAnnotation.namespace();
      if ((ns != null) && (!"##default".equals(ns))) {
        return ns;
      }
    }

    return getNamespaceURI(declaration.getPackage());
  }

  /**
   * The namespace URI for a given package declaration.
   *
   * @param pkg The package.
   * @return The namespace URI for a given package declaration.
   */
  public static String getNamespaceURI(PackageDeclaration pkg) {
    XmlSchema annotation = pkg.getAnnotation(XmlSchema.class);
    if (annotation != null) {
      return annotation.namespace();
    }
    return "";
  }

}
