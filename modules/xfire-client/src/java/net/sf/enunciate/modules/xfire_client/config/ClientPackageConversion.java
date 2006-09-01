package net.sf.enunciate.modules.xfire_client.config;

/**
 * Configuration specifying the conversion of client-side package names.
 *
 * @author Ryan Heaton
 */
public class ClientPackageConversion {

  private String from;
  private String to;

  /**
   * Regular expression to map from.
   *
   * @return Regular expression to map from.
   */
  public String getFrom() {
    return from;
  }

  /**
   * Regular expression to map from.
   *
   * @param from Regular expression to map from.
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * Package to map to.
   *
   * @return Package to map to.
   */
  public String getTo() {
    return to;
  }

  /**
   * Package to map to.
   *
   * @param to Package to map to.
   */
  public void setTo(String to) {
    this.to = to;
  }
}
