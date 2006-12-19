package net.sf.enunciate.samples.genealogy.services.impl;

import net.sf.enunciate.samples.genealogy.cite.InfoSet;
import net.sf.enunciate.samples.genealogy.cite.Source;
import net.sf.enunciate.samples.genealogy.data.Event;
import net.sf.enunciate.samples.genealogy.services.ServiceException;
import net.sf.enunciate.samples.genealogy.services.SourceService;
import net.sf.enunciate.samples.genealogy.services.UnknownSourceBean;
import net.sf.enunciate.samples.genealogy.services.UnknownSourceException;

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
    try {
      Thread.sleep(10 * 1000); //simulate a long wait time...  doens't matter, should be one-way...
    }
    catch (InterruptedException e) {
      //fall through...
    }
  }

  public Source getSource(String id) throws ServiceException, UnknownSourceException {
    if ("valid".equals(id)) {
      Source source = new Source();
      source.setId("valid");
      source.setLink(URI.create("uri:some-uri"));
      source.setTitle("some-title");
      return source;
    }
    else if ("throw".equals(id)) {
      throw new ServiceException("some message", "another message");
    }
    else if ("unknown".equals(id)) {
      UnknownSourceBean bean = new UnknownSourceBean();
      bean.setSourceId("unknown");
      bean.setErrorCode(888);
      throw new UnknownSourceException("some message", bean);
    }

    return null;
  }

  public String addInfoSet(String sourceId, InfoSet infoSet) throws ServiceException {
    if ("somesource".equals(sourceId)) {
      return "newid";
    }
    else if ("unknown".equals(sourceId)) {
      throw new ServiceException("unknown source id", "anyhow");
    }

    return "okay";
  }

  public String addEvents(String infoSetId, Event[] assertions, String contributorId) throws ServiceException {
    if ("illegal".equals(contributorId)) {
      throw new ServiceException("illegal contributor", "illegal");
    }

    if ("unknown".equals(infoSetId)) {
      throw new ServiceException("unknown info set", infoSetId);
    }

    if (assertions.length < 3) {
      throw new ServiceException("you must add three", "three");
    }

    return contributorId;
  }
}
