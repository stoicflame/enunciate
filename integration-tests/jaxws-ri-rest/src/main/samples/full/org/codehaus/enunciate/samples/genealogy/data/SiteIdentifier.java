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

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

/**
 * Identifier for a site - includes the site's ID and name.
 *
 * @author Ken Duoba.
 */
@XmlRootElement(name = "SiteIdentifier")
public class SiteIdentifier
{
  private String code;

  private URI detailsURI;

  private URIWrapper deviceStatusURI;

  private String ID;

  private URIWrapper intervalDataURI;

  private String name;

  /** Path to the site in the site tree */
  private String path;

  /**
   * @return The value of the code property.
   */
  @XmlElement(name = "Code")
  public String getCode()
  {
    return code;
  }

  @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink", required = true)
  @XmlSchemaType(name = "anyURI", namespace = XMLConstants.W3C_XML_SCHEMA_NS_URI)
  public URI getDetailsURI()
  {
    return detailsURI;
  }

  /**
   * @return The value of the deviceStatusURI property.
   */
  @XmlElement(name = "DeviceStatus")
  public URIWrapper getDeviceStatusURI()
  {
    return deviceStatusURI;
  }

  @XmlAttribute(name = "id")
  @XmlID
  public String getID()
  {
    return ID;
  }

  /**
   * @return The value of the intervalDataURI property.
   */
  @XmlElement(name = "IntervalData")
  public URIWrapper getIntervalDataURI()
  {
    return intervalDataURI;
  }

  @XmlElement(name = "Name")
  public String getName()
  {
    return name;
  }

  @XmlElement(name = "Path")
  public String getPath()
  {
    return path;
  }

  /**
   * @param code
   *          The new value for the code property.
   */
  public void setCode(String code)
  {
    this.code = code;
  }

  public void setDetailsURI(URI siteURI)
  {
    this.detailsURI = siteURI;
  }

  public void setDeviceStatusURI(URIWrapper deviceStatusURI)
  {
    this.deviceStatusURI = deviceStatusURI;
  }

  public void setDeviceStatusURI(URI deviceStatusURI)
  {
    this.deviceStatusURI = deviceStatusURI == null
        ? null
          : new URIWrapper(deviceStatusURI);
  }

  public void setID(String siteID)
  {
    this.ID = siteID;
  }

  public void setIntervalDataURI(URIWrapper intervalDataURI)
  {
    this.intervalDataURI = intervalDataURI;
  }

  public void setIntervalDataURI(URI intervalDataURI)
  {

    this.intervalDataURI = intervalDataURI == null
        ? null
          : new URIWrapper(intervalDataURI);
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

}
