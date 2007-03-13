/**
 * The original code for this sample model was taken from the samples
 * for spring framework.  See http://www.springframework.org.
 */
package org.codehaus.enunciate.samples.petclinic;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enumeration of specialties.
 *
 * @author Ryan Heaton
 */
@XmlRootElement (
  namespace = "http://org.codehaus.enunciate/samples/petclinic/vets"
)
public enum Specialty {

  RADIOLOGY,

  SURGERY,

  DENTISTRY

}
