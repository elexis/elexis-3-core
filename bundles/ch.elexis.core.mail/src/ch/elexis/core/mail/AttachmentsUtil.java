package ch.elexis.core.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.internal.DocumentConverterServiceHolder;
import ch.elexis.core.mail.internal.DocumentStoreServiceHolder;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IDocumentConverter;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.utils.CoreUtil;

public class AttachmentsUtil {

	private static final Logger logger = LoggerFactory.getLogger(AttachmentsUtil.class);

	private static File attachmentsFolder;

	public static final String ATTACHMENT_DELIMITER = ":::";

	private synchronized static File getAttachmentsFolder() {
		if (attachmentsFolder == null) {
			File tmpDir = CoreUtil.getTempDir();
			attachmentsFolder = new File(tmpDir, "_att" + System.currentTimeMillis() + "_");
			attachmentsFolder.mkdir();
		}
		return attachmentsFolder;
	}

	private static Optional<File> getTempFile(IDocument iDocument) {
		String extension = iDocument.getExtension();
		Optional<IDocumentConverter> converterService = DocumentConverterServiceHolder.get();
		if (converterService.isPresent() && converterService.get().isAvailable() && extension != null
				&& !extension.toLowerCase().endsWith("pdf")) {
			Optional<File> converted = DocumentConverterServiceHolder.get().get().convertToPdf(iDocument);
			if (converted.isPresent()) {
				return converted;
			}
		}
		String fileName = getFileNameWithTimestamp(iDocument);
		File tmpFile = new File(getAttachmentsFolder(), fileName);
		while (tmpFile.exists()) {
			fileName = getFileNameWithTimestamp(iDocument, true);
			tmpFile = new File(getAttachmentsFolder(), fileName);
		}
		try (FileOutputStream fout = new FileOutputStream(tmpFile)) {
			Optional<InputStream> content = DocumentStoreServiceHolder.getService().loadContent(iDocument);
			if (content.isPresent()) {
				IOUtils.copy(content.get(), fout);
				content.get().close();
			}
		} catch (IOException e) {
			logger.error("Could not export IDocument.", e);
		}
		if (tmpFile != null && tmpFile.exists()) {
			return Optional.of(tmpFile);
		}
		return Optional.empty();
	}

	private static String getFileNameWithTimestamp(IDocument iDocument) {
		return getFileNameWithTimestamp(iDocument, false);
	}

	private static String getFileNameWithTimestamp(IDocument iDocument, boolean addSecond) {
		StringBuilder ret = new StringBuilder();
		ret.append(iDocument.getPatient().getCode()).append("_");
		ret.append(iDocument.getPatient().getLastName()).append(StringUtils.SPACE);
		ret.append(iDocument.getPatient().getFirstName()).append("_");
		String title = iDocument.getTitle();
		if (iDocument.getExtension() != null && title.endsWith(iDocument.getExtension())) {
			title = title.substring(0, title.lastIndexOf('.'));
		}
		ret.append(title).append("_");

		Date date = new Date();
		if (addSecond) {
			date.setTime(date.getTime() + 1000);
		}
		ret.append(new SimpleDateFormat("ddMMyyyy_HHmmss").format(date));
		String extension = iDocument.getExtension();
		if (extension != null && extension.indexOf('.') != -1) {
			extension = extension.substring(extension.lastIndexOf('.') + 1);
		}
		ret.append(".").append(extension);
		return ret.toString().replaceAll("[^a-züäöA-ZÜÄÖ0-9 _\\.\\-]", StringUtils.EMPTY);
	}

	private static File getTempFile(IImage iImage) {
		File tmpFile = new File(getAttachmentsFolder(), getFileName(iImage));
		try (FileOutputStream fout = new FileOutputStream(tmpFile);
				ByteArrayInputStream content = new ByteArrayInputStream(iImage.getImage())) {
			IOUtils.copy(content, fout);
		} catch (IOException e) {
			logger.error("Could not export IImage.", e);
		}
		if (tmpFile != null && tmpFile.exists()) {
			return tmpFile;
		}
		return null;
	}

	private static String getFileName(IImage iImage) {
		return iImage.getTitle();
	}

	private static String getFileName(IDocument iDocument) {
		StringBuilder ret = new StringBuilder();
		ret.append(iDocument.getPatient().getCode()).append("_");

		ret.append(iDocument.getPatient().getLastName()).append(StringUtils.SPACE);
		ret.append(iDocument.getPatient().getFirstName()).append("_");
		String title = iDocument.getTitle();
		if (iDocument.getExtension() != null && title.endsWith(iDocument.getExtension())) {
			title = title.substring(0, title.lastIndexOf('.'));
		}
		ret.append(title).append("_");
		ret.append(new SimpleDateFormat("ddMMyyyy_HHmmss").format(iDocument.getLastchanged()));
		String extension = iDocument.getExtension();
		if (extension != null && extension.indexOf('.') != -1) {
			extension = extension.substring(extension.lastIndexOf('.') + 1);
		}
		ret.append(".").append(extension);

		return ret.toString().replaceAll("[^a-züäöA-ZÜÄÖ0-9 _\\.\\-]", StringUtils.EMPTY);
	}

	/**
	 * Convert the files to a String that can be parsed by the mail commands.
	 *
	 * @param attachments
	 * @return
	 */
	public static String getAttachmentsString(List<File> attachments) {
		StringBuilder sb = new StringBuilder();
		for (File file : attachments) {
			if (sb.length() > 0) {
				sb.append(":::");
			}
			sb.append(file.getAbsolutePath());
		}
		return sb.toString();
	}

	/**
	 * Convert the String, that can be parsed by the mail commands, to a list of
	 * files.
	 *
	 * @param attachments
	 * @return
	 */
	public static List<File> getAttachmentsFiles(String attachments) {
		List<File> ret = new ArrayList<>();
		if (attachments != null && !attachments.isEmpty()) {
			String[] parts = attachments.split(":::");
			for (String string : parts) {
				ret.add(new File(string));
			}
		}
		return ret;
	}

	/**
	 * Convert a String of {@link IDocument} references to a String that can be
	 * parsed by the mail commands.
	 *
	 * @param documents
	 * @return
	 */
	public static String toAttachments(String documents) {
		StringJoiner sj = new StringJoiner(":::");
		String[] parts = documents.split(":::");
		for (String string : parts) {
			Optional<Identifiable> loaded = StoreToStringServiceHolder.get().loadFromString(string);
			if (loaded.isPresent() && loaded.get() instanceof IDocument) {
				getTempFile((IDocument) loaded.get()).ifPresent(f -> {
					sj.add(f.getAbsolutePath());
				});
			}
		}
		return sj.toString();
	}

	/**
	 * Convert a {@link IDocument} reference String to a String that can be parsed
	 * by the mail commands.
	 *
	 * @param documents
	 * @return
	 */
	public static String toAttachment(String document) {
		Optional<Identifiable> loaded = StoreToStringServiceHolder.get().loadFromString(document);
		if (loaded.isPresent() && loaded.get() instanceof IDocument) {
			Optional<File> file = getTempFile((IDocument) loaded.get());
			if (file.isPresent()) {
				return file.get().getAbsolutePath();
			}
		}
		return "?";
	}

	/**
	 * Get a String representation for a list of {@link IDocument}.
	 *
	 * @param iDocuments
	 * @return
	 */
	public static String getDocumentsString(List<IDocument> iDocuments) {
		StringJoiner sj = new StringJoiner(":::");
		for (Object object : iDocuments) {
			if (object instanceof IDocument) {
				sj.add(StoreToStringServiceHolder.getStoreToString(object));
			}
		}
		return sj.toString();
	}

	/**
	 * Get a list of {@link IDocument}s from their String representation.
	 * 
	 * @param documents
	 * @return
	 */
	public static List<IDocument> getDocuments(String documents) {
		List<IDocument> ret = new ArrayList<>();
		String[] documentsParts = documents.split(ATTACHMENT_DELIMITER);
		for (String string : documentsParts) {
			Optional<Identifiable> loaded = StoreToStringServiceHolder.get().loadFromString(string);
			if (loaded.isPresent() && loaded.get() instanceof IDocument) {
				ret.add((IDocument) loaded.get());
			}
		}
		return ret;
	}

	public static File getAttachmentsFile(IImage iImage) {
		return getTempFile(iImage);
	}
}
