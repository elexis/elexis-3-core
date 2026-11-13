package ch.elexis.core.mail.model;

import java.util.List;

/**
 * One page of message headers of a folder, newest first.
 *
 * @param headers
 *            the headers of this page
 * @param pageIndex
 *            the zero based page number
 * @param totalCount
 *            the number of messages in the folder
 * @param provisional
 *            whether sequence order is used until the date snapshot is ready
 * @param uidValidity
 *            the UIDVALIDITY of the folder when the page was loaded
 */
public record MailPage(List<MailHeader> headers, int pageIndex, int totalCount, long uidValidity, boolean provisional) {

	/**
	 * Creates an exact page, retaining the existing constructor.
	 * @param headers the page headers
	 * @param pageIndex the zero based page index
	 * @param totalCount the message count
	 * @param uidValidity the folder generation
	 */
	public MailPage(List<MailHeader> headers, int pageIndex, int totalCount, long uidValidity) {
		this(headers, pageIndex, totalCount, uidValidity, false);
	}
}
