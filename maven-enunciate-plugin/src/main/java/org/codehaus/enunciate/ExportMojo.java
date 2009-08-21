package org.codehaus.enunciate;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.BasicDeploymentModule;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.EndpointImplementation;
import org.codehaus.enunciate.contract.rest.RESTEndpoint;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.*;

import net.sf.jelly.apt.freemarker.FreemarkerModel;

/**
 * "Exports" the JAXB/JAX-WS classes in the current project. For use with the "jar" packaging.
 *
 * @goal export
 * @phase process-sources
 * @requiresDependencyResolution compile

 * @author Ryan Heaton
 * @link http://docs.codehaus.org/display/ENUNCIATE/Working+With+Precompiled+Classes
 */
public class ExportMojo extends ConfigMojo {

  /**
   * The directory where the jar is built.  If using this goal along with "jar" packaging, this must be configured to be the
   * same value as the "classesDirectory" parameter to the jar plugin.
   *
   * @parameter expression="${project.build.outputDirectory}"
   * @required
   */
  private String classesDirectory;

  @Override
  public void execute() throws MojoExecutionException {
    super.execute();

    Enunciate.Stepper stepper = (Enunciate.Stepper) getPluginContext().get(ConfigMojo.ENUNCIATE_STEPPER_PROPERTY);
    if (stepper == null) {
      throw new MojoExecutionException("No stepper found in the project!");
    }

    try {
      stepper.stepTo(Enunciate.Target.GENERATE);
      stepper.close();
    }
    catch (Exception e) {
      throw new MojoExecutionException("Problem exporting the API classes.", e);
    }
  }

  @Override
  protected EnunciateConfiguration createEnunciateConfiguration() {
    EnunciateConfiguration configuration = new EnunciateConfiguration(Arrays.asList((DeploymentModule) new ExportListDeploymentModule()));
    configuration.setExcludeUnreferencedClasses(false);
    return configuration;
  }

  protected class ExportListDeploymentModule extends BasicDeploymentModule {

    @Override
    public String getName() {
      return "maven-export";
    }

    @Override
    protected void doGenerate() throws EnunciateException, IOException {
      EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();

      Set<String> exportedClasses = new HashSet<String>();
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          exportedClasses.add(typeDefinition.getQualifiedName());
        }

        for (RootElementDeclaration rootElementDeclaration : schemaInfo.getGlobalElements()) {
          exportedClasses.add(rootElementDeclaration.getQualifiedName());
        }
      }

      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          exportedClasses.add(ei.getQualifiedName());

          for (EndpointImplementation implementation : ei.getEndpointImplementations()) {
            exportedClasses.add(implementation.getQualifiedName());
          }
        }
      }

      for (RESTEndpoint endpoint : model.getRESTEndpoints()) {
        exportedClasses.add(endpoint.getQualifiedName());
      }

      File apiExportsFile = new File(classesDirectory, "META-INF/enunciate/api-exports");
      apiExportsFile.getParentFile().mkdirs();
      PrintWriter out = new PrintWriter(new FileWriter(apiExportsFile));
      for (String exportedClass : exportedClasses) {
        out.println(exportedClass);
      }
      out.flush();
      out.close();
    }
  }
}
