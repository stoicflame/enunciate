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
package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class StaticInterfaceDescriptionFile implements InterfaceDescriptionFile {

  private boolean written = false;
  private final File file;
  private final Enunciate enunciate;

  public StaticInterfaceDescriptionFile(File file, Enunciate enunciate) {
    this.file = file;
    this.enunciate = enunciate;
  }

  @Override
  public String getHref() {
    if (!written) {
      throw new IllegalStateException("No href available: file hasn't been written.");
    }

    return file.getName();
  }

  @Override
  public void writeTo(File directory) throws IOException {
    this.enunciate.copyFile(this.file, this.file.getParentFile(), directory);
    this.written = true;
  }
}
