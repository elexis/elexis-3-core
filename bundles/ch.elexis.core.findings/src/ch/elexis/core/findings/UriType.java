package ch.elexis.core.findings;

import org.apache.commons.lang3.StringUtils;

/**
 * Definition of URIs to reference other resources.
 *
 * @author thomas
 *
 */
public enum UriType {
	HTTP("http://"), DB(StringUtils.EMPTY);

	private final String prefix;

	private UriType(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public static UriType parseType(String uri) {
		if (uri != null) {
			if (uri.contains("::")) {
				return DB;
			} else if (uri.startsWith(HTTP.getPrefix())) {
				return HTTP;
			}
		}
		return null;
	}

	public String toString(String extension) {
		return prefix + extension;
	}
}
