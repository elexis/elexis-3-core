package ch.elexis.core.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.mail.internal.DocumentStoreServiceHolder;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.utils.CoreUtil;

public class AttachmentsUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(AttachmentsUtil.class);
	
	private static File attachmentsFolder;
	
	private synchronized static File getAttachmentsFolder(){
		if (attachmentsFolder == null) {
			File tmpDir = CoreUtil.getTempDir();
			attachmentsFolder = new File(tmpDir, "_att" + System.currentTimeMillis() + "_");
			attachmentsFolder.mkdir();
		}
		return attachmentsFolder;
	}
	
	private static Optional<File> getTempFile(IDocument iDocument){
		File tmpFile = new File(getAttachmentsFolder(), getFileName(iDocument));
		try (FileOutputStream fout = new FileOutputStream(tmpFile)) {
			Optional<InputStream> content =
				DocumentStoreServiceHolder.getService().loadContent(iDocument);
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
	
	private static String getFileName(IDocument iDocument){
		StringBuilder ret = new StringBuilder();
		ret.append(iDocument.getPatient().getCode()).append("_");
		
		ret.append(iDocument.getPatient().getLastName()).append("_");
		ret.append(iDocument.getPatient().getFirstName()).append("_");
		String title = iDocument.getTitle();
		if (iDocument.getExtension() != null && title.endsWith(iDocument.getExtension())) {
			title = title.substring(0, title.lastIndexOf('.'));
		}
		ret.append(title).append("_");
		ret.append(new SimpleDateFormat("dd_MM_yyyy_HHmmss").format(iDocument.getLastchanged()));
		String extension = iDocument.getExtension();
		if (extension.indexOf('.') != -1) {
			extension = extension.substring(extension.lastIndexOf('.') + 1);
		}
		ret.append(".").append(extension);
		
		return ret.toString().replaceAll("[^a-züäöA-ZÜÄÖ0-9_\\.\\-]", "");
	}
	
	/**
	 * Convert the files to a String that can be parsed by the mail commands.
	 * 
	 * @param attachments
	 * @return
	 */
	public static String getAttachmentsString(List<File> attachments){
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
	 * Convert the String, that can be parsed by the mail commands, to a list of files.
	 * 
	 * @param attachments
	 * @return
	 */
	public static List<File> getAttachmentsFiles(String attachments){
		List<File> ret = new ArrayList<File>();
		if (attachments != null && !attachments.isEmpty()) {
			String[] parts = attachments.split(":::");
			for (String string : parts) {
				ret.add(new File(string));
			}
		}
		return ret;
	}
	
	/**
	 * Convert a String of {@link IDocument} references to a String that can be parsed by the mail
	 * commands.
	 * 
	 * @param documents
	 * @return
	 */
	public static String toAttachments(String documents){
		StringJoiner sj = new StringJoiner(":::");
		String[] parts = documents.split(":::");
		for (String string : parts) {
			Object loaded = StoreToStringServiceHolder.getLoadFromString(string);
			if (loaded instanceof IDocument) {
				getTempFile((IDocument) loaded).ifPresent(f -> {
					sj.add(f.getAbsolutePath());
				});
			}
		}
		return sj.toString();
	}
	
	/**
	 * Get a String representation for a list of {@link IDocument}.
	 * 
	 * @param iDocuments
	 * @return
	 */
	public static String getDocumentsString(List<IDocument> iDocuments){
		StringJoiner sj = new StringJoiner(":::");
		for (Object object : iDocuments) {
			if (object instanceof IDocument) {
				sj.add(StoreToStringServiceHolder.getStoreToString(object));
			}
		}
		return sj.toString();
	}
}
