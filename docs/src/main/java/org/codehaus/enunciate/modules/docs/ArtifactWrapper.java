/*
 * Copyright 2006-2008 Web Cohesion
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

package org.codehaus.enunciate.modules.docs;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.main.Artifact;

/**
 * Wraps an artifact to provide display.
 *
 * @author Ryan Heaton
 */
public class ArtifactWrapper extends StringModel {

  private Artifact artifact;

  public ArtifactWrapper(Artifact artifact, BeansWrapper wrapper) {
    super(artifact, wrapper);
    this.artifact = artifact;
  }

  @Override
  public TemplateModel get(String property) throws TemplateModelException {
    if ("size".equals(property)) {
      long sizeInBytes = artifact.getSize();
      String units = "bytes";
      float unitSize = 1;

      if ((sizeInBytes / 1024) > 0) {
        units = "K";
        unitSize = 1024;
      }

      if ((sizeInBytes / 1048576) > 0) {
        units = "M";
        unitSize = 1048576;
      }

      if ((sizeInBytes / 1073741824) > 0) {
        units = "G";
        unitSize = 1073741824;
      }

      return wrap(String.format("%.2f%s", ((float) sizeInBytes) / unitSize, units));
    }

    return super.get(property);
  }
}
