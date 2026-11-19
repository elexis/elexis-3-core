package ch.elexis.core.mail.model;

import java.util.List;

/** Result of the single cleanup search per folder.
 * @param uidValidity searched source identity
 * @param uids matching unflagged UIDs in ascending order
 */
public record MailCleanupCandidates(long uidValidity, List<Long> uids) {
}
