/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.samples.idl.genealogy.services.impl;

import com.webcohesion.enunciate.samples.idl.genealogy.cite.Source;
import com.webcohesion.enunciate.samples.idl.genealogy.cite.InfoSet;
import com.webcohesion.enunciate.samples.idl.genealogy.data.Event;
import com.webcohesion.enunciate.samples.idl.genealogy.services.SourceService;
import com.webcohesion.enunciate.samples.idl.genealogy.services.UnknownSourceException;

import jakarta.jws.WebService;
import java.net.URI;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import com.webcohesion.enunciate.samples.idl.genealogy.services.ServiceException;
import com.webcohesion.enunciate.samples.idl.genealogy.services.UnknownSourceBean;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "com.webcohesion.enunciate.samples.idl.genealogy.services.SourceService"
)
@jakarta.ws.rs.Path("source")
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
