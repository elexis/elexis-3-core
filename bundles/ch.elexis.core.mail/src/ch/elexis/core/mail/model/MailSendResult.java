package ch.elexis.core.mail.model;

/**
 * Outcome of sending a message.
 *
 * @param state
 *            {@link MailSendState#SENT}, {@link MailSendState#UNCERTAIN} or {@link MailSendState#FAILED}
 * @param error
 *            the reason, <code>null</code> if sent
 * @param detail
 *            a detail the user may see, for example the rejected address, <code>null</code> if none
 * @param draft
 *            the draft after the attempt; after a failure it is stored in the drafts folder
 * @param filing the filing outcome, null when none was requested or sent
 */
public record MailSendResult(MailSendState state, MailSendError error, String detail, MailDraft draft,
		MailFilingResult filing) {

	/**
	 * Preserves the constructor without optional filing or consultation intent.
	 * @param state the state value
	 * @param error the error value
	 * @param detail the detail value
	 * @param draft the draft value
	 */
	public MailSendResult(MailSendState state, MailSendError error, String detail, MailDraft draft) {
		this(state, error, detail, draft, null);
	}
}
