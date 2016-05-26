package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.EnunciateContext;

/**
 * @author Ryan Heaton
 */
public class OneTimeLogMessage {

  public static final OneTimeLogMessage SOURCE_FILES_NOT_FOUND = new OneTimeLogMessage(Level.WARN,
                          "Some source files were not found for the Java classes that define the Web service API.",
                          "Enunciate will be unable to find the documentation for the classes that are missing source files. (For details, rebuild with debug-level logging.)",
                          "In order for Enunciate to find the source files for API classes that are not being compiled in the current project, the source file must be on the Enunciate sourcepath and the class must be explicitly included in the Enunciate configuration.",
                          "For more information, see https://github.com/stoicflame/enunciate/wiki/Discovering-Source-Files");

  private final String[] message;
  private final Level level;
  private boolean logged = false;

  enum Level {
    INFO,
    WARN
  }

  private OneTimeLogMessage(Level level, String... message) {
    this.level = level;
    this.message = message;
  }

  public synchronized void log(EnunciateContext context) {
    if (!this.logged) {
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
      this.logged = true;
    }
  }

}
