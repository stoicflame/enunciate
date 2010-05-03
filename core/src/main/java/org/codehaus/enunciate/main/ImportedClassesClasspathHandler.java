package org.codehaus.enunciate.main;

import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.config.APIImport;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.util.AntPatternMatcher;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
* @author Ryan Heaton
*/
public class ImportedClassesClasspathHandler implements ClasspathHandler {

  private final Enunciate enunciate;
  private final Map<String, File> classesToSources = new HashMap<String, File>();
  private final File tempSourcesDir;

  private File currentEntry;
  private Map<String, File> currentEntryClassesToSources;
  private boolean classesImportedFromCurrentEntry;

  public ImportedClassesClasspathHandler(Enunciate enunciate) throws IOException {
    this.enunciate = enunciate;
    this.tempSourcesDir = enunciate.createTempDir();
  }

  public Map<String, File> getClassesToSources() {
    return classesToSources;
  }

  public void startPathEntry(File pathEntry) {
    this.currentEntry = pathEntry;
    this.currentEntryClassesToSources = new HashMap<String, File>();
    this.classesImportedFromCurrentEntry = false;
  }

  public void handleResource(ClasspathResource resource) {
    String path = resource.getPath();
    if (path.endsWith(".class")) {
      String classname = path.substring(0, path.length() - 6).replace('/', '.').replace('$', '.');
      if (!classname.endsWith(".package-info")) {
        enunciate.debug("Noticed class %s in %s.", classname, currentEntry);
        currentEntryClassesToSources.put(classname, currentEntryClassesToSources.get(classname));
      }
    }
    else if (path.endsWith(".java")) {
      String classname = path.substring(0, path.length() - 5).replace('/', '.');
      if (!classname.endsWith(".package-info")) {
        File sourcesFile = new File(tempSourcesDir, path);
        enunciate.debug("Noticed the source for class %s in %s, extracting to %s.", classname, currentEntry, sourcesFile);
        try {
          InputStream in = resource.read();
          sourcesFile.getParentFile().mkdirs();
          FileOutputStream out = new FileOutputStream(sourcesFile);
          byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
          int len;
          while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
          }
          out.flush();
          out.close();
          in.close();
          currentEntryClassesToSources.put(classname, sourcesFile);
        }
        catch (IOException e) {
          enunciate.warn("Unable to extract source file %s (%s).", sourcesFile, e.getMessage());
        }
      }
    }
    else if ("META-INF/enunciate/api-exports".equals(path)) {
      enunciate.debug("Importing classes listed in META-INF/enunciate/api-exports in %s.", currentEntry);
      try {
        InputStream in = resource.read();
        Set<String> autoImports = readAutoImports(in);
        in.close();
        for (String autoImport : autoImports) {
          classesImportedFromCurrentEntry = classesImportedFromCurrentEntry || !classesToSources.containsKey(autoImport);
          classesToSources.put(autoImport, classesToSources.get(autoImport));
        }
      }
      catch (IOException e) {
        enunciate.warn("Unable to read export list found in %s: %s.", currentEntry, e.getMessage());
      }
    }
  }

  public boolean endPathEntry(File pathEntry) {
    if (this.enunciate.getConfig() != null) {
      Set<String> classesFound = this.currentEntryClassesToSources.keySet();
      for (DeploymentModule deploymentModule : this.enunciate.getConfig().getEnabledModules()) {
        if (deploymentModule instanceof EnunciateClasspathListener) {
          ((EnunciateClasspathListener)deploymentModule).onClassesFound(classesFound);
        }
      }
    }

    this.classesImportedFromCurrentEntry = copyImportedClasses(this.currentEntryClassesToSources, this.classesToSources) || this.classesImportedFromCurrentEntry;

    this.currentEntry = null;
    this.currentEntryClassesToSources = null;
    return this.classesImportedFromCurrentEntry;
  }

  /**
   * Read the set of auto-imports from the input stream.
   *
   * @param in The input stream.
   * @return The auto imports.
   */
  private Set<String> readAutoImports(InputStream in) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    Set<String> autoImports = new TreeSet<String>();
    String classname = reader.readLine();
    while (classname != null) {
      autoImports.add(classname);
      classname = reader.readLine();
    }
    return autoImports;
  }

  /**
   * Copy the relevant found classes that are imported to the specified map.
   *
   * @param foundClasses2Sources the found classes.
   * @param classes2sources      the target map.
   * @return whether any of the found classes were imported.
   */
  protected boolean copyImportedClasses(Map<String, File> foundClasses2Sources, Map<String, File> classes2sources) {
    boolean imported = false;
    for (Map.Entry<String, File> foundEntry : foundClasses2Sources.entrySet()) {
      if (foundEntry.getKey().endsWith(".package-info")) {
        File sourceFile = foundEntry.getValue();
        if (sourceFile != null) {
          //APT has a bug where it won't find the package-info file unless it's on the source path.
          imported |= !classes2sources.containsKey(foundEntry.getKey());
          classes2sources.put(foundEntry.getKey(), sourceFile);
        }
      }
      else if (this.enunciate.getConfig() != null && this.enunciate.getConfig().getAPIImports() != null && !this.enunciate.getConfig().getAPIImports().isEmpty()) {
        AntPatternMatcher matcher = new AntPatternMatcher();
        matcher.setPathSeparator(".");
        for (APIImport apiImport : this.enunciate.getConfig().getAPIImports()) {
          String pattern = apiImport.getPattern();
          if (pattern != null) {
            if (!classes2sources.containsKey(foundEntry.getKey())) {
              if (pattern.equals(foundEntry.getKey())) {
                this.enunciate.debug("Class %s will be imported because it was explicitly listed.", foundEntry.getKey());
                imported |= !classes2sources.containsKey(foundEntry.getKey());
                classes2sources.put(foundEntry.getKey(), apiImport.isSeekSource() ? foundEntry.getValue() : null);
              }
              else if (matcher.isPattern(pattern) && matcher.match(pattern, foundEntry.getKey())) {
                this.enunciate.debug("Class %s will be imported because it matches pattern %s.", foundEntry.getKey(), pattern);
                imported |= !classes2sources.containsKey(foundEntry.getKey());
                classes2sources.put(foundEntry.getKey(), apiImport.isSeekSource() ? foundEntry.getValue() : null);
              }
            }
            else if (foundEntry.getValue() != null) {
              imported |= !classes2sources.containsKey(foundEntry.getKey());
              classes2sources.put(foundEntry.getKey(), foundEntry.getValue());
            }
          }
        }
      }
    }

    return imported;
  }
}
