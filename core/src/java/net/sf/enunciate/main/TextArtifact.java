package net.sf.enunciate.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * An artifact that is some text.
 *
 * @author Ryan Heaton
 */
public class TextArtifact extends BaseArtifact {

  private final String text;

  public TextArtifact(String module, String id, String text) {
    super(module, id);
    this.text = text;
  }

  /**
   * The text for this artifact.
   *
   * @return The text for this artifact.
   */
  public String getText() {
    return text;
  }

  /**
   * Exports its text to the specified file.
   *
   * @param file The file to export the text to.
   */
  public void exportTo(File file, Enunciate enunciate) throws IOException {
    if (file.exists() && file.isDirectory()) {
      file = new File(file, getId());
    }
    
    file.getParentFile().mkdirs();
    PrintWriter writer = new PrintWriter(file);
    writer.print(this.text);
    writer.flush();
    writer.close();
  }
}
