package net.sf.enunciate.samples.genealogy.data;

import java.util.Date;

/**
 * An assertion that occurs at some point in time.
 *
 * @author Ryan Heaton
 */
public abstract class OccurringAssertion extends Assertion {

  private Date date;
  private String place;

  /**
   * The date of the occurrence.
   *
   * @return The date of the occurrence.
   */
  public Date getDate() {
    return date;
  }

  /**
   * The date of the occurrence.
   *
   * @param date The date of the occurrence.
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * The place of the occurrence.
   *
   * @return The place of the occurrence.
   */
  public String getPlace() {
    return place;
  }

  /**
   * The place of the occurrence.
   *
   * @param place The place of the occurrence.
   */
  public void setPlace(String place) {
    this.place = place;
  }
}
