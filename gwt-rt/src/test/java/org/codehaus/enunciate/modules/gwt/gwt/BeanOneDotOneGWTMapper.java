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
import org.codehaus.enunciate.modules.gwt.BeanOneDotOne;
import org.codehaus.enunciate.modules.gwt.GWTBeanOneDotOne;

/**
 * @author Ryan Heaton
 */
public class BeanOneDotOneGWTMapper extends BaseGWTMapper {

  public BeanOneDotOneGWTMapper() {
    super(BeanOneDotOne.class, GWTBeanOneDotOne.class, "property1", "property2");
  }

  protected BeanOneDotOneGWTMapper(Class<? extends BeanOneDotOne> jaxbClass, Class<? extends GWTBeanOneDotOne> gwtClass, String... properties) {
    super(jaxbClass, gwtClass, BaseGWTMapper.append(properties, "property1", "property2"));
  }
}
