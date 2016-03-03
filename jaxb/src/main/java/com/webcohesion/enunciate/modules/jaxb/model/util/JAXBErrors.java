package com.webcohesion.enunciate.modules.jaxb.model.util;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JAXBErrors {

  static final String CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY = "com.webcohesion.enunciate.modules.jaxb.model.util.JAXBErrors#CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY";

  private JAXBErrors() {}

  public static List<String> findConflictingAccessorNamingErrors(EnunciateJaxbContext context) {
    List<String> errors = (List<String>) context.getContext().getProperty(CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY);
    if (errors == null) {
      errors = new ArrayList<String>();
      context.getContext().setProperty(CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY, errors);

      for (SchemaInfo schemaInfo : context.getSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          Map<String, Accessor> accessorsBySimpleName = new HashMap<String, Accessor>();
          for (Accessor accessor : typeDefinition.getAllAccessors()) {
            String name = accessor.getClientSimpleName();
            Accessor conflict = accessorsBySimpleName.get(name);
            if (conflict != null) {
              errors.add(String.format("%s: accessor \"%s\" conflicts with accessor \"%s\": both are named \"%s\".", typeDefinition.getQualifiedName(), accessor, conflict, name));
            }
            else {
              accessorsBySimpleName.put(name, accessor);
            }
          }
        }
      }
    }
    return errors;
  }
}
