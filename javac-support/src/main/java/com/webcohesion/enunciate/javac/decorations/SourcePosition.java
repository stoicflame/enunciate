package com.webcohesion.enunciate.javac.decorations;

import com.sun.source.util.TreePath;

/**
 * @author Ryan Heaton
 */
public class SourcePosition implements Comparable<SourcePosition> {

  private final TreePath path;
  private final long position;
  private final long line;
  private final long column;

  public SourcePosition(TreePath path, long position, long line, long column) {
    this.path = path;
    this.position = position;
    this.line = line;
    this.column = column;
  }

  public TreePath getPath() {
    return path;
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
