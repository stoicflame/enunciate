package com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data;

/**
 * @author Ryan Heaton
 */
public class PersonResults {

  private JsonMap<String, Person> results;

  public JsonMap<String, Person> getResults() {
    return results;
  }

  public void setResults(JsonMap<String, Person> results) {
    this.results = results;
  }
}
