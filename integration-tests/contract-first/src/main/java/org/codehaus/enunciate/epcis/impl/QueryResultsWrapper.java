package org.codehaus.enunciate.epcis.impl;

import epcglobal.epcis_query.xsd._1.QueryResults;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class QueryResultsWrapper {

  private QueryResults results;

  public QueryResults getResults() {
    return results;
  }

  public void setResults(QueryResults results) {
    this.results = results;
  }
}
