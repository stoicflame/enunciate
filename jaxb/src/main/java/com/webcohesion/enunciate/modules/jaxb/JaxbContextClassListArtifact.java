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
