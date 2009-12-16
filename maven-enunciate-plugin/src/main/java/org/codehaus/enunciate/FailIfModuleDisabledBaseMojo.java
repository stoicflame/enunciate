package org.codehaus.enunciate;

/**
 * Simple relocation of the {@link FailIfModuleDisabledMojo}.
 *
 * @author Ryan Heaton
 * @extendsPlugin maven-enunciate-slim-plugin
 * @goal failIfModuleDisabled
 * @requiresDependencyResolution runtime
 * @executionStrategy once-per-session
 */
public class FailIfModuleDisabledBaseMojo extends FailIfModuleDisabledMojo {

}