package ch.elexis.core.mail.model;

import java.time.Instant;

/**
 * Last observed mailbox storage quota, independent of connection state.
 * @param status availability of the quota
 * @param usedKb used storage in KB, or -1 when unknown
 * @param limitKb storage limit in KB, or -1 when unknown
 * @param checkedAt observation time, or null before the first observation
 */
public record MailQuota(MailQuotaStatus status, long usedKb, long limitKb, Instant checkedAt) {
}
