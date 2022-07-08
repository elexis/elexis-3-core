package ch.elexis.core.ui.util;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Safely convert an int to a String value. null or empty values are handled as
 * value "0".
 *
 * @since 3.8
 */
public class ZeroDefaultStringIntegerConverter extends Converter<Integer, String> {

	public ZeroDefaultStringIntegerConverter() {
		super(Integer.class, String.class);
	}

	@Override
	public String convert(Integer fromObject) {
		if (fromObject == null) {
			return "0"; //$NON-NLS-1$
		}
		return Integer.toString(fromObject);
	}

}
