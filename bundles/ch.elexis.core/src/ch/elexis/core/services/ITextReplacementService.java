package ch.elexis.core.services;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.text.ITextPlaceholderResolver;

/**
 * Perform replacement of text templates according to <a href=
 * "https://medelexis.ch/wp-content/uploads/Tabelle_Platzhalter.pdf">Elexis
 * Platzhalter</a>. Cannot use IDataAccess as this is available in
 * ch.elexis.core.data only.
 */
public interface ITextReplacementService {

	public static final String DONT_SHOW_REPLACEMENT_ERRORS = "*";
	public static final String MATCH_TEMPLATE = "\\[[" + DONT_SHOW_REPLACEMENT_ERRORS //$NON-NLS-1$
			+ "]?[-a-zA-ZäöüÄÖÜéàè_ ]+\\.[-a-zA-Z0-9äöüÄÖÜéàè_ ]+\\]";
	public static final String MATCH_GENDERIZE = "\\[[" + DONT_SHOW_REPLACEMENT_ERRORS + "]?[a-zA-Z]+:mwn?:[^\\[]+\\]"; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Get all currently available {@link ITextPlaceholderResolver} implemenations.
	 *
	 * @return
	 */
	public List<ITextPlaceholderResolver> getResolvers();

	/**
	 * @param context  to apply during replacement
	 * @param template the source text
	 * @return replaced result
	 */
	public default String performReplacement(IContext context, String template) {
		return performReplacement(context, template, System.lineSeparator());
	}

	/**
	 * @param context        to apply during replacement
	 * @param template       the source text
	 * @param newLinePattern the new line to used in the resulting string
	 * @return replaced result
	 */
	public String performReplacement(IContext context, String template, String newLinePattern);

	/**
	 * Get the type part of a placeholder.
	 * 
	 * @param placeholder
	 * @return
	 */
	public static String getPlaceholderType(String placeholder) {
		if (StringUtils.isNotBlank(placeholder)) {
			String ret = placeholder;
			if (ret.startsWith("[")) {
				ret = ret.substring(1);
			}
			if(ret.indexOf(".") > 1) {
				ret = ret.substring(0, (ret.indexOf(".")));
			}
			if (ret.indexOf(":") > 1) {
				ret = ret.substring(0, (ret.indexOf(":")));
			}
			return ret;
		}
		return null;
	}
}
