/*
 * © 2024 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.util;

import java.util.function.Supplier;

public class Memoized<R> implements Supplier<R> {

  private final Supplier<R> supplier;
  private R value;

  public Memoized(Supplier<R> supplier) {
    this.supplier = supplier;
  }

  @Override
  public R get() {
    if (this.value == null) {
      this.value = this.supplier.get();
    }
    return this.value;
  }
}
