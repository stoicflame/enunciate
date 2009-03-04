package org.codehaus.enunciate.modules.rest;

import org.apache.commons.beanutils.ConvertUtils;

/**
 * Uses Apache's BeanUtils {@link ConvertUtils} to convert Strings to various
 * different types.
 * 
 * If that doesn't work, it attempts to map Enums. When that fails,
 * {@link UnsupportedOperationException}
 * 
 * @author holmes
 */
public class DefaultConverter implements ConverterSupport {

	/**
	 * Convert a string to a specified type.
	 * 
	 * @param value
	 *            The value.
	 * @param type
	 *            The type.
	 * @return The conversion.
	 */
	public Object convert(final String value, final Class type) {
		if (ConvertUtils.lookup(type) != null) {
			return ConvertUtils.convert(value, type);
		} else if (Enum.class.isAssignableFrom(type)) {
			return Enum.valueOf(type, value);
		} else {
			throw new UnsupportedOperationException("A converter was not found for " + type.getName());
		}
	}
}
