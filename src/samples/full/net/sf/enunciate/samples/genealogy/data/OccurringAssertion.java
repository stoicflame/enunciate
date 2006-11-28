package net.sf.enunciate.samples.genealogy.data;

import java.util.Date;

/**
 * @author Ryan Heaton
 */
public abstract class OccurringAssertion extends Assertion {

  private Date date;
  private String place;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getPlace() {
    return place;
  }

  public void setPlace(String place) {
    this.place = place;
  }
}
