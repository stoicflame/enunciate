package net.sf.enunciate.config;

import net.sf.enunciate.contract.validation.DefaultValidator;
import net.sf.enunciate.contract.validation.Validator;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * Base configuration object for enunciate.
 *
 * @author Ryan Heaton
 */
public class EnunciateConfiguration {

  private Validator validator;
  private LinkedHashMap<String, String> clientPackageConversions = new LinkedHashMap<String, String>();

  /**
   * The configured validator, if any.
   *
   * @return The configured validator, or null if none.
   */
  public Validator getValidator() {
    return validator;
  }

  /**
   * The validator to use.
   *
   * @param validator The validator to use.
   */
  public void setValidator(Validator validator) {
    this.validator = validator;
  }

  /**
   * The client package conversions.
   *
   * @return The client package conversions.
   */
  public LinkedHashMap<String, String> getClientPackageConversions() {
    return clientPackageConversions;
  }

  /**
   * Add a client package conversion.
   *
   * @param conversion The conversion to add.
   */
  public void addClientPackageConversion(ClientPackageConversion conversion) {
    String from = conversion.getFrom();
    String to = conversion.getTo();

    if (from == null) {
      throw new IllegalArgumentException("A 'from' attribute must be specified on a clientPackageConversion element.");
    }

    if (to == null) {
      throw new IllegalArgumentException("A 'to' attribute must be specified on a clientPackageConversion element.");
    }

    this.clientPackageConversions.put(from, to);
  }

  /**
   * Reads a configuration from an input stream.
   *
   * @param in The input stream to read from.
   * @return The configuration.
   */
  public static EnunciateConfiguration readFrom(InputStream in) throws IOException, SAXException {
    Digester digester = new Digester();
    digester.setValidating(false);
    digester.addObjectCreate("enunciate", EnunciateConfiguration.class);

    //allow a validator to be configured.
    digester.addObjectCreate("enunciate/validator", "class", DefaultValidator.class);
    digester.addSetNext("enunciate/validator", "validator");

    //allow client package conversions to be configured.
    digester.addObjectCreate("enunciate/client-package-conversions/convert", ClientPackageConversion.class);
    digester.addSetProperties("enunciate/client-package-conversions/convert");
    digester.addSetNext("enunciate/client-package-conversions/convert", "addClientPackageConversion");

    return (EnunciateConfiguration) digester.parse(in);
  }

}
