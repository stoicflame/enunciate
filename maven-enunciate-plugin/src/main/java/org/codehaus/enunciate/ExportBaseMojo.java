package org.codehaus.enunciate;

/**
 * Simple relocation of the {@link ExportMojo}.
 *
 * @goal export
 * @extendsPlugin maven-enunciate-slim-plugin
 * @phase process-sources
 * @requiresDependencyResolution runtime
 * @executionStrategy once-per-session
 *
 * @author Ryan Heaton
 * @link http://docs.codehaus.org/display/ENUNCIATE/Working+With+Precompiled+Classes
 */
public class ExportBaseMojo extends ExportMojo {

}