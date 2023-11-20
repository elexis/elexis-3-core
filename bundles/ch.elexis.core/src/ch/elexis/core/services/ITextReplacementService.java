package ch.elexis.core.services;

import java.util.List;

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
}
