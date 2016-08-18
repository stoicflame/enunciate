/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
