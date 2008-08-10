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

package org.codehaus.enunciate.modules.amf;

import javax.xml.namespace.QName;

/**
 * @author Ryan Heaton
 */
public class QNameAMFMapper implements CustomAMFMapper<QName, String> {

  public String toAMF(QName jaxbObject, AMFMappingContext context) throws AMFMappingException {
    return jaxbObject == null ? null : jaxbObject.toString();
  }

  public QName toJAXB(String amfObject, AMFMappingContext context) throws AMFMappingException {
    return amfObject == null ? null : QName.valueOf(amfObject);
  }

  public Class<? extends QName> getJaxbClass() {
    return QName.class;
  }

  public Class<? extends String> getAmfClass() {
    return String.class;
  }
}
