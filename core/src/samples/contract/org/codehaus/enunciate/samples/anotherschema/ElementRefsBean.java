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

package org.codehaus.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementRef;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class ElementRefsBean {

  private Collection<BeanOne> beanOnes;
  private Collection<BeanThree> beanThrees;
  private Collection<Object> foursAndThrees;

  @XmlElementRef
  public Collection<BeanOne> getBeanOnes() {
    return beanOnes;
  }

  public void setBeanOnes(Collection<BeanOne> beanOnes) {
    this.beanOnes = beanOnes;
  }

  @XmlElementRef
  public Collection<BeanThree> getBeanThrees() {
    return beanThrees;
  }

  public void setBeanThrees(Collection<BeanThree> beanThrees) {
    this.beanThrees = beanThrees;
  }

  @XmlElementRefs (
    {@XmlElementRef (
      type=BeanFour.class
    ),
    @XmlElementRef (
      type = BeanThree.class
    )}
  )
  public Collection<Object> getFoursAndThrees() {
    return foursAndThrees;
  }

  public void setFoursAndThrees(Collection<Object> foursAndThrees) {
    this.foursAndThrees = foursAndThrees;
  }
}
