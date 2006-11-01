@XmlSchema (
  xmlns = {
    @XmlNs(prefix = "another1", namespaceURI = "http://net.sf.enunciate/core/samples/another1"),
    @XmlNs(prefix = "another2", namespaceURI = "http://net.sf.enunciate/core/samples/another2")
  },

  namespace = "http://net.sf.enunciate/core/samples/another",

  elementFormDefault = XmlNsForm.QUALIFIED,

  attributeFormDefault = XmlNsForm.QUALIFIED
)
@XmlSchemaType (
  name = "specified-bean-four",
  namespace = "http://net.sf.enunciate/core/samples/beanfour",
  type = BeanFour.class
)
package net.sf.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchemaType;