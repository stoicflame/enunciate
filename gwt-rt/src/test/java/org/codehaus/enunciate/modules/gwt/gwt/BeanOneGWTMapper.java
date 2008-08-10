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

package org.codehaus.enunciate.modules.gwt.gwt;

import org.codehaus.enunciate.modules.gwt.BaseGWTMapper;
import org.codehaus.enunciate.modules.gwt.BeanOne;
import org.codehaus.enunciate.modules.gwt.GWTBeanOne;

/**
 * @author Ryan Heaton
 */
public class BeanOneGWTMapper extends BaseGWTMapper {

  public BeanOneGWTMapper() {
    super(BeanOne.class, GWTBeanOne.class, "property1", "property2", "property3", "property4", "property5", "property6", "property7", "property8", "property9");
  }
}
