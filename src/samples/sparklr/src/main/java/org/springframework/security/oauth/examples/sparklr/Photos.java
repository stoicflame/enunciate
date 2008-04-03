package org.springframework.security.oauth.examples.sparklr;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Photos {

  private Collection<PhotoInfo> photos;

  @XmlElement (
    name = "photo"
  )
  public Collection<PhotoInfo> getPhotos() {
    return photos;
  }

  public void setPhotos(Collection<PhotoInfo> photos) {
    this.photos = photos;
  }
}
