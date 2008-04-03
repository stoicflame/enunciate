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

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.codehaus.enunciate.modules.gwt.BeansService"
)
public class BeansServiceImpl implements BeansService {

  public BeanOne getSomething(String id, BeanOneDotOne oneDotOne, int num) {
    BeanOne one = new BeanOne();
    one.setProperty4(oneDotOne);
    one.setProperty1(id);
    one.setProperty2(num);
    return one;
  }

  public BeanOne getSomethingElse(String id, BeanOneDotOne oneDotOne, int num) {
    BeanOne one = new BeanOne();
    one.setProperty4(oneDotOne);
    one.setProperty1(id + "hah");
    one.setProperty2(num + 1);
    return one;
  }

  public void doSomethingVoid() {
  }


  public int throwAnException(int i, byte b, Character c, String message) throws BeansServiceException {
    BeansServiceException ex = new BeansServiceException(message);
    ex.setProperty1(i);
    ex.setProperty2(b);
    ex.setProperty3(c);
    throw ex;
  }
}
