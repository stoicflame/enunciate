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
