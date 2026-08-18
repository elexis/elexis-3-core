package ch.elexis.core.mediorder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import ch.elexis.core.model.IBlob;

public final class MediorderBlobId {

	public static final String PREFIX = "MEDIORDER_"; //$NON-NLS-1$
	public static final String UNASSIGNED_PREFIX = "MEDIORDER_UNDEFINDED_"; //$NON-NLS-1$

	private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"); //$NON-NLS-1$

	private MediorderBlobId() {
	}

	public static String create(String patientId, LocalDateTime receivedAt) {
		return idPrefix(patientId) + "_" + TIMESTAMP.format(receivedAt); //$NON-NLS-1$
	}

	public static String idPrefix(String patientId) {
		return PREFIX + patientId;
	}

	public static boolean belongsTo(String blobId, String patientId) {
		if (blobId == null || patientId == null) {
			return false;
		}
		return blobId.equals(idPrefix(patientId)) || blobId.startsWith(idPrefix(patientId) + "_"); //$NON-NLS-1$
	}

	public static boolean hasTimestamp(String blobId) {
		return parseTimestamp(blobId).isPresent();
	}

	public static Optional<LocalDateTime> parseTimestamp(String blobId) {
		if (blobId == null) {
			return Optional.empty();
		}
		int separator = blobId.lastIndexOf('_');
		if (separator < 0) {
			return Optional.empty();
		}
		try {
			return Optional.of(LocalDateTime.parse(blobId.substring(separator + 1), TIMESTAMP));
		} catch (DateTimeParseException e) {
			return Optional.empty();
		}
	}

	public static Optional<LocalDateTime> resolveTimestamp(IBlob blob) {
		if (blob == null) {
			return Optional.empty();
		}
		return parseTimestamp(blob.getId()).or(() -> Optional.ofNullable(blob.getDate()).map(LocalDate::atStartOfDay));
	}
}
