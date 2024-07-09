package ch.elexis.core.mail.ui.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;

public class ArchiveUtil {

	public static String PREF_MAIL_ARCHIVE_ENABLED = "mail/archive/enabled";
	public static String PREF_MAIL_ARCHIVE_DOCUMENT_CATEGORY = "mail/archive/document/category";

	public static String DEFAULT_CATEGORY = "Email Anhang Archiv";

	/**
	 * Archive attaments to omnivore of active patient.
	 * 
	 * @param attachments
	 */
	public static void archiveAttachments(List<File> attachments) {
		if (ConfigServiceHolder.get().get(PREF_MAIL_ARCHIVE_ENABLED, false)) {
			if (attachments != null && !attachments.isEmpty()) {
				if (OmnivoreDocumentStoreServiceHolder.isAvailable()) {
					String prefix = getPrefix();
					Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();
					if (patient.isPresent()) {
						attachments.forEach(f -> archiveFile(prefix, f, patient.get()));
					} else {
						LoggerFactory.getLogger(ArchiveUtil.class).warn("No active patient");
					}
				} else {
					LoggerFactory.getLogger(ArchiveUtil.class).warn("No omnivore document service available");
				}
			}
		}
	}

	private static String getPrefix() {
		return "Mailversand " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
	}

	private static void archiveFile(String namePrefix, File file, IPatient iPatient) {
		IDocumentStore omnivoreStore = OmnivoreDocumentStoreServiceHolder.get();

		ICategory category = getArchiveCategory(omnivoreStore);

		IDocument archiveDocument = omnivoreStore.createDocument(iPatient.getId(), namePrefix + " - " + file.getName(),
				category.getName());
		archiveDocument.setCreated(new Date());
		archiveDocument.setMimeType(FilenameUtils.getExtension(file.getName()));
		try (FileInputStream inStream = new FileInputStream(file)) {
			omnivoreStore.saveDocument(archiveDocument, inStream);
		} catch (IOException | ElexisException e) {
			LoggerFactory.getLogger(ArchiveUtil.class).error("Exception archiving attachment", e);
		}
	}

	private static ICategory getArchiveCategory(IDocumentStore omnivoreStore) {
		String categoryName = ConfigServiceHolder.get().get(PREF_MAIL_ARCHIVE_DOCUMENT_CATEGORY, DEFAULT_CATEGORY);
		Optional<ICategory> existing = omnivoreStore.getCategoryByName(categoryName);
		if (existing.isEmpty()) {
			return omnivoreStore.createCategory(DEFAULT_CATEGORY);
		}
		return existing.get();
	}
}
