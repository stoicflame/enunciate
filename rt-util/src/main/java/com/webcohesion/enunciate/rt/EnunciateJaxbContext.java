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
package com.webcohesion.enunciate.rt;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import javax.xml.bind.*;

/**
 * @author Ryan Heaton
 */
public class EnunciateJaxbContext extends JAXBContext {

  private final JAXBContext delegate;
  private final NamespacePrefixMapper namespacePrefixMapper;

  public EnunciateJaxbContext(JAXBContext delegate, NamespacePrefixMapper namespacePrefixMapper) {
    this.delegate = delegate;
    this.namespacePrefixMapper = namespacePrefixMapper;
  }

  public Unmarshaller createUnmarshaller() throws JAXBException {
    return this.delegate.createUnmarshaller();
  }

  public Marshaller createMarshaller() throws JAXBException {
    Marshaller marshaller = this.delegate.createMarshaller();
    if (this.namespacePrefixMapper != null) {
      marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", this.namespacePrefixMapper);
    }
    return marshaller;
  }

  public Validator createValidator() throws JAXBException {
    return this.delegate.createValidator();
  }
}
