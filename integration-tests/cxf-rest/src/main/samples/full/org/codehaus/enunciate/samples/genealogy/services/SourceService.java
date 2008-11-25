/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.samples.genealogy.services;

import org.codehaus.enunciate.rest.annotations.*;
import org.codehaus.enunciate.samples.genealogy.cite.InfoSet;
import org.codehaus.enunciate.samples.genealogy.cite.Source;
import org.codehaus.enunciate.samples.genealogy.data.Event;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * The source service is used to access and store source information about genealogical information.
 * Compared to the person service, this one is much more customized.
 *
 * @author Ryan Heaton
 */
@WebService (
  name = "source-service",
  targetNamespace = "http://enunciate.codehaus.org/samples/full",
  serviceName = "source-service",
  portName = "source-service-port"
)
@RESTEndpoint
public interface SourceService {

  /**
   * Adds a source to the database.  Example of a one-way method.
   *
   * @param source The source to add to the database.
   */
  @Oneway
  @Noun ("source")
  @Verb ( VerbType.create )
  void addSource(@NounValue Source source);

  /**
   * Reads a source from the database.
   *
   * @param id The id of the source to read from the database.
   * @return The source.
   * @throws ServiceException If a source couldn't be read from the database.
   * @throws UnknownSourceException If no source by that id was found in the database.
   */
  @Noun ("source")
  @Verb ( VerbType.read )
  Source getSource(@ProperNoun String id) throws ServiceException, UnknownSourceException;

  /**
   * Adds an infoset to a specified source.
   *
   * @param sourceId The id of the source to which to add the infoset.
   * @param infoSet The infoset to add to the source.
   * @return The id of the infoset that was added.
   * @throws ServiceException If the infoset couldn't be added to the source.
   */
  String addInfoSet(String sourceId, InfoSet infoSet) throws ServiceException;

  /**
   * Adds a bunch of events to an infoset.
   *
   * @param infoSetId The id of the infoset to which to add the events.
   * @param assertions The events that are to be added to the infoset.
   * @param contributorId The if of the contributor adding the events to the infoset.
   * @return Some message.
   * @throws ServiceException If the events couldn't be added to the infoset.
   */
  @WebResult (
    header = true,
    name = "resultOfAddingEvents"
  )
  String addEvents(String infoSetId, Event[] assertions, @WebParam (header = true, name="contributorId") String contributorId) throws ServiceException;
}
