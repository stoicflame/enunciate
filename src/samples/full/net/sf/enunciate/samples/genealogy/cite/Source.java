package net.sf.enunciate.samples.genealogy.cite;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import java.net.URI;

/**
 * A source of genealogical information.
 *
 * @author Ryan Heaton
 */
@XmlRootElement
public class Source {

  private String id;
  private String title;
  private URI link;
  private InfoSet[] infoSets;
  private Repository repository;

  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public URI getLink() {
    return link;
  }

  public void setLink(URI link) {
    this.link = link;
  }

  public InfoSet[] getInfoSets() {
    return infoSets;
  }

  public void setInfoSets(InfoSet[] infoSets) {
    this.infoSets = infoSets;
  }

  @XmlElementRef
  public Repository getRepository() {
    return repository;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }
}
