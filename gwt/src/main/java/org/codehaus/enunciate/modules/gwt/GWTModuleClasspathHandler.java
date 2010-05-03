package org.codehaus.enunciate.modules.gwt;

import org.codehaus.enunciate.main.ClasspathHandler;
import org.codehaus.enunciate.main.ClasspathResource;
import org.codehaus.enunciate.main.Enunciate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class GWTModuleClasspathHandler implements ClasspathHandler {

  private final Set<String> gwtSourcePackages = new TreeSet<String>();
  private final DocumentBuilder documentBuilder;
  private final Enunciate enunciate;

  public GWTModuleClasspathHandler(Enunciate enunciate) {
    this.enunciate = enunciate;
    try {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setValidating(false);
      builderFactory.setIgnoringElementContentWhitespace(true);
      documentBuilder = builderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  public Set<String> getGwtSourcePackages() {
    return gwtSourcePackages;
  }

  public void startPathEntry(File pathEntry) {
  }

  public void handleResource(ClasspathResource resource) {
    String path = resource.getPath();
    if (path.endsWith(".gwt.xml")) {
      String modulename = path.substring(0, path.length() - 8).replace('/', '.');
      enunciate.debug("Noticed GWT module %s.", modulename);
      int lastDot = modulename.lastIndexOf('.');
      if (lastDot < 0) {
        throw new IllegalStateException("Illegal GWT module name: " + modulename);
      }

      String modulePackage = modulename.substring(0, lastDot);
      try {
        InputStream resourceStream = resource.read();
        Document document = documentBuilder.parse(resourceStream);
        resourceStream.close();

        NodeList sourceNodes = document.getDocumentElement().getElementsByTagName("source");
        for (int i = 0; i < sourceNodes.getLength(); i++) {
          Element node = (Element) sourceNodes.item(i);
          String subPackage = node.getAttribute("path");
          String pckg = modulePackage + "." + subPackage;
          enunciate.debug("Any class in package %s (and any subpackages) will be preserved as GWT-compatible code.");
          this.gwtSourcePackages.add(pckg);
        }
      }
      catch (Exception e) {
        enunciate.warn("Unable to read GWT module XML (%s). Skipping...", e.getMessage());
      }
    }
  }

  public boolean endPathEntry(File pathEntry) {
    return false;
  }
}
