package ch.elexis.core.text;

import org.apache.commons.lang3.StringUtils;

/**
 * Marks a placeholder replacement value as rich text (HTML markup).
 *
 * Producers of rich text (e.g. the structured diagnosis data accessor) wrap
 * their HTML with {@link #wrap(String)}. Text plugins that support rich text
 * (currently the docx plugin) detect the marker and render the contained
 * markup; all unmarked values are always inserted as plain text. This way the
 * standard text output behavior can never change for ordinary field values,
 * no matter what characters they contain.
 */
public final class RichTextMarker {

	public static final String START = "<richtext>"; //$NON-NLS-1$
	public static final String END = "</richtext>"; //$NON-NLS-1$

	private RichTextMarker() {
		// no instances
	}

	public static String wrap(String html) {
		return START + html + END;
	}

	public static boolean isMarked(String text) {
		return text != null && text.contains(START);
	}

	public static String unwrap(String text) {
		if (text == null) {
			return null;
		}
		return text.replace(START, StringUtils.EMPTY).replace(END, StringUtils.EMPTY); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
