package org.springframework.security.oauth.examples.sparklr;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Photos {

  private Collection<PhotoInfo> photoItems;

  @XmlElement (
    name = "photo"
  )
  public Collection<PhotoInfo> getPhotoItems() {
    return photoItems;
  }

  public void setPhotoItems(Collection<PhotoInfo> photoItems) {
    this.photoItems = photoItems;
  }
}
