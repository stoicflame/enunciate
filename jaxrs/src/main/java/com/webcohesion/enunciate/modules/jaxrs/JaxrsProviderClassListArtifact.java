package com.webcohesion.enunciate.modules.jaxrs;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.artifacts.BaseArtifact;

import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class JaxrsProviderClassListArtifact extends BaseArtifact {

  private final EnunciateJaxrsContext jaxrsContext;
  private final Date created = new Date();

  public JaxrsProviderClassListArtifact(EnunciateJaxrsContext jaxrsContext) {
    super(JaxrsModule.NAME, "jaxrs-provider-classes.list");
    this.jaxrsContext = jaxrsContext;
    setBelongsOnServerSideClasspath(true);
  }

  @Override
  public String getName() {
    return "jaxrs-provider-classes.list";
  }

  @Override
  public String getDescription() {
    return "A plain text file that contains the list of all JAX-RS providers in the current project.";
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

    Set<String> written = new HashSet<String>();
    for (TypeElement provider : this.jaxrsContext.getProviders()) {
      if (written.add(provider.getQualifiedName().toString())) {
        out.write(provider.getQualifiedName() + "\n");
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
