package com.webcohesion.enunciate.modules.jaxrs.model;

import javax.lang.model.type.TypeKind;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface ResourceParameterConstraints {

  public enum ResourceParameterContraintType {
    UNBOUND_STRING,

    PRIMITIVE,

    REGEX,

    ENUMERATION
  }

  ResourceParameterContraintType getType();

  public static class UnboundString implements ResourceParameterConstraints {
    @Override
    public ResourceParameterContraintType getType() {
      return ResourceParameterContraintType.UNBOUND_STRING;
    }
  }

  public static class Primitive implements ResourceParameterConstraints {

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

  public static class Regex implements ResourceParameterConstraints {

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

  public static class Enumeration implements ResourceParameterConstraints {

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
