package net.sf.enunciate.samples.genealogy.services.impl;

import net.sf.enunciate.samples.genealogy.cite.InfoSet;
import net.sf.enunciate.samples.genealogy.cite.Source;
import net.sf.enunciate.samples.genealogy.services.SourceService;

import javax.jws.WebService;
import java.net.URI;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "net.sf.enunciate.samples.genealogy.services.SourceService"
)
public class SourceServiceImpl implements SourceService {

  public void addSource(Source source) {

  }

  public Source getSource(String id) {
    if ("valid".equals(id)) {
      Source source = new Source();
      source.setId("valid");
      source.setLink(URI.create("uri:some-uri"));
      source.setTitle("some-title");
      return source;
    }

    return null;
  }

  public String addInfoSet(String sourceId, InfoSet infoSet) {
    return null;
  }
}
