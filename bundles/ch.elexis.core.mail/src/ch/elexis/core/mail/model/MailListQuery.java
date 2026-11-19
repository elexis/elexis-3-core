package ch.elexis.core.mail.model;

/**
 * Selects and orders a paged server-side message list.
 * @param text optional text, blank means no search; otherwise trimmed length two to 200 characters
 * @param order the non-null received-date order
 * @param filter the non-null server flag filter
 */
public record MailListQuery(String text, MailSortOrder order, MailListFilter filter) {

	/**
	 * Validates the required order and filter and normalizes the optional text.
	 * @param text the optional search text
	 * @param order the received-date order
	 * @param filter the flag filter
	 * @throws IllegalArgumentException if order, filter or nonblank text is invalid
	 */
	public MailListQuery {
		if (order == null || filter == null) {
			throw new IllegalArgumentException("Mail order and filter are required");
		}
		text = text == null || text.isBlank() ? null : text.trim();
		if (text != null && (text.length() < 2 || text.length() > 200)) {
			throw new IllegalArgumentException("Invalid mail search text");
		}
	}
}
