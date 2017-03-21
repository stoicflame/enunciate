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
package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.PropertyMetadata;
import com.webcohesion.enunciate.modules.jaxb.model.Element;

/**
 * @author Ryan Heaton
 */
public class WrappedPropertyImpl extends PropertyImpl {

  private final String wrapperName;
  private final String wrapperNamespace;

  public WrappedPropertyImpl(Element accessor, String wrapperName, String wrapperNamespace, ApiRegistrationContext registrationContext) {
    super(accessor, registrationContext);
    this.wrapperName = wrapperName;
    this.wrapperNamespace = wrapperNamespace;
  }

  public PropertyMetadata getWrapper() {
    if (this.wrapperNamespace != null && !this.wrapperNamespace.isEmpty() && !this.wrapperNamespace.equals(getNamespace())) {
      //if the namespace differs, we need a value and a title.
      return new PropertyMetadata(this.wrapperName, "{" + this.wrapperNamespace + "}" + this.wrapperName, null);
    }
    else {
      return new PropertyMetadata(this.wrapperName);
    }
  }

  public String getWrapperName() {
    return wrapperName;
  }

  public String getWrapperNamespace() {
    return wrapperNamespace;
  }
}
