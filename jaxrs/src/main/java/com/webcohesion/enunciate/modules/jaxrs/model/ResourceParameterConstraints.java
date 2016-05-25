package com.webcohesion.enunciate.modules.jaxrs.model;

import javax.lang.model.type.TypeKind;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface ResourceParameterConstraints {

  enum ResourceParameterContraintType {
    UNBOUND_STRING,

    PRIMITIVE,

    REGEX,

    ENUMERATION
  }

  ResourceParameterContraintType getType();

  class UnboundString implements ResourceParameterConstraints {
    @Override
    public ResourceParameterContraintType getType() {
      return ResourceParameterContraintType.UNBOUND_STRING;
    }
  }

  class Primitive implements ResourceParameterConstraints {

    private final TypeKind kind;

    public Primitive(TypeKind kind) {
      this.kind = kind;
    }

    public TypeKind getKind() {
      return kind;
    }

    @Override
    public ResourceParameterContraintType getType() {
      return ResourceParameterContraintType.PRIMITIVE;
    }
  }

  class Regex implements ResourceParameterConstraints {

    private final String regex;

    public Regex(String regex) {
      this.regex = regex;
    }

    @Override
    public ResourceParameterContraintType getType() {
      return ResourceParameterContraintType.REGEX;
    }

    public String getRegex() {
      return regex;
    }
  }

  class Enumeration implements ResourceParameterConstraints {

    private final Set<String> values;

    public Enumeration(Set<String> values) {
      this.values = values;
    }

    @Override
    public ResourceParameterContraintType getType() {
      return ResourceParameterContraintType.ENUMERATION;
    }

    public Set<String> getValues() {
      return values;
    }
  }

}
