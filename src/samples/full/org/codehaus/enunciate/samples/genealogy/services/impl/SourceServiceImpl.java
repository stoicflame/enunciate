package org.codehaus.enunciate.samples.genealogy.services.impl;

import org.codehaus.enunciate.samples.genealogy.cite.InfoSet;
import org.codehaus.enunciate.samples.genealogy.cite.Source;
import org.codehaus.enunciate.samples.genealogy.data.Event;
import org.codehaus.enunciate.samples.genealogy.services.SourceService;
import org.codehaus.enunciate.samples.genealogy.services.UnknownSourceException;
import org.codehaus.enunciate.rest.annotations.RESTEndpoint;

import javax.jws.WebService;
import java.net.URI;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.codehaus.enunciate.samples.genealogy.services.ServiceException;
import org.codehaus.enunciate.samples.genealogy.services.UnknownSourceBean;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.codehaus.enunciate.samples.genealogy.services.SourceService"
)
@RESTEndpoint
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
    else if ("resource".equals(sourceId)) {
      InputStreamReader reader = new InputStreamReader(SourceServiceImpl.class.getResourceAsStream("infosetid.txt"));
      BufferedReader buffered = new BufferedReader(reader);
      try {
        return buffered.readLine();
      }
      catch (IOException e) {
        throw new ServiceException("unable to read", e.getMessage());
      }
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
