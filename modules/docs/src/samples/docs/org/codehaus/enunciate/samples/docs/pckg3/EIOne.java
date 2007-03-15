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

package org.codehaus.enunciate.samples.docs.pckg3;

import javax.jws.WebService;

/**
 * documentation for EIOne.
 *
 * @author Ryan Heaton
 * @deprecated some reason
 */
@WebService
public interface EIOne {

  /**
   * docs for method1
   */
  void method1();

  /**
   * docs for method2
   * <i>should</i> be marked up however you like.
   *
   * @param param1 docs for method2.param1
   * @param param2 docs for method2.param2
   * @return return docs for method2
   * @someother someother value
   */
  int method2(String param1, String param2) throws FaultOne ;
}
