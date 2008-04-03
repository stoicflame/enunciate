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

package org.codehaus.enunciate.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * An artifact that is some text.
 *
 * @author Ryan Heaton
 */
public class TextArtifact extends BaseArtifact {

  private boolean publicArtifact = true;
  private final String text;

  public TextArtifact(String module, String id, String text) {
    super(module, id);
    this.text = text;
  }

  /**
   * The text for this artifact.
   *
   * @return The text for this artifact.
   */
  public String getText() {
    return text;
  }

  /**
   * Exports its text to the specified file.
   *
   * @param file The file to export the text to.
   */
  public void exportTo(File file, Enunciate enunciate) throws IOException {
    if (file.exists() && file.isDirectory()) {
      file = new File(file, getId());
    }
    
    file.getParentFile().mkdirs();
    PrintWriter writer = new PrintWriter(file);
    writer.print(this.text);
    writer.flush();
    writer.close();
  }

  /**
   * The size of the bytes of the text, UTF-8.
   *
   * @return The size of the bytes of the text, UTF-8.
   */
  public long getSize() {
    try {
      return this.text.getBytes("utf-8").length;
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Whether this file artifact is public.
   *
   * @return Whether this file artifact is public.
   */
  public boolean isPublic() {
    return publicArtifact;
  }

  /**
   * Whether this file artifact is public.
   *
   * @param bundled Whether this file artifact is public.
   */
  public void setPublic(boolean bundled) {
    this.publicArtifact = bundled;
  }
}
