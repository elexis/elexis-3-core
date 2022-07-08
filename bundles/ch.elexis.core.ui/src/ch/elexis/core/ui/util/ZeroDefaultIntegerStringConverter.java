package ch.elexis.core.ui.util;

import org.eclipse.core.databinding.conversion.Converter;
import org.slf4j.LoggerFactory;

/**
 * Safely convert a string to an int value. null or empty values are handled as
 * value 0.
 *
 * @since 3.8
 */
public class ZeroDefaultIntegerStringConverter extends Converter<String, Integer> {

	public ZeroDefaultIntegerStringConverter() {
		super(String.class, Integer.class);
	}

	@Override
	public Integer convert(String fromObject) {
		if (fromObject != null && fromObject.length() > 0) {
			try {
				int value = Integer.valueOf(fromObject);
				return value;
			} catch (NumberFormatException nfe) {
				LoggerFactory.getLogger(getClass()).warn("Conversion error, returning 0", nfe); //$NON-NLS-1$
			}
		}
		return 0;
	}

}
