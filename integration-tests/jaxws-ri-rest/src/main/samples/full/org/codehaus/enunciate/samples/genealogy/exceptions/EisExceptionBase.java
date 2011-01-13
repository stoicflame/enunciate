package org.codehaus.enunciate.samples.genealogy.exceptions;

public class EisExceptionBase extends Exception {


  private static final long serialVersionUID = -1278972432304986397L;


  private String msg;


  public EisExceptionBase() {

    super();

  }


  public EisExceptionBase(String message) {

    super(message);

    this.msg = message;

  }


  public EisExceptionBase(String message, Throwable cause) {

    super(message, cause);

    this.msg = message;

  }


  public String getMsg() {

    return msg;

  }


  public void setMsg(String msg) {

    this.msg = msg;

  }

}