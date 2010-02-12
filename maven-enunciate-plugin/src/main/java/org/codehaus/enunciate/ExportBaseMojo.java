package org.codehaus.enunciate;

/**
 * Simple relocation of the {@link ExportMojo}.
 *
 * @goal export
 * @phase process-sources
 * @requiresDependencyResolution test
 * @extendsPlugin maven-enunciate-slim-plugin
 *
 * @author Ryan Heaton
 * @link http://docs.codehaus.org/display/ENUNCIATE/Working+With+Precompiled+Classes
 */
public class ExportBaseMojo extends ExportMojo {

}