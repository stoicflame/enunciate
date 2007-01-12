package net.sf.enunciate.template.strategies;

import net.sf.jelly.apt.strategies.FileStrategy;
import net.sf.jelly.apt.strategies.MissingParameterException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A file strategy that takes into account a base directory.
 *
 * @author Ryan Heaton
 */
public class EnunciateFileStrategy extends FileStrategy {

  private final File outputDirectory;

  public EnunciateFileStrategy(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Override
  public PrintWriter getWriter() throws IOException, MissingParameterException {
    File dir = this.outputDirectory;
    if (dir == null) {
      //no output directory is specified,
      return super.getWriter();
    }
    else {
      String pckg = getPackage();
      if ((pckg != null) && (pckg.trim().length() > 0)) {
        String[] dirnames = pckg.split("\\.");
        for (String dirname : dirnames) {
          dir = new File(dir, dirname);
        }
      }
      dir.mkdirs();

      String charset = getCharset();
      if (charset != null) {
        return new PrintWriter(new File(dir, getName()), charset);
      }
      else {
        return new PrintWriter(new File(dir, getName()));
      }
    }
  }
}
