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

import com.webcohesion.enunciate.EnunciateContext;

/**
 * @author Ryan Heaton
 */
public class OneTimeLogMessage {

  public static final OneTimeLogMessage SOURCE_FILES_NOT_FOUND = new OneTimeLogMessage(Level.WARN, "source-files-not-found",
                                                                                       "Some source files were not found for the Java classes that define the Web service API.",
                                                                                       "Enunciate will be unable to find the documentation for the classes that are missing source files. (For details, rebuild with debug-level logging.)",
                                                                                       "In order for Enunciate to find the source files for API classes that are not being compiled in the current project, the source file must be on the Enunciate sourcepath and the class must be explicitly included in the Enunciate configuration.",
                                                                                       "For more information, see https://github.com/stoicflame/enunciate/wiki/Discovering-Source-Files");
  public static final OneTimeLogMessage JACKSON_1_DEPRECATED = new OneTimeLogMessage(Level.WARN, "jackson1-deprecated", "Enunciate support for Jackson 1 is deprecated. It is recommended that you update to Jackson 2.");

  private final String[] message;
  private final String name;
  private final Level level;
  private int logged = 0;

  enum Level {
    INFO,
    WARN
  }

  private OneTimeLogMessage(Level level, String name, String... message) {
    this.level = level;
    this.name = name;
    this.message = message;
  }

  public synchronized void log(EnunciateContext context) {
    if (!isLogged()) {
      if (!context.getConfiguration().getDisabledWarnings().contains(this.name)) {
        for (String message : this.message) {
          switch (this.level) {
            case INFO:
              context.getLogger().info(message);
              break;
            case WARN:
              context.getLogger().warn(message);
              break;
          }
        }
      }
    }

    this.logged++;
  }

  public boolean isLogged() {
    return logged > 0;
  }

  public int getLogged() {
    return logged;
  }

}
