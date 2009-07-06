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

package org.codehaus.enunciate.samples.docs.pckg3;

import org.codehaus.enunciate.samples.docs.pckg2.BeanTwo;

/**
 * docs for RESTEI
 * 
 * @author Ryan Heaton
 */
@javax.ws.rs.Path("{id}")
public class RESTEI {

  /**
   * documentation for <span>method1</span>
   *
   * @param two docs for two
   * @param id docs for id
   * @param param1 docs for param1
   * @param param2 docs for param2
   * @return docs for return
   * @throws FaultTwo if something bad happens
   */
  @javax.ws.rs.POST
  public BeanTwo method1(BeanTwo two, @javax.ws.rs.PathParam( "id" ) String id, @javax.ws.rs.QueryParam("param1") String param1, @javax.ws.rs.QueryParam("param2") String param2) throws FaultTwo {
    return null;
  }

}
