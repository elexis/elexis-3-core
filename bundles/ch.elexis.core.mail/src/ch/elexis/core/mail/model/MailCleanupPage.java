package ch.elexis.core.mail.model;

import java.util.List;

/** Bounded cleanup header fetch on the reader lane.
 * @param uidValidity selected source identity, headers are empty if it differs from the requested one
 * @param headers at most 50 fetched unflagged headers
 */
public record MailCleanupPage(long uidValidity, List<MailHeader> headers) {
}
