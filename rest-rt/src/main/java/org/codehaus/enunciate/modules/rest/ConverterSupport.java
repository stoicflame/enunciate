package org.codehaus.enunciate.modules.rest;

/**
 * Used to convert a String to a specific type.
 * 
 * @author holmes
 */
public interface ConverterSupport {

	/**
	 * Convert a string to a specified type.
	 * 
	 * @param value
	 *            The value.
	 * @param type
	 *            The type.
	 * @return The conversion.
	 */
	public Object convert(String value, Class type);
}
