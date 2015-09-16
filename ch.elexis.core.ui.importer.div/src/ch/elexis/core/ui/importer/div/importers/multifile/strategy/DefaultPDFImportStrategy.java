package ch.elexis.core.ui.importer.div.importers.multifile.strategy;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.text.IOpaqueDocument;
import ch.elexis.core.data.services.GlobalServiceDescriptors;
import ch.elexis.core.data.services.IDocumentManager;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.importer.div.importers.DefaultLabImportUiHandler;
import ch.elexis.core.ui.importer.div.importers.LabImportUtil;
import ch.elexis.core.ui.importer.div.importers.LabImportUtil.TransientLabResult;
import ch.elexis.core.ui.importer.div.importers.Messages;
import ch.elexis.core.ui.importer.div.importers.OverwriteAllImportUiHandler;
import ch.elexis.core.ui.importer.div.importers.multifile.IMultiFileParser;
import ch.elexis.core.ui.text.GenericDocument;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.io.FileTool;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

/**
 * Imports PDFFiles into Omnivore and adds them as a {@link LabResult} to the Labor View
 * 
 * @author lucia
 * 		
 */
public class DefaultPDFImportStrategy implements IFileImportStrategy {
	private static final Logger log = LoggerFactory.getLogger(DefaultPDFImportStrategy.class);
	
	private static final String PDF = "pdf";
	private IDocumentManager docManager;
	private Labor myLab;
	private String labName;
	private Patient patient;
	private TimeTool dateTime;
	private String group;
	private String prio;
	
	private boolean testMode = false;
	
	public DefaultPDFImportStrategy(){
		Object os = Extensions.findBestService(GlobalServiceDescriptors.DOCUMENT_MANAGEMENT);
		if (os != null) {
			this.docManager = (IDocumentManager) os;
		}
	}
	
	@Override
	public Result<Object> execute(File file, Map<String, Object> context){
		if (this.docManager == null) {
			return new Result<Object>(SEVERITY.ERROR, 2,
				MessageFormat.format(Messages.DefaultPDFImportStrategy_NoDocManager, file.getName(),
					patient.getLabel()),
				patient.getId(), true);
		}
		
		try {
			initValuesFromContext(context);
		} catch (IllegalStateException ise) {
			return new Result<Object>(SEVERITY.ERROR, 2,
				Messages.DefaultPDFImportStrategy_InitContextFailed + "\n" + ise.getMessage(),
				context, true);
		}
		
		// get or create LabItem and create labresult
		String name = "Dokument";
		String shortname = "doc";
		LabItem labItem = getLabItem(shortname, name, LabItem.typ.DOCUMENT);
		if (labItem == null) {
			labItem =
				new LabItem(shortname, name, myLab, "", "", PDF, LabItem.typ.DOCUMENT, group, prio);
		}
		
		String titel = generatePDFTitle(file.getName(), dateTime);
		TransientLabResult importResult =
			new TransientLabResult.Builder(patient, myLab, labItem, titel).date(dateTime).build();
			
		String orderId;
		if (testMode) {
			orderId = LabImportUtil.importLabResults(Collections.singletonList(importResult),
				new OverwriteAllImportUiHandler());
		} else {
			orderId = LabImportUtil.importLabResults(Collections.singletonList(importResult),
				new DefaultLabImportUiHandler());
		}
		
		// add doc to document manager
		try {
			addDocument(titel, labName, dateTime.toString(TimeTool.DATE_GER), file, file.getName());
		} catch (IOException | ElexisException e) {
			log.error(
				"error saving pdf [" + file.getAbsolutePath() + "] in document manager (omnivore)");
		}
		return new Result<Object>(SEVERITY.OK, 0, "OK", orderId, false); //$NON-NLS-1$	
	}
	
	private void initValuesFromContext(Map<String, Object> context){
		StringBuilder sbFailed = new StringBuilder();
		patient = (Patient) context.get(IMultiFileParser.CTX_PATIENT);
		if (patient == null) {
			sbFailed.append(Messages.DefaultPDFImportStrategy_Patient);
			sbFailed.append("; ");
		}
		
		myLab = Labor.load((String) context.get(IMultiFileParser.CTX_LABID));
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
	
	private LabItem getLabItem(String shortname, String name, LabItem.typ type){
		Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
		qbe.add(LabItem.SHORTNAME, Query.EQUALS, shortname);
		qbe.add(LabItem.LAB_ID, Query.EQUALS, myLab.getId());
		qbe.add(LabItem.TYPE, Query.EQUALS, new Integer(type.ordinal()).toString());
		
		LabItem labItem = null;
		List<LabItem> itemList = qbe.execute();
		if (itemList.size() > 0) {
			labItem = itemList.get(0);
		}
		
		return labItem;
	}
	
	private String generatePDFTitle(String filename, TimeTool dateTime){
		SimpleDateFormat sdfTitle = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		String title = "Laborbefund" + sdfTitle.format(dateTime.getTime()) + "." //$NON-NLS-2$
			+ FileTool.getExtension(filename);
		log.debug("generated labresult pdf title '" + title + "");
		return title;
	}
	
	private boolean addDocument(final String title, final String category, final String dateStr,
		final File file, String keywords) throws IOException, ElexisException{
		findOrCreateCategory(category);
		
		List<IOpaqueDocument> documentList = this.docManager.listDocuments(this.patient, category,
			title, null, new TimeSpan(dateStr + "-" + dateStr), null); //$NON-NLS-1$
			
		if (documentList == null || documentList.size() == 0) {
			this.docManager.addDocument(new GenericDocument(this.patient, title, category, file,
				dateStr, keywords, FileTool.getExtension(file.getName())));
			return true;
		}
		return false;
	}
	
	private void findOrCreateCategory(String category){
		if (category != null) {
			boolean exists = false;
			String[] categories = docManager.getCategories();
			for (String cat : categories) {
				if (category.equals(cat)) {
					exists = true;
				}
			}
			
			if (!exists) {
				if (docManager.addCategorie(category)) {
					log.info("Created category " + category + " for multi file import");
				} else {
					log.warn("Could not create category " + category + " for multi file import");
				}
			}
		}
	}
	
	@Override
	public void setTestMode(boolean testing){
		this.testMode = testing;
	}
}
