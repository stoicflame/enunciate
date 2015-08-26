package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jackson.model.Member;
import com.webcohesion.enunciate.modules.jackson.model.ObjectTypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonClassType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson.model.types.KnownJsonType;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ObjectDataTypeImpl extends DataTypeImpl {

  private final ObjectTypeDefinition typeDefinition;

  public ObjectDataTypeImpl(ObjectTypeDefinition typeDefinition) {
    super(typeDefinition);
    this.typeDefinition = typeDefinition;
  }

  @Override
  public BaseType getBaseType() {
    return BaseType.object;
  }

  @Override
  public List<? extends Value> getValues() {
    return null;
  }

  @Override
  public List<? extends Property> getProperties() {
    SortedSet<Member> members = this.typeDefinition.getMembers();
    ArrayList<Property> properties = new ArrayList<Property>(members.size());
    FacetFilter facetFilter = this.typeDefinition.getContext().getContext().getConfiguration().getFacetFilter();
    for (Member member : members) {
      if (!facetFilter.accept(member)) {
        continue;
      }

      if (member.getChoices().size() > 1) {
        JsonTypeInfo.As inclusion = member.getSubtypeIdInclusion();
        if (inclusion == JsonTypeInfo.As.WRAPPER_ARRAY || inclusion == JsonTypeInfo.As.WRAPPER_OBJECT) {
          for (Member choice : member.getChoices()) {
            properties.add(new PropertyImpl(choice));
          }
        }
        else {
          properties.add(new PropertyImpl(member));
        }
      }
      else {
        properties.add(new PropertyImpl(member));
      }
    }

    //sort the properties by name.
    Collections.sort(properties, new Comparator<Property>() {
      @Override
      public int compare(Property o1, Property o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    return properties;
  }

  @Override
  public List<DataTypeReference> getSupertypes() {
    ArrayList<DataTypeReference> supertypes = null;

    JsonType supertype = this.typeDefinition.getSupertype();
    while (supertype != null) {
      if (supertypes == null) {
        supertypes = new ArrayList<DataTypeReference>();
      }

      supertypes.add(new DataTypeReferenceImpl(supertype));
      supertype = supertype instanceof JsonClassType ?
        ((JsonClassType)supertype).getTypeDefinition() instanceof ObjectTypeDefinition ?
          ((ObjectTypeDefinition)((JsonClassType)supertype).getTypeDefinition()).getSupertype()
          : null
        : null;
    }

    return supertypes;
  }

  @Override
  public Example getExample() {
    return new ExampleImpl(this.typeDefinition);
  }
}
