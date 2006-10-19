package net.sf.enunciate;

import net.sf.enunciate.modules.BasicDeploymentModule;
import net.sf.enunciate.main.Enunciate;

import java.io.IOException;

/**
 * This deployment module is used to run the tests that depend on the sample source code.
 * 
 * @author Ryan Heaton
 */
public class EnunciateTestDeploymentModule extends BasicDeploymentModule {

  @Override
  public String getName() {
    return "enunciate-test";
  }

  @Override
  public String getNamespace() {
    return "http://enunciate.sf.net";
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);
  }

  @Override
  protected void doGenerate() throws EnunciateException, IOException {
    super.doGenerate();
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();
  }

  @Override
  protected void doCompile() throws EnunciateException, IOException {
    super.doCompile();
  }

  @Override
  protected void doPackage() throws EnunciateException, IOException {
    super.doPackage();
  }

  @Override
  public void close() throws EnunciateException {
    super.close();
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }
}
