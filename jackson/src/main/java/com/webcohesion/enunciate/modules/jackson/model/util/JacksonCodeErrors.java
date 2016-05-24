package com.webcohesion.enunciate.modules.jackson.model.util;

import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.model.Accessor;
import com.webcohesion.enunciate.modules.jackson.model.TypeDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JacksonCodeErrors {

  static final String CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY = "com.webcohesion.enunciate.modules.jackson.model.util.JacksonCodeErrors#CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY";

  private JacksonCodeErrors() {}

  public static List<String> findConflictingAccessorNamingErrors(EnunciateJacksonContext context) {
    List<String> errors = (List<String>) context.getContext().getProperty(CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY);
    if (errors == null) {
      errors = new ArrayList<String>();
      context.getContext().setProperty(CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY, errors);

      for (TypeDefinition typeDefinition : context.getTypeDefinitions()) {
        Map<String, Accessor> accessorsBySimpleName = new HashMap<String, Accessor>();
        for (Accessor accessor : typeDefinition.getAllAccessors()) {
          String name = accessor.getClientSimpleName();
          Accessor conflict = accessorsBySimpleName.get(name);
          if (conflict != null) {
            errors.add(String.format("%s: accessor \"%s\" conflicts with accessor \"%s\" of %s: both are named \"%s\".", typeDefinition.getQualifiedName(), accessor, conflict, conflict.getTypeDefinition().getQualifiedName(), name));
          }
          else {
            accessorsBySimpleName.put(name, accessor);
          }
        }
      }
    }
    return errors;
  }
}
