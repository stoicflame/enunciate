package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;

import javax.lang.model.element.VariableElement;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class EnumValue extends DecoratedVariableElement implements HasFacets {

  private final EnumTypeDefinition typeDefinition;
  private final String name;
  private final Object value;
  private final Set<Facet> facets = new TreeSet<Facet>();

  public EnumValue(EnumTypeDefinition typeDefinition, VariableElement delegate, String name, Object value) {
    super(delegate, typeDefinition.getContext().getContext().getProcessingEnvironment());
    this.typeDefinition = typeDefinition;
    this.name = name;
    this.value = value;
    this.facets.addAll(Facet.gatherFacets(delegate));
    this.facets.addAll(typeDefinition.getFacets());
    setDocComment(((DecoratedVariableElement)delegate).getDocComment());
  }

  public EnumTypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public Set<Facet> getFacets() {
    return this.facets;
  }
}
