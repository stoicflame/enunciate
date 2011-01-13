package org.codehaus.enunciate.samples.genealogy.exceptions;

import org.codehaus.enunciate.samples.genealogy.data.Person;

import javax.xml.ws.WebFault;

@WebFault ( faultBean = "org.codehaus.enunciate.samples.genealogy.data.Person" )
public class EisAccountException extends Exception {

  private static final long serialVersionUID = 6609084036637969280L;
  private Person customer;


  public EisAccountException(String message, Person customer, Throwable cause) {
    super(message, cause);
    this.customer = customer;
  }


  public EisAccountException(String message, Person customer) {
    super(message);
    this.customer = customer;
  }


  public Person getFaultInfo() {
    return customer;
  }

}