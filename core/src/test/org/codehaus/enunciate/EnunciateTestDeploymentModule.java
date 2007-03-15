/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate;

import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.BasicDeploymentModule;

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
