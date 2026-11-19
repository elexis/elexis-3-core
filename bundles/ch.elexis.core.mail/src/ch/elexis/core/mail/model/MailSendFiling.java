package ch.elexis.core.mail.model;

/**
 * Immutable patient and consultation intent captured when sending.
 * 
 * @param patientId   the explicitly selected patient
 * @param encounterId the selected consultation, null without a note
 */
public record MailSendFiling(String patientId, String encounterId) {
}
