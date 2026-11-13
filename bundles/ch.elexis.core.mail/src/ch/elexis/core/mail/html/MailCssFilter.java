package ch.elexis.core.mail.html;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Set;

/**
 * Filters inline CSS using a declaration tokenizer and an explicit property allow list.
 */
public final class MailCssFilter {

	private static final Set<String> PROPERTIES = Set.of("color", "background-color", "font", "font-family",
			"font-size", "font-weight", "font-style", "text-align", "text-decoration", "text-indent", "line-height",
			"letter-spacing", "vertical-align", "white-space", "width", "height", "max-width", "min-width",
			"display", "list-style-type");

	private static final Set<String> DISPLAY_VALUES = Set.of("block", "inline", "inline-block", "table",
			"table-row", "table-cell", "table-caption", "table-row-group", "table-header-group", "table-footer-group",
			"table-column", "table-column-group", "none");

	/**
	 * Filters a style attribute without repairing malformed declarations.
	 *
	 * @param style the non-null style attribute value
	 * @return allowed declarations separated by semicolons, or an empty string
	 */
	public String filter(String style) {
		StringBuilder result = new StringBuilder();
		Deque<Character> brackets = new ArrayDeque<>();
		char quote = 0;
		int start = 0;
		int colon = -1;
		boolean malformed = false;
		for (int i = 0; i <= style.length(); i++) {
			char c = i == style.length() ? ';' : style.charAt(i);
			if (quote != 0) {
				if (c == quote) {
					quote = 0;
				}
			} else if (c == '\'' || c == '"') {
				quote = c;
			} else if (c == '(' || c == '[' || c == '{') {
				brackets.push(c);
			} else if (c == ')' || c == ']' || c == '}') {
				if (brackets.isEmpty() || !matches(brackets.pop(), c)) {
					malformed = true;
				}
			} else if (brackets.isEmpty()) {
				if (c == ':' && colon < 0) {
					colon = i;
				} else if (c == ';') {
					if (!malformed && colon >= start) {
						append(result, style.substring(start, colon), style.substring(colon + 1, i));
					}
					start = i + 1;
					colon = -1;
					malformed = false;
				}
			}
		}
		return result.toString();
	}

	private boolean matches(char open, char close) {
		return open == '(' && close == ')' || open == '[' && close == ']' || open == '{' && close == '}';
	}

	private void append(StringBuilder result, String property, String value) {
		property = property.trim().toLowerCase(Locale.ROOT);
		value = value.trim();
		if (!(PROPERTIES.contains(property) || property.startsWith("margin") || property.startsWith("padding")
				|| property.startsWith("border")) || value.isEmpty() || value.length() > 200) {
			return;
		}
		String lower = value.toLowerCase(Locale.ROOT);
		StringBuilder compact = new StringBuilder();
		for (int i = 0; i < lower.length(); i++) {
			if (!Character.isWhitespace(lower.charAt(i))) {
				compact.append(lower.charAt(i));
			}
		}
		String tokens = compact.toString();
		if (tokens.contains("url(") || tokens.contains("expression") || tokens.contains("image-set")
				|| tokens.contains("var(") || lower.contains("@") || lower.contains("\\") || lower.contains("/*")
				|| property.equals("display") && !DISPLAY_VALUES.contains(lower)) {
			return;
		}
		if (!result.isEmpty()) {
			result.append(';');
		}
		result.append(property).append(':').append(value);
	}
}
