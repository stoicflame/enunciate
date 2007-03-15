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

@XmlSchema (
  xmlns = {
    @XmlNs(prefix = "another1", namespaceURI = "http://org.codehaus.enunciate/core/samples/another1"),
    @XmlNs(prefix = "another2", namespaceURI = "http://org.codehaus.enunciate/core/samples/another2")
  },

  namespace = "http://org.codehaus.enunciate/core/samples/another",

  elementFormDefault = XmlNsForm.QUALIFIED,

  attributeFormDefault = XmlNsForm.QUALIFIED
)
@XmlSchemaType (
  name = "specified-bean-four",
  namespace = "http://org.codehaus.enunciate/core/samples/beanfour",
  type = BeanFour.class
)
package org.codehaus.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchemaType;