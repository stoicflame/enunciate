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
package com.webcohesion.enunciate.javac.decorations;

import com.sun.source.util.TreePath;

import javax.tools.JavaFileObject;

/**
 * @author Ryan Heaton
 */
public class SourcePosition implements Comparable<SourcePosition> {

  private final TreePath path;
  private final JavaFileObject sourceFile;
  private final long position;
  private final long line;
  private final long column;

  public SourcePosition(TreePath path, JavaFileObject sourceFile, long position, long line, long column) {
    this.path = path;
    this.sourceFile = sourceFile;
    this.position = position;
    this.line = line;
    this.column = column;
  }

  public TreePath getPath() {
    return path;
  }

  public JavaFileObject getSourceFile() {
    return sourceFile;
  }

  public long getPosition() {
    return position;
  }

  public long getLine() {
    return line;
  }

  public long getColumn() {
    return column;
  }

  @Override
  public int compareTo(SourcePosition o) {
    return new Long(this.position).compareTo(o.position);
  }
}
