package ch.elexis.core.ui.importer.div.importers.multifile.strategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
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
import ch.elexis.core.importer.div.importers.multifile.strategy.FileImportStrategyUtil;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategy;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.importer.div.importers.DefaultLabImportUiHandler;
import ch.elexis.core.ui.importer.div.importers.Messages;
import ch.elexis.core.ui.importer.div.services.DocumentStoreServiceHolder;
import ch.elexis.core.ui.importer.div.services.LabImportUtilHolder;
import ch.elexis.data.LabResult;
import ch.rgw.io.FileTool;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.TimeTool;

/**
 * Imports PDFFiles into Omnivore and adds them as a {@link LabResult} to the Labor View
 * 
 * @author lucia
 * 		
 */
@Component
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
	
	@Override
	public Result<Object> execute(File file, Map<String, Object> context, HL7Parser hl7parser, IPersistenceHandler persistenceHandler){
		try {
			initValuesFromContext(context);
			if (DocumentStoreServiceHolder.isAvailable()) {
				if (moveAfterImport) {
					FileImportStrategyUtil.moveAfterImport(false, file);
				}
				return new Result<Object>(SEVERITY.ERROR, 2,
					MessageFormat.format(Messages.DefaultPDFImportStrategy_NoDocManager,
						file.getName(), patient.getLabel()),
					patient.getId(), true);
			}
		} catch (IllegalStateException ise) {
			if (moveAfterImport) {
				FileImportStrategyUtil.moveAfterImport(false, file);
			}
			return new Result<Object>(SEVERITY.ERROR, 2,
				Messages.DefaultPDFImportStrategy_InitContextFailed + "\n" + ise.getMessage(),
				context, true);
		}
		
		// get or create LabItem and create labresult
		String name = "Dokument";
		String shortname = "doc";
		ILabItem labItem =
			LabImportUtilHolder.get().getLabItem(shortname, name, LabItemTyp.DOCUMENT).orElse(null);
		if (labItem == null) {
			labItem = LabImportUtilHolder.get().createLabItem(shortname, name, myLab, "", "", PDF,
				LabItemTyp.DOCUMENT, group, prio);
			log.debug("LabItem created [{}]", labItem);
		}
		
		String titel = generatePDFTitle(file.getName(), dateTime);
		
		TransientLabResult importResult =
			new TransientLabResult.Builder(patient, myLab, labItem, titel).date(dateTime)
				.build(LabImportUtilHolder.get());
				
		ImportHandler importHandler;
		if (testMode) {
			importHandler = new OverwriteAllImportHandler();
		} else {
			importHandler = new DefaultLabImportUiHandler();
		}
		String orderId =
			LabImportUtilHolder.get().importLabResults(Collections.singletonList(importResult),
				importHandler);
			
		// add doc to document manager
		try {
			addDocument(titel, labName, dateTime, file, file.getName());
		} catch (IOException | ElexisException e) {
			log.error(
				"error saving pdf [" + file.getAbsolutePath() + "] in document manager (omnivore)");
		}
		if (moveAfterImport) {
			FileImportStrategyUtil.moveAfterImport(true, file);
		}
		return new Result<Object>(SEVERITY.OK, 0, "OK", orderId, false); //$NON-NLS-1$	
	}
	
	private void initValuesFromContext(Map<String, Object> context){
		StringBuilder sbFailed = new StringBuilder();
		patient = (IPatient) context.get(IMultiFileParser.CTX_PATIENT);
		if (patient == null) {
			sbFailed.append(Messages.DefaultPDFImportStrategy_Patient);
			sbFailed.append("; ");
		}
		
		myLab = LabImportUtilHolder.get()
			.loadCoreModel((String) context.get(IMultiFileParser.CTX_LABID), ILaboratory.class)
			.get();
		if (myLab == null) {
			sbFailed.append(Messages.DefaultPDFImportStrategy_Lab);
			sbFailed.append("; ");
		}
		
		labName = (String) context.get(IMultiFileParser.CTX_LABNAME);
		if (labName == null) {
			sbFailed.append(Messages.DefaultPDFImportStrategy_LabName);
			sbFailed.append("; ");
		}
		
		dateTime = (TimeTool) context.get(IMultiFileParser.CTX_TIME);
		if (dateTime == null) {
			sbFailed.append(Messages.DefaultPDFImportStrategy_Date);
			sbFailed.append("; ");
		}
		
		group = (String) context.get(IMultiFileParser.CTX_GROUP);
		if (group == null) {
			sbFailed.append(Messages.DefaultPDFImportStrategy_Group);
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
	
	private String generatePDFTitle(String filename, TimeTool dateTime){
		SimpleDateFormat sdfTitle = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		String title = "Laborbefund" + sdfTitle.format(dateTime.getTime()) + "." //$NON-NLS-2$
			+ FileTool.getExtension(filename);
		log.debug("generated labresult pdf title '" + title + "");
		return title;
	}
	
	private boolean addDocument(final String title, final String category,
		final TimeTool creationTime, final File file, String keywords)
		throws IOException, ElexisException{
		ICategory iCategory = findOrCreateCategory(category);
		
		List<IDocument> existing =
			DocumentStoreServiceHolder.get().getDocuments(patient.getId(), null, iCategory, null);
		existing = existing.stream().filter(d -> documentMatches(d, title, dateTime))
			.collect(Collectors.toList());
		
		if (existing.isEmpty()) {
			IDocument document =
				DocumentStoreServiceHolder.get().createDocument(patient.getId(), title,
					iCategory.getName());
			document.setCreated(dateTime.getTime());
			document.setExtension(FileTool.getExtension(file.getName()));
			document.setKeywords(keywords);
			DocumentStoreServiceHolder.get().saveDocument(document, new FileInputStream(file));
			return true;
		}
		return false;
	}
	
	private boolean documentMatches(IDocument document, String title, TimeTool timeTool){
		return document.getTitle().equals(title)
			&& document.getCreated().equals(timeTool.getTime());
	}
	
	private ICategory findOrCreateCategory(String category){
		if (category == null) {
			return DocumentStoreServiceHolder.get().getCategoryDefault();
		}
		List<ICategory> categories = DocumentStoreServiceHolder.get().getCategories();
		for (ICategory iCategory : categories) {
			if (iCategory.getName().equals(category)) {
				return iCategory;
			}
		}
		// does not exist -> create
		log.info("Created category " + category + " for multi file import");
		return DocumentStoreServiceHolder.get().createCategory(category);
	}
	
	@Override
	public void setTestMode(boolean testing){
		this.testMode = testing;
	}
	
	@Override
	public IFileImportStrategy setMoveAfterImport(boolean value){
		this.moveAfterImport = value;
		return this;
	}
	
	/**
	 * Add the {@link ILabContactResolver} that should be used on import.
	 * 
	 * @param resolver
	 * @return
	 */
	public IFileImportStrategy setLabContactResolver(ILabContactResolver resolver){
		// currently no use for a contact resolver here
		return this;
	}
}
