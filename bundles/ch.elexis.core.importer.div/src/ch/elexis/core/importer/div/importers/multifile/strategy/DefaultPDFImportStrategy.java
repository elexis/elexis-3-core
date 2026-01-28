package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.ILabContactResolver;
import ch.elexis.core.importer.div.importers.IPersistenceHandler;
import ch.elexis.core.importer.div.importers.ImportHandler;
import ch.elexis.core.importer.div.importers.OverwriteAllImportHandler;
import ch.elexis.core.importer.div.importers.TransientLabResult;
import ch.elexis.core.importer.div.importers.multifile.IMultiFileParser;
import ch.elexis.core.importer.div.service.holder.LabImportUtilHolder;
import ch.elexis.core.importer.div.service.holder.OmnivoreDocumentStoreServiceHolder;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.TimeTool;

/**
 * Imports PDFFiles into Omnivore and adds them as a {@link LabResult} to the
 * Labor View
 *
 * @author lucia
 *
 */
public class DefaultPDFImportStrategy implements IFileImportStrategy {
	private static final Logger log = LoggerFactory.getLogger(DefaultPDFImportStrategy.class);

	private static final String PDF = "pdf";
	private ILaboratory myLab;
	private String labName;
	private IPatient patient;
	private TimeTool dateTime;
	private String group;
	private String prio;

	private boolean testMode = false;

	private boolean moveAfterImport;

	private ImportHandler defaultImportHandler;

	private String pdfImportCategory;

	public DefaultPDFImportStrategy(ImportHandler defaultImportHandler) {
		this.defaultImportHandler = defaultImportHandler;
	}

	@Override
	public Result<Object> execute(IVirtualFilesystemHandle fileHandle, Map<String, Object> context, HL7Parser hl7parser,
			IPersistenceHandler persistenceHandler) {

		ImportHandler importHandler;
		if (testMode) {
			importHandler = new OverwriteAllImportHandler();
		} else {
			importHandler = defaultImportHandler;
		}

		try {
			initValuesFromContext(context);
			if (!OmnivoreDocumentStoreServiceHolder.isAvailable()) {
				if (moveAfterImport) {
					try {
						FileImportStrategyUtil.moveAfterImport(false, fileHandle);
					} catch (IOException e) {
						return new Result<>(SEVERITY.ERROR, 2,
								"Could not move after import [" + fileHandle.getAbsolutePath() + "]: " + e.getMessage(),
								context, true);
					}
				}
				return new Result<>(SEVERITY.ERROR, 2,
						MessageFormat.format(Messages.DefaultPDFImportStrategy_NoDocManager, fileHandle.getName(),
								patient.getLabel()),
						patient.getId(), true);
			}
		} catch (IllegalStateException ise) {
			if (moveAfterImport) {
				try {
					FileImportStrategyUtil.moveAfterImport(false, fileHandle);
				} catch (IOException e) {
					return new Result<>(SEVERITY.ERROR, 2,
							"Could not move after import [" + fileHandle.getAbsolutePath() + "]: " + e.getMessage(),
							context, true);
				}
			}
			return new Result<>(SEVERITY.ERROR, 2,
					Messages.DefaultPDFImportStrategy_InitContextFailed + StringUtils.LF + ise.getMessage(), context,
					true);
		}

		// get or create LabItem and create labresult
		String name = "Dokument";
		String shortname = "doc";
		ILabItem labItem = LabImportUtilHolder.get().getLabItem(shortname, name, LabItemTyp.DOCUMENT).orElse(null);
		if (labItem == null) {
			labItem = LabImportUtilHolder.get().createLabItem(shortname, name, myLab, StringUtils.EMPTY,
					StringUtils.EMPTY, PDF, LabItemTyp.DOCUMENT, group, prio);
			log.debug("LabItem created [{}]", labItem);
		}

		String titel = generatePDFTitle(fileHandle.getName(), dateTime);

		String orderId = "noorder";

		// add doc to document manager
		try {
			String category = StringUtils.isNotBlank(pdfImportCategory) ? pdfImportCategory : labName;
			if (addDocument(titel, category, dateTime, fileHandle, fileHandle.getName())) {
				TransientLabResult importResult = new TransientLabResult.Builder(patient, myLab, labItem, titel)
						.date(dateTime).build(LabImportUtilHolder.get());

				orderId = LabImportUtilHolder.get().importLabResults(Collections.singletonList(importResult),
						importHandler);
			} else {
				log.error("pdf [{}] already present in document manager (omnivore)", fileHandle.getAbsolutePath());
			}
		} catch (IOException | IllegalStateException | ElexisException e) {
			log.error("error saving pdf [{}] in document manager (omnivore)", fileHandle.getAbsolutePath(), e);
			return new Result<>(SEVERITY.ERROR, 2,
					"Could not store document [" + fileHandle.getAbsolutePath() + "]: " + e.getMessage(), context,
					true);
		}
		if (moveAfterImport) {
			try {
				FileImportStrategyUtil.moveAfterImport(true, fileHandle);
			} catch (IOException e) {
				e.printStackTrace();
				return new Result<>(SEVERITY.ERROR, 2,
						"Could not move after import [" + fileHandle.getAbsolutePath() + "]: " + e.getMessage(),
						context, true);
			}
		}
		return new Result<>(SEVERITY.OK, 0, "OK", orderId, false); //$NON-NLS-1$
	}

	private void initValuesFromContext(Map<String, Object> context) {
		StringBuilder sbFailed = new StringBuilder();
		patient = (IPatient) context.get(IMultiFileParser.CTX_PATIENT);
		if (patient == null) {
			sbFailed.append(Messages.Core_Patient);
			sbFailed.append("; ");
		}

		myLab = LabImportUtilHolder.get()
				.loadCoreModel((String) context.get(IMultiFileParser.CTX_LABID), ILaboratory.class).orElse(null);
		if (myLab == null) {
			sbFailed.append(Messages.Core_Laboratory);
			sbFailed.append("; ");
		}

		if (ConfigServiceHolder.get().getLocal(HL7Parser.CFG_IMPORT_ENCDATA, false)) {
			labName = ConfigServiceHolder.get().getLocal(HL7Parser.CFG_IMPORT_ENCDATA_CATEGORY, null);
		}

		if (labName == null || labName.isEmpty()) {
			labName = (String) context.get(IMultiFileParser.CTX_LABNAME);
			if (labName == null) {
				sbFailed.append(Messages.DefaultPDFImportStrategy_LabName);
				sbFailed.append("; ");
			}
		}

		dateTime = (TimeTool) context.get(IMultiFileParser.CTX_TIME);
		if (dateTime == null) {
			sbFailed.append(Messages.Core_Date);
			sbFailed.append("; ");
		}

		group = (String) context.get(IMultiFileParser.CTX_GROUP);
		if (group == null) {
			sbFailed.append(Messages.Core_Group);
			sbFailed.append("; ");
		}

		prio = (String) context.get(IMultiFileParser.CTX_PRIO);
		if (prio == null) {
			sbFailed.append(Messages.DefaultPDFImportStrategy_Prio);
			sbFailed.append("; ");
		}
		String msg = sbFailed.toString();
		if (msg != null && !msg.isEmpty()) {
			throw new IllegalStateException(msg);
		}
	}

	private String generatePDFTitle(String filename, TimeTool dateTime) {
		SimpleDateFormat sdfTitle = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		String title = "Laborbefund" + sdfTitle.format(dateTime.getTime()) + "." //$NON-NLS-2$
				+ FilenameUtils.getExtension(filename);
		log.debug("generated labresult pdf title '" + title + StringUtils.EMPTY);
		return title;
	}

	private boolean addDocument(final String title, final String category, final TimeTool creationTime,
			final IVirtualFilesystemHandle fileHandle, String keywords) throws IOException, ElexisException {
		ICategory iCategory = findOrCreateCategory(category);

		List<IDocument> existing = OmnivoreDocumentStoreServiceHolder.get().getDocuments(patient.getId(), null,
				iCategory, null);
		existing = existing.stream().filter(d -> documentMatches(d, title, dateTime)).collect(Collectors.toList());

		if (existing.isEmpty()) {
			IDocument document = OmnivoreDocumentStoreServiceHolder.get().createDocument(patient.getId(), title,
					iCategory.getName());
			String extension = FilenameUtils.getExtension(fileHandle.getName());
			document.setCreated(dateTime.getTime());
			document.setExtension(extension);
			document.setMimeType(extension.toLowerCase());
			document.setKeywords(keywords);
			try (InputStream is = fileHandle.openInputStream()) {
				OmnivoreDocumentStoreServiceHolder.get().saveDocument(document, is);
			}
			return true;
		} else {
			log.warn("Overwriting existing lab document [" + title + "]");
			try (InputStream is = fileHandle.openInputStream()) {
				OmnivoreDocumentStoreServiceHolder.get().saveDocument(existing.get(0), is);
			}
		}
		return false;
	}

	private boolean documentMatches(IDocument document, String title, TimeTool timeTool) {
		return document.getTitle().equals(title) && document.getCreated().equals(timeTool.getTime());
	}

	private ICategory findOrCreateCategory(String category) {
		if (category == null) {
			return OmnivoreDocumentStoreServiceHolder.get().getCategoryDefault();
		}
		List<ICategory> categories = OmnivoreDocumentStoreServiceHolder.get().getCategories();
		for (ICategory iCategory : categories) {
			if (iCategory.getName().equals(category)) {
				return iCategory;
			}
		}
		// does not exist -> create
		log.info("Created category " + category + " for multi file import");
		return OmnivoreDocumentStoreServiceHolder.get().createCategory(category);
	}

	@Override
	public void setTestMode(boolean testing) {
		this.testMode = testing;
	}

	@Override
	public IFileImportStrategy setMoveAfterImport(boolean value) {
		this.moveAfterImport = value;
		return this;
	}

	/**
	 * Add the {@link ILabContactResolver} that should be used on import.
	 *
	 * @param resolver
	 * @return
	 */
	public IFileImportStrategy setLabContactResolver(ILabContactResolver resolver) {
		// currently no use for a contact resolver here
		return this;
	}

	public DefaultPDFImportStrategy setPDFImportCategory(String category) {
		this.pdfImportCategory = category;
		return this;
	}
}
