package com.webcohesion.enunciate.modules.jackson1.model.util;

import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.modules.jackson1.model.Accessor;
import com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class Jackson1CodeErrors {

  static final String CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY = "com.webcohesion.enunciate.modules.jackson.model.util.JacksonCodeErrors#CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY";

  private Jackson1CodeErrors() {}

  public static List<String> findConflictingAccessorNamingErrors(EnunciateJackson1Context context) {
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
