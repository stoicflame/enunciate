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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortedList<T> extends ArrayList<T> {
  private Comparator<T> comparator;

  public SortedList(Comparator<T> comparator) {
    this.comparator = comparator;
  }

  @Override
  public boolean add(T t) {
    int index = Collections.binarySearch(this, t, comparator);
    if (index < 0) {
      index = -index - 1;
    }
    if (index >= size()) {
      super.add(t);
    }
    else {
      super.add(index, t);
    }
    return true;
  }
}

