package ch.elexis.core.mediorder;

import java.util.Optional;

public record MediorderHistoryRef(String patientId, String blobId) {

	private static final String SEPARATOR = "|"; //$NON-NLS-1$

	public static String encode(String patientId, String blobId) {
		if (patientId == null || patientId.isBlank()) {
			return null;
		}
		return blobId == null || blobId.isBlank() ? patientId : patientId + SEPARATOR + blobId;
	}

	public static Optional<MediorderHistoryRef> decode(String id) {
		if (id == null || id.isBlank()) {
			return Optional.empty();
		}
		int separator = id.indexOf(SEPARATOR);
		if (separator < 0) {
			return Optional.of(new MediorderHistoryRef(id, null));
		}
		String patientId = id.substring(0, separator);
		if (patientId.isBlank()) {
			return Optional.empty();
		}
		String blobId = id.substring(separator + SEPARATOR.length());
		return Optional.of(new MediorderHistoryRef(patientId, blobId.isBlank() ? null : blobId));
	}
}
