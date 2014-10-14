@XmlSchema(
  namespace = "hello",
  xmlns = {
    @XmlNs(prefix = "p1", namespaceURI = "urn:p1"),
    @XmlNs(prefix = "p2", namespaceURI = "urn:p2")
  },
  elementFormDefault = XmlNsForm.QUALIFIED
)
@XmlJavaTypeAdapters (
  @XmlJavaTypeAdapter ( value = XmlAdapter.class )
)
package org.codehaus.enunciate.util.testpckg;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;