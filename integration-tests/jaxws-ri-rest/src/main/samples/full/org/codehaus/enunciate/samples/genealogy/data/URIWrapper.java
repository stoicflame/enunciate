/*
 * Copyright (c) 2011 Energy Intellect
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Energy
 * Intellect. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Energy Intellect.
 */
package org.codehaus.enunciate.samples.genealogy.data;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Wrapper for a URI, so JAXB can create elements with href attributes for URIs.
 *
 * @author Ken Duoba.
 */
@XmlType
public class URIWrapper
{
  private URI uri;

  /**
   * Constructor with uri given.
   *
   * @param uri
   */
  public URIWrapper(URI uri)
  {
    this.uri = uri;
  }

  /**
   * Constructor
   */
  public URIWrapper()
  {
    super();
  }

  /**
   * @return the wrapped URI
   */
  @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink", required=true)
  //@XmlSchemaType(name = "anyURI")
  public URI getUri()
  {
    return uri;
  }

  /**
   * Sets the wrapped URI.
   *
   * @param uri
   *          a URI or null.
   */
  public void setUri(URI uri)
  {
    this.uri = uri;
  }
}
