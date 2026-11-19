package ch.elexis.core.mail.model;

import java.util.List;

/**
 * Immutable result of a filing job in request order.
 * @param messageKey the server-derived business identity
 * @param patientId the explicitly selected patient
 * @param items the per-item outcomes
 * @param note the note outcome, null when none was attempted
 */
public record MailFilingResult(String messageKey, String patientId, List<MailFiledItem> items, MailNoteStatus note) {

	/**
	 * Copies the results to preserve their order and immutability.
	 * @param messageKey the server-derived business identity
	 * @param patientId the explicitly selected patient
	 * @param items the ordered outcomes
	 * @param note the note outcome, null when none was attempted
	 * @throws NullPointerException if the results or an element are null
	 */
	public MailFilingResult {
		items = List.copyOf(items);
	}

	/**
	 * Preserves the constructor without optional filing or consultation intent.
	 * @param messageKey the messageKey value
	 * @param patientId the patientId value
	 * @param items the items value
	 */
	public MailFilingResult(String messageKey, String patientId, List<MailFiledItem> items) {
		this(messageKey, patientId, items, null);
	}
}
