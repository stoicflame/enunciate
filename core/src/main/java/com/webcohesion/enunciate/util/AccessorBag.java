package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessorBag {
  public static class ElementList<E extends Element> extends ArrayList<E> {
    /**
     * Add the specified member declaration, or if it is already in the list (by name), replace it.
     *
     * @param memberDeclaration The member to add/replace.
     */
    public void addOrReplace(E memberDeclaration) {
      removeByName(memberDeclaration);
      add(memberDeclaration);
    }

    /**
     * Remove member declarations with the specified name, if it exists..
     *
     * @param name The member to remove.
     */
    public void removeByName(Name name) {
      removeIf(e -> e.getSimpleName().equals(name));
    }

    /**
     * Remove specified member declaration (by name), if it exists..
     *
     * @param memberDeclaration  The member to remove.
     */
    public void removeByName(E memberDeclaration) {
      removeByName(memberDeclaration.getSimpleName());
    }
  }

  public final ElementList<VariableElement> fields = new ElementList<>();
  public final ElementList<PropertyElement> properties = new ElementList<>();
  public String typeIdProperty;

  public List<Element> getAccessors() {
    if (fields.isEmpty() && properties.isEmpty()) {
      return Collections.emptyList();
    }
    List<Element> accessors = new ArrayList<>(fields.size() + properties.size());
    accessors.addAll(fields);
    accessors.addAll(properties);
    return accessors;
  }
}
