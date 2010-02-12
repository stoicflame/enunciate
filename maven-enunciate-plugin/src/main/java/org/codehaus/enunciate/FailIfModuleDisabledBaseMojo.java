package org.codehaus.enunciate;

/**
 * Simple relocation of the {@link FailIfModuleDisabledMojo}.
 * 
 * @goal failIfModuleDisabled
 * @requiresDependencyResolution runtime
 * @extendsPlugin maven-enunciate-slim-plugin
 * 
 * @author Ryan Heaton
 */
public class FailIfModuleDisabledBaseMojo
    extends FailIfModuleDisabledMojo
{

}