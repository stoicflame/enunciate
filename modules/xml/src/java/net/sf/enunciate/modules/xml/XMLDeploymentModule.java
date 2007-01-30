package net.sf.enunciate.modules.xml;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xml.config.SchemaConfig;
import net.sf.enunciate.modules.xml.config.WsdlConfig;
import net.sf.enunciate.modules.xml.config.XMLRuleSet;
import net.sf.enunciate.main.FileArtifact;
import org.apache.commons.digester.RuleSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * Deployment module for the XML schemas and WSDL.
 *
 * @author Ryan Heaton
 */
public class XMLDeploymentModule extends FreemarkerDeploymentModule {

  private boolean prettyPrint = true;
  private boolean validateSchemas = true;
  private final XMLAPIObjectWrapper xmlWrapper = new XMLAPIObjectWrapper();
  private final XMLRuleSet rules = new XMLRuleSet();
  private final ArrayList<SchemaConfig> schemaConfigs = new ArrayList<SchemaConfig>();
  private final ArrayList<WsdlConfig> wsdlConfigs = new ArrayList<WsdlConfig>();

  /**
   * @return "xml"
   */
  @Override
  public String getName() {
    return "xml";
  }

  /**
   * The URL to "xml.fmt".
   *
   * @return The URL to "xml.fmt".
   */
  protected URL getTemplateURL() {
    return XMLDeploymentModule.class.getResource("xml.fmt");
  }

  /**
   * Add a custom schema configuration.
   *
   * @param config The configuration to add.
   */
  public void addSchemaConfig(SchemaConfig config) {
    this.schemaConfigs.add(config);
  }

  /**
   * Add a custom wsdl configuration.
   *
   * @param config The configuration to add.
   */
  public void addWsdlConfig(WsdlConfig config) {
    this.wsdlConfigs.add(config);
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    Map<String, SchemaInfo> ns2schema = model.getNamespacesToSchemas();
    Map<String, String> ns2prefix = model.getNamespacesToPrefixes();
    Map<String, WsdlInfo> ns2wsdl = model.getNamespacesToWSDLs();

    for (SchemaInfo schemaInfo : ns2schema.values()) {
      //make sure each schema has a "file" and a "location" property.
      String prefix = ns2prefix.get(schemaInfo.getNamespace());
      if (prefix != null) {
        String file = prefix + ".xsd";
        schemaInfo.setProperty("filename", file);
        schemaInfo.setProperty("location", file);
      }
    }

    for (WsdlInfo wsdlInfo : ns2wsdl.values()) {
      //make sure each wsdl has a "file" property.
      String prefix = ns2prefix.get(wsdlInfo.getTargetNamespace());
      if (prefix != null) {
        String file = prefix + ".wsdl";
        wsdlInfo.setProperty("filename", file);
      }
    }

    for (SchemaConfig customConfig : this.schemaConfigs) {
      SchemaInfo schemaInfo = ns2schema.get(customConfig.getNamespace());

      if (schemaInfo != null) {
        if (customConfig.getFile() != null) {
          schemaInfo.setProperty("filename", customConfig.getFile());
          schemaInfo.setProperty("location", customConfig.getFile());
        }

        if (customConfig.getLocation() != null) {
          schemaInfo.setProperty("location", customConfig.getLocation());
        }
      }
    }

    for (WsdlConfig customConfig : this.wsdlConfigs) {
      WsdlInfo wsdlInfo = ns2wsdl.get(customConfig.getNamespace());

      if (wsdlInfo != null) {
        if (customConfig.getFile() != null) {
          wsdlInfo.setProperty("filename", customConfig.getFile());
        }
      }
    }

    model.put("prefix", new PrefixMethod());
    File artifactDir = getGenerateDir();
    model.setFileOutputDirectory(artifactDir);
    processTemplate(getTemplateURL(), model);

    for (WsdlInfo wsdl : ns2wsdl.values()) {
      String file = (String) wsdl.getProperty("filename");
      File wsdlFile = new File(artifactDir, file);
      wsdl.setProperty("file", wsdlFile);

      if (prettyPrint) {
        prettyPrint(wsdlFile);
      }

      FileArtifact wsdlArtifact = new FileArtifact(getName(), wsdl.getId() + ".wsdl", wsdlFile);
      wsdlArtifact.setDescription("WSDL file for namespace " + wsdl.getTargetNamespace());
      getEnunciate().addArtifact(wsdlArtifact);
    }

    for (SchemaInfo schemaInfo : ns2schema.values()) {
      String file = (String) schemaInfo.getProperty("filename");
      File schemaFile = new File(artifactDir, file);
      schemaInfo.setProperty("file", schemaFile);

      if (prettyPrint) {
        prettyPrint(schemaFile);
      }

      if (validateSchemas) {
        //todo: write some logic to validate the schemas.
      }

      FileArtifact schemaArtifact = new FileArtifact(getName(), schemaInfo.getId() + ".xsd", schemaFile);
      schemaArtifact.setDescription("Schema file for namespace " + schemaInfo.getNamespace());
      getEnunciate().addArtifact(schemaArtifact);
    }
  }

  /**
   * Pretty-prints the specified xml file.
   *
   * @param file The file to pretty-print.
   */
  protected void prettyPrint(File file) {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(false);
      SAXParser parser = factory.newSAXParser();
      File prettyFile = File.createTempFile("enunciate", file.getName());
      parser.parse(file, new PrettyPrinter(prettyFile));

      if (file.delete()) {
        enunciate.copyFile(prettyFile, file);
      }
      else {
        System.err.println("Unable to delete " + file.getAbsolutePath() + ".  Skipping pretty-print transformation....");
      }
    }
    catch (Exception e) {
      //fall through... skip pretty printing.
      System.err.println("Unable to pretty-print " + file.getAbsolutePath() + " (" + e.getMessage() + ").  Skipping pretty-print transformation....");
      if (enunciate.isDebug()) {
        e.printStackTrace(System.err);
      }
    }
  }

  @Override
  protected ObjectWrapper getObjectWrapper() {
    return xmlWrapper;
  }

  @Override
  public RuleSet getConfigurationRules() {
    return this.rules;
  }

  @Override
  public Validator getValidator() {
    return new XMLValidator();
  }

  /**
   * Whether to pretty-print the xml.
   *
   * @param prettyPrint Whether to pretty-print the xml.
   */
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  /**
   * Whether to validate the generated schemas in an attempt to catch possible errors that enunciate might have missed.
   *
   * @param validateSchemas Whether to validate the generated schemas in an attempt to catch possible errors that enunciate might have missed.
   */
  public void setValidateSchemas(boolean validateSchemas) {
    this.validateSchemas = validateSchemas;
  }
}
