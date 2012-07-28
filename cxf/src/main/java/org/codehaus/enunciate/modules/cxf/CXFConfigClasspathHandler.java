package org.codehaus.enunciate.modules.cxf;

import org.codehaus.enunciate.main.ClasspathHandler;
import org.codehaus.enunciate.main.ClasspathResource;

import java.io.File;

/**
 * @author Ryan Heaton
 */
public class CXFConfigClasspathHandler implements ClasspathHandler {

  @Override
  public void startPathEntry(File pathEntry) {
  }

  @Override
  public void handleResource(ClasspathResource resource) {
  }

  @Override
  public boolean endPathEntry(File pathEntry) {
    return false;
  }
}
