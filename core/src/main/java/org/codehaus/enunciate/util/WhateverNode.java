package org.codehaus.enunciate.util;

/**
 * This singleton value class is used to contain "..." as a value (for documentation purposes).
 */
public final class WhateverNode extends RawValueNode {

  public final static WhateverNode instance = new WhateverNode();

  private WhateverNode() {
    super("...");
  }

  public static WhateverNode getInstance() {
    return instance;
  }

}