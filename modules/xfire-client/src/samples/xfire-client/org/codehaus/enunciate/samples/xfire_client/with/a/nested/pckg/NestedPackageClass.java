/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.samples.xfire_client.with.a.nested.pckg;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement (
  name = "nested-pckg",
  namespace = "urn:nested-pckg"
)
public class NestedPackageClass {

  private Collection<NestedPackageItem> items;
  private NestedPackageEnum type;

  public Collection<NestedPackageItem> getItems() {
    return items;
  }

  public void setItems(Collection<NestedPackageItem> items) {
    this.items = items;
  }

  public NestedPackageEnum getType() {
    return type;
  }

  public void setType(NestedPackageEnum type) {
    this.type = type;
  }
}
