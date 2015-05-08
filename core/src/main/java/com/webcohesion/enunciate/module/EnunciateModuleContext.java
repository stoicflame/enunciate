package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.EnunciateContext;

/**
 * A context for a specific module.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciateModuleContext {

  protected final EnunciateContext context;

  public EnunciateModuleContext(EnunciateContext context) {
    this.context = context;
  }

  protected void debug(String message, Object... formatArgs) {
    this.context.getLogger().debug(message, formatArgs);
  }

  protected void info(String message, Object... formatArgs) {
    this.context.getLogger().info(message, formatArgs);
  }

  protected void warn(String message, Object... formatArgs) {
    this.context.getLogger().warn(message, formatArgs);
  }
}
