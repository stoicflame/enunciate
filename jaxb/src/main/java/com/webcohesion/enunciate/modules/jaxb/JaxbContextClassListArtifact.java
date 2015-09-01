package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.artifacts.BaseArtifact;
import com.webcohesion.enunciate.modules.jaxb.model.Registry;
import com.webcohesion.enunciate.modules.jaxb.model.RootElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

/**
 * @author Ryan Heaton
 */
public class JaxbContextClassListArtifact extends BaseArtifact {

  private final EnunciateJaxbContext jaxbContext;
  private final Date created = new Date();

  public JaxbContextClassListArtifact(EnunciateJaxbContext jaxbContext) {
    super(JaxbModule.NAME, "jaxb-context-classes.list");
    this.jaxbContext = jaxbContext;
    setBelongsOnServerSideClasspath(true);
  }

  @Override
  public String getName() {
    return "jaxb-context-classes.list";
  }

  @Override
  public String getDescription() {
    return "A plain text files that contains the list of all root elements and registries that should be noticed by the jaxb context.";
  }

  @Override
  public boolean isPublic() {
    return false;
  }

  @Override
  public Date getCreated() {
    return this.created;
  }

  @Override
  public void exportTo(File fileOrDirectory, Enunciate enunciate) throws IOException {
    FileWriter out = new FileWriter(fileOrDirectory.isDirectory() ? new File(fileOrDirectory, getName()) : fileOrDirectory);

    for (SchemaInfo schemaInfo : this.jaxbContext.getSchemas().values()) {
      for (Registry registry : schemaInfo.getRegistries()) {
        out.write(registry.getQualifiedName() + "\n");
      }

      Collection<RootElementDeclaration> elements = schemaInfo.getRootElements();
      for (RootElementDeclaration element : elements) {
        out.write(element.getQualifiedName() + "\n");
      }
    }

    out.flush();
    out.close();
  }

  @Override
  public long getSize() {
    return -1;
  }
}
