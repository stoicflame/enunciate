package com.webcohesion.enunciate.javac.decorations;

/**
 * @author Ryan Heaton
 */
public class SourcePosition implements Comparable<SourcePosition> {

  private final long position;
  private final long line;
  private final long column;

  public SourcePosition(long position, long line, long column) {
    this.position = position;
    this.line = line;
    this.column = column;
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
