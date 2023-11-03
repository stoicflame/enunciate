/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.mojo;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.artifacts.Artifact;
import com.webcohesion.enunciate.module.DocumentationProviderModule;
import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.module.WebInfAwareModule;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Assembles the Enunciate documentation.
 *
 * @author Ryan Heaton
 */
@Mojo ( name = "assemble", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, threadSafe = true )
public class AssembleBaseMojo extends ConfigMojo {

  /**
   * The directory where the docs are put.
   */
  @Parameter( defaultValue = "${project.build.directory}/${project.build.finalName}", property = "enunciate.docsDir", required = true )
  protected String docsDir;

  /**
   * The name of the subdirectory where the documentation is put.
   */
  @Parameter
  protected String docsSubdir;

  /**
   * The directory where the webapp is built.  If using this goal along with "war" packaging, this must be configured to be the
   * same value as the "webappDirectory" parameter to the war plugin.
   */
  @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", property = "enunciate.webappDirectory")
  protected String webappDirectory;

  /**
   * Whether to force the "packaging" of the project to be "war" packaging.
   */
  @Parameter(defaultValue = "true")
  protected boolean forceWarPackaging = true;

  /**
   * The path to the WEB-INF directory for the webapp.
   */
  @Parameter( defaultValue = "${basedir}/src/main/webapp/WEB-INF" )
  protected String webInfDirectory;

  @Override
  protected void applyAdditionalConfiguration(EnunciateModule module) {
    super.applyAdditionalConfiguration(module);

    if (module instanceof DocumentationProviderModule) {
      DocumentationProviderModule docsProvider = (DocumentationProviderModule) module;
      docsProvider.setDefaultDocsDir(new File(this.docsDir));
      if (this.docsSubdir != null) {
        docsProvider.setDefaultDocsSubdir(this.docsSubdir);
      }
    }

    if (module instanceof WebInfAwareModule) {
      ((WebInfAwareModule)module).setWebInfDir(new File(this.webInfDirectory));
    }
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skipEnunciate) {
      getLog().info("Skipping enunciate per configuration.");
      return;
    }

    if (forceWarPackaging && !"war".equalsIgnoreCase(this.project.getPackaging())) {
      throw new MojoExecutionException("The 'assemble' goal requires 'war' packaging.");
    }

    super.execute();
  }

  @Override
  protected void postProcess(Enunciate enunciate) {
    super.postProcess(enunciate);

    File webInfClasses = new File(new File(new File(this.webappDirectory), "WEB-INF"), "classes");
    webInfClasses.mkdirs();

    Set<com.webcohesion.enunciate.artifacts.Artifact> artifacts = enunciate.getArtifacts();
    for (Artifact artifact : artifacts) {
      if (artifact.isBelongsOnServerSideClasspath()) {
        try {
          artifact.exportTo(webInfClasses, enunciate);
        }
        catch (IOException e) {
          throw new EnunciateException(e);
        }
      }
    }
  }
}
