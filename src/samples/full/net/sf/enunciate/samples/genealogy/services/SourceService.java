package net.sf.enunciate.samples.genealogy.services;

import net.sf.enunciate.samples.genealogy.cite.InfoSet;
import net.sf.enunciate.samples.genealogy.cite.Source;
import net.sf.enunciate.samples.genealogy.data.Event;
import net.sf.enunciate.rest.annotations.*;

import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;

/**
 * @author Ryan Heaton
 */
@WebService (
  name = "source-service",
  targetNamespace = "http://enunciate.sf.net/samples/full",
  serviceName = "source-service",
  portName = "source-service-port"
)
@RESTEndpoint
public interface SourceService {

  @Oneway
  @Noun ("source")
  @Verb ( VerbType.create )
  void addSource(@NounValue Source source);

  @Noun ("source")
  @Verb ( VerbType.read )
  Source getSource(@ProperNoun String id) throws ServiceException, UnknownSourceException;

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  String addInfoSet(String sourceId, InfoSet infoSet) throws ServiceException;

  @WebResult (
    header = true
  )
  String addEvents(String infoSetId, Event[] assertions, @WebParam (header = true, name="contributorId") String contributorId) throws ServiceException;
}
