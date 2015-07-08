package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.modules.jackson.model.Member;
import com.webcohesion.enunciate.modules.jackson.model.ObjectTypeDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

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
  public List<? extends Value> getValues() {
    return null;
  }

  @Override
  public List<? extends Property> getProperties() {
    SortedSet<Member> members = this.typeDefinition.getMembers();
    ArrayList<Property> properties = new ArrayList<Property>(members.size());
    for (Member member : members) {
      for (Member choice : member.getChoices()) {
        properties.add(new PropertyImpl(choice));
      }
    }
    return properties;
  }

}
