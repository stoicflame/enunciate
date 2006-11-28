package net.sf.enunciate.samples.genealogy.services.impl;

import net.sf.enunciate.samples.genealogy.cite.InfoSet;
import net.sf.enunciate.samples.genealogy.cite.Source;
import net.sf.enunciate.samples.genealogy.services.SourceService;

import javax.jws.WebService;

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
    return null;
  }

  public String addInfoSet(String sourceId, InfoSet infoSet) {
    return null;
  }
}
