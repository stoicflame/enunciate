/*
 * Copyright 2006-2008 Web Cohesion
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

package org.codehaus.enunciate.modules.gwt;

import java.net.URI;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class BeanOneDotTwo extends BeanOneDotOne {
  private URI property3;
  private Collection<String> property4;

  public URI getProperty3() {
    return property3;
  }

  public void setProperty3(URI property3) {
    this.property3 = property3;
  }

  public Collection<String> getProperty4() {
    return property4;
  }

  public void setProperty4(Collection<String> property4) {
    this.property4 = property4;
  }
}
