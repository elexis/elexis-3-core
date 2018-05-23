package ch.elexis.core.ui.importer.div.importers;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.services.GlobalServiceDescriptors;
import ch.elexis.core.data.services.IDocumentManager;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.importer.div.importers.ILabImportUtil;
import ch.elexis.core.importer.div.importers.ImportHandler;
import ch.elexis.core.importer.div.importers.TransientLabResult;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.text.GenericDocument;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabMapping;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Xid;
import ch.elexis.hl7.model.OrcMessage;
import ch.elexis.hl7.model.TextData;
import ch.elexis.hl7.v26.HL7Constants;
import ch.rgw.tools.TimeTool;

/**
 * Utility class that provides basic functionality a Lab importer implementation needs. Lab
 * importers should use this class!
 * 
 * @author thomashu
 * 
 */
public class LabImportUtil implements ILabImportUtil {
	private static Logger logger = LoggerFactory.getLogger(LabImportUtil.class);
	
	/**
	 * Searches for a Labor matching the identifier as part of the Kuerzel or Name attribute. If no
	 * matching Labor is found, a new Labor is created with identifier as Kuerzel.
	 * 
	 * @param identifier
	 * @return
	 */
	public static Labor getOrCreateLabor(String identifier){
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Labor identifier [" + identifier + "] invalid.");
		}
		Labor labor = null;
		Query<Labor> qbe = new Query<Labor>(Labor.class);
		qbe.startGroup();
		qbe.add(Kontakt.FLD_SHORT_LABEL, Query.LIKE, "%" + identifier + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		qbe.or();
		qbe.add(Kontakt.FLD_NAME1, Query.LIKE, "%" + identifier + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		qbe.endGroup();
		List<Labor> results = qbe.execute();
		if (results.isEmpty()) {
			labor = new Labor(identifier, "Labor " + identifier); //$NON-NLS-1$
			logger.warn(
				"Found no Labor for identifier [" + identifier + "]. Created new Labor contact.");
		} else {
			labor = results.get(0);
			if (results.size() > 1) {
				logger.warn("Found more than one Labor for identifier [" + identifier
					+ "]. This can cause problems when importing results.");
			}
		}
		return labor;
	}
	
	/**
	 * find or set the labor this identifier is linked to
	 * 
	 * @param identifier
	 *            the value to match to a labor
	 * @return the found or new set labor contact or null if no lab could be found AND none was
	 *         selected
	 */
	public static Labor getLinkLabor(String identifier){
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("Labor identifier [" + identifier + "] invalid.");
		}
		Labor labor = null;
		// check if there is a connection to an XID
		Kontakt k = (Kontakt) Xid.findObject(XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY, identifier);
		if (k != null) {
			labor = Labor.load(k.getId());
		}
		
		// if no connection exists ask to which lab it should be linked
		if (labor == null) {
			KontaktSelektor ks = new KontaktSelektor(UiDesk.getTopShell(), Labor.class,
				Messages.LabImporterUtil_Select,
				Messages.LabImporterUtil_SelectLab + " [" + identifier + "]",
				Kontakt.DEFAULT_SORT);
			if (ks.open() == Dialog.OK) {
				labor = (Labor) ks.getSelection();
				labor.addXid(XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY, identifier, true);
			}
		}
		return labor;
	}
	
	/**
	 * Searches for a LabItem with an existing LabMapping for the identifier and the labor. If there
	 * is no LabMapping, for backwards compatibility the LaborId and Kürzel attributes of all
	 * LabItems will be used to find a match.
	 * 
	 * @param identifier
	 * @param labor
	 * @return
	 */
	public static LabItem getLabItem(String identifier, Labor labor){
		LabMapping mapping = LabMapping.getByContactAndItemName(labor.getId(), identifier);
		if (mapping != null) {
			return mapping.getLabItem();
		}
		
		LabItem labItem = null;
		Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
		qbe.add(LabItem.LAB_ID, Query.EQUALS, labor.getId());
		qbe.add(LabItem.SHORTNAME, Query.EQUALS, identifier);
		List<LabItem> list = qbe.execute();
		if (!list.isEmpty()) {
			labItem = list.get(0);
			if (list.size() > 1) {
				logger.warn("Found more than one LabItem for identifier [" + identifier
					+ "] and Labor [" + labor.getLabel(true)
					+ "]. This can cause problems when importing results.");
			}
		}
		return labItem;
	}
	
	/**
	 * Search for a LabResult with matching patient, item and timestamps. The timestamp attributes
	 * can be null if not relevant for the search, but at least one timestamp has to be specified.
	 * 
	 * @param patient
	 * @param timestamp
	 * @param item
	 * @return
	 */
	public static List<LabResult> getLabResults(IPatient patient, ILabItem item, TimeTool date,
		TimeTool analyseTime, TimeTool observationTime){
		
		if (date == null && analyseTime == null && observationTime == null) {
			throw new IllegalArgumentException("No timestamp specified.");
		}
		
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.add(LabResult.PATIENT_ID, Query.EQUALS, patient.getId());
		qr.add(LabResult.ITEM_ID, Query.EQUALS, item.getId());
		if (date != null) {
			qr.add(LabResult.DATE, Query.EQUALS, date.toString(TimeTool.DATE_GER));
		}
		if (analyseTime != null) {
			qr.add(LabResult.ANALYSETIME, Query.EQUALS, analyseTime.toString(TimeTool.TIMESTAMP));
		}
		if (observationTime != null) {
			qr.add(LabResult.OBSERVATIONTIME, Query.EQUALS,
				observationTime.toString(TimeTool.TIMESTAMP));
		}
		return qr.execute();
	}
	
	/**
	 * Import a list of TransientLabResults. Create LabOrder objects for new results.
	 */
	public String importLabResults(List<TransientLabResult> results, ImportHandler uiHandler){
		boolean overWriteAll = false;
		String mandantId = findMandantIdForLabResults(results);
		String orderId = LabOrder.getNextOrderId();
		boolean newResult = false;
		for (TransientLabResult transientLabResult : results) {
			List<LabResult> existing = getExistingResults(transientLabResult);
			if (existing.isEmpty()) {
				ILabResult labResult = createLabResult(transientLabResult, orderId, mandantId);
				newResult = true;
				CoreHub.getLocalLockService().acquireLock((LabResult) labResult);
				CoreHub.getLocalLockService().releaseLock((LabResult) labResult);
			} else {
				for (LabResult labResult : existing) {
					if (overWriteAll) {
						CoreHub.getLocalLockService().acquireLock((LabResult) labResult);
						transientLabResult.overwriteExisting(labResult);
						CoreHub.getLocalLockService().releaseLock((LabResult) labResult);
						continue;
					}
					// dont bother user if result has the same value
					if (transientLabResult.isSameResult(labResult)) {
						logger.info("Result " + labResult.toString() + " already exists.");
						continue;
					}
					
					ImportHandler.OverwriteState retVal = uiHandler.askOverwrite(
						transientLabResult.getPatient(), labResult, transientLabResult);
					
					if (retVal == ImportHandler.OverwriteState.OVERWRITE) {
						CoreHub.getLocalLockService().acquireLock((LabResult) labResult);
						transientLabResult.overwriteExisting(labResult);
						CoreHub.getLocalLockService().releaseLock((LabResult) labResult);
						continue;
					} else if (retVal == ImportHandler.OverwriteState.OVERWRITEALL) {
						overWriteAll = true;
						CoreHub.getLocalLockService().acquireLock((LabResult) labResult);
						transientLabResult.overwriteExisting(labResult);
						CoreHub.getLocalLockService().releaseLock((LabResult) labResult);
						continue;
					} else {
						logger.info("Will not overwrite labResult [" + labResult.getId()
							+ "] due to user decision.");
					}
				}
			}
		}
		// if no result was created, no laborder was created, lookup existing orderid with 1st result
		if (!newResult && !results.isEmpty()) {
			List<LabResult> existing = getExistingResults(results.get(0));
			if (!existing.isEmpty()) {
				List<LabOrder> orders = LabOrder.getLabOrders(existing.get(0).getPatient().getId(),
					null, null, existing.get(0), null, null, null);
				if (!orders.isEmpty()) {
					orderId = orders.get(0).get(LabOrder.FLD_ORDERID);
				}
			}
		}
		ElexisEventDispatcher.getInstance()
			.fire(new ElexisEvent(null, LabResult.class, ElexisEvent.EVENT_RELOAD));
		
		return orderId;
	}
	
	/**
	 * Tries to find the {@link Mandant} Id for given {@link TransientLabResult}
	 * 
	 * @param results
	 * @return
	 */
	private String findMandantIdForLabResults(List<TransientLabResult> results){
		if (results != null && !results.isEmpty()) {
			TransientLabResult transientLabResult = results.get(0);
			OrcMessage orcMessage = transientLabResult.getOrcMessage();
			// case 1 try to find mandant via orc message
			if (orcMessage != null && !orcMessage.getNames().isEmpty()) {
				for (String name : orcMessage.getNames()) {
					String[] splitNames = name.split(" ");
					int size = splitNames.length;
					if (size > 1) {
						Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
						qbe.startGroup();
						qbe.add(Kontakt.FLD_NAME1, Query.LIKE, splitNames[0], true);
						qbe.and();
						qbe.add(Kontakt.FLD_NAME2, Query.LIKE, splitNames[1], true);
						qbe.endGroup();
						qbe.or();
						qbe.startGroup();
						qbe.add(Kontakt.FLD_NAME1, Query.LIKE, splitNames[1], true);
						qbe.and();
						qbe.add(Kontakt.FLD_NAME2, Query.LIKE, splitNames[0], true);
						qbe.endGroup();
						List<Mandant> list = qbe.execute();
						if (list.size() == 1) {
							String id = list.get(0).getId();
							if (id != null) {
								logger.debug("labimport - mandantor [" + id
									+ "] found with orc name db match");
								return id;
							}
							
						}
					}
				}
				logger.warn("labimport - " + orcMessage.getNames().toString()
					+ " not found or not unique in db - try to find mandantor via last konsultation");
			}
			
			// case 2 try to find mandant via last consultation for patient
			IPatient iPatient = transientLabResult.getPatient();
			if (iPatient != null) {
				Patient patient = Patient.load(iPatient.getId());
				if (patient.exists()) {
					Konsultation konsultation = patient.getLastKonsultation();
					if (konsultation != null && konsultation.exists()) {
						Mandant mandant = konsultation.getMandant();
						if (mandant != null && mandant.getId() != null) {
							logger.debug("labimport - mandantor found [" + mandant.getId()
								+ "] with last konsultation");
							return mandant.getId();
						}
					}
				}
			}
		}

		// case 3 use the current mandant
		Mandant mandant = ElexisEventDispatcher.getSelectedMandator();
		if (mandant != null) {
			logger.debug("labimport - use the active selected mandantor [" + mandant.getId() + "]");
			return mandant.getId();
		}
		throw new RuntimeException("No selected mandantor found!"); //should not happen!
	}
	
	/**
	 * Match for existing result with same item and date. Matching dates are checked for validitiy
	 * (not same as transmission date).
	 * 
	 * @param transientLabResult
	 * @return
	 */
	private static List<LabResult> getExistingResults(TransientLabResult transientLabResult){
		List<LabResult> ret = Collections.emptyList();
		
		// don't overwrite documents
		if (!transientLabResult.getLabItem().getTyp().equals(LabItemTyp.DOCUMENT)) {
			if (transientLabResult.isObservationTime()) {
				ret = LabImportUtil.getLabResults(transientLabResult.getPatient(),
					transientLabResult.getLabItem(), null, null,
					transientLabResult.getObservationTime());
			} else if (transientLabResult.isAnalyseTime()) {
				ret = LabImportUtil.getLabResults(transientLabResult.getPatient(),
					transientLabResult.getLabItem(), null, transientLabResult.getAnalyseTime(),
					null);
			} else {
				ret = LabImportUtil.getLabResults(transientLabResult.getPatient(),
					transientLabResult.getLabItem(), transientLabResult.getDate(), null, null);
			}
			
			// filter by subid
			if (transientLabResult.getSubId() != null) {
				Iterator<LabResult> it = ret.iterator();
				while (it.hasNext()) {
					LabResult result = it.next();
					String subId = result.getDetail(LabResult.EXTINFO_HL7_SUBID);
					if (subId != null && !transientLabResult.getSubId().equals(subId)) {
						it.remove();
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * Create the LabResult from the TransientLabResult. Also lookup if there is a matching
	 * LabOrder. If it is in State.ORDERED the created LabResult is set to that LabOrder, else
	 * create a new LabOrder and add the LabResult to it.
	 * 
	 * @deprecated Use {@link #createLabResult(TransientLabResult, String, String)}
	 * @param transientLabResult
	 * @param orderId
	 * @return the created lab result element
	 */
	@Deprecated
	public ILabResult createLabResult(TransientLabResult transientLabResult, String orderId){
		return createLabResult(transientLabResult, orderId, CoreHub.actMandant.getId());
	}
	
	public ILabResult createLabResult(TransientLabResult transientLabResult, String orderId,
		String mandantId){
		ILabResult labResult = null;
		
		List<LabOrder> existing = LabOrder.getLabOrders(transientLabResult.getPatient().getId(),
			null, transientLabResult.getLabItem(), null, null, null, State.ORDERED);
		
		LabOrder labOrder = null;
		if (existing == null || existing.isEmpty()) {

			TimeTool time = transientLabResult.getObservationTime();
			if (time == null) {
				time = transientLabResult.getDate();
			}

			labResult = transientLabResult.persist(null, orderId, mandantId, time, "Import");
			labOrder = (LabOrder) labResult.getLabOrder();
			
		} else {
			// TODO for multiple entries we could check on which one the observationtime matches
			labOrder = existing.get(0);
			labResult = transientLabResult.persist(labOrder, null, null, null, null);
		}
		
		labOrder.setState(State.DONE_IMPORT);
		
		return labResult;
	}
	
	@Override
	public ILabItem getLabItem(String identifier, IContact labor){
		LabMapping mapping = LabMapping.getByContactAndItemName(labor.getId(), identifier);
		if (mapping != null) {
			return mapping.getLabItem();
		}
		
		LabItem labItem = null;
		Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
		qbe.add(LabItem.LAB_ID, Query.EQUALS, labor.getId());
		qbe.add(LabItem.SHORTNAME, Query.EQUALS, identifier);
		List<LabItem> list = qbe.execute();
		if (!list.isEmpty()) {
			labItem = list.get(0);
			if (list.size() > 1) {
				logger.warn(
					"Found more than one LabItem for identifier [" + identifier + "] and Labor ["
						+ labor.getLabel() + "]. This can cause problems when importing results.");
			}
		}
		return labItem;
	}
	
	@Override
	public ILabItem createLabItem(String code, String name, IContact labor, String male,
		String female, String unit, LabItemTyp typ, String testGroupName,
		String nextTestGroupSequence){
		return new LabItem(code, name, labor.getId(), male, female, unit, typ, testGroupName,
			nextTestGroupSequence);
	}
	
	@Override
	public ILabItem getDocumentLabItem(String shortname, String name, IContact labor){
		Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
		qbe.add(LabItem.SHORTNAME, Query.EQUALS, shortname);
		qbe.add(LabItem.LAB_ID, Query.EQUALS, labor.getId());
		qbe.add(LabItem.TYPE, Query.EQUALS, new Integer(LabItemTyp.DOCUMENT.ordinal()).toString());
		
		LabItem labItem = null;
		List<LabItem> itemList = qbe.execute();
		if (itemList.size() > 0) {
			labItem = itemList.get(0);
		}
		
		return labItem;
	}
	
	@Override
	public void createCommentsLabResult(TextData hl7TextData, IPatient pat, IContact labor,
		int number, TimeTool commentDate){
		if (hl7TextData.getDate() == null) {
			hl7TextData.setDate(commentDate.getTime());
		}
		TimeTool commentsDate = new TimeTool(hl7TextData.getDate());
		
		// find LabItem
		Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
		qbe.add(LabItem.LAB_ID, Query.EQUALS, labor.getId());
		qbe.add(LabItem.TITLE, Query.EQUALS, HL7Constants.COMMENT_NAME);
		qbe.add(LabItem.SHORTNAME, Query.EQUALS, HL7Constants.COMMENT_CODE);
		List<LabItem> list = qbe.execute();
		LabItem li = null;
		if (list.size() < 1) {
			// LabItem doesn't yet exist
			LabItemTyp typ = LabItemTyp.TEXT;
			li = new LabItem(HL7Constants.COMMENT_CODE, HL7Constants.COMMENT_NAME, labor.getId(),
				"", "", //$NON-NLS-1$ //$NON-NLS-2$
				"", typ, HL7Constants.COMMENT_GROUP, Integer.toString(number)); //$NON-NLS-1$
		} else {
			li = list.get(0);
		}
		
		// add LabResult
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.add(LabResult.PATIENT_ID, Query.EQUALS, pat.getId());
		qr.add(LabResult.DATE, Query.EQUALS, commentsDate.toString(TimeTool.DATE_GER));
		qr.add(LabResult.ITEM_ID, Query.EQUALS, li.getId());
		if (qr.execute().size() == 0) {
			StringBuilder comment = new StringBuilder();
			if (hl7TextData.getText() != null) {
				comment.append(hl7TextData.getText());
			}
			if (hl7TextData.getComment() != null) {
				comment.append(hl7TextData.getComment());
			}
			// only add coments not yet existing
			LabResult labResult =
				new LabResult(pat, commentsDate, li, "text", comment.toString(), labor); //$NON-NLS-1$
			labResult.setObservationTime(commentsDate);
			
			CoreHub.getLocalLockService().acquireLock((LabResult) labResult);
			CoreHub.getLocalLockService().releaseLock((LabResult) labResult);
		}
	}
	
	@Override
	public void createDocumentManagerEntry(String title, String lab, byte[] data, String mimeType,
		TimeTool date, IPatient pat){
		Object os = Extensions.findBestService(GlobalServiceDescriptors.DOCUMENT_MANAGEMENT);
		if (os != null) {
			IDocumentManager docManager = (IDocumentManager) os;
			
			//find or create a category for this lab
			boolean catIsNotExisting = true;
			for (String cat : docManager.getCategories()) {
				if (cat.equals(lab)) {
					catIsNotExisting = false;
					break;
				}
			}
			
			if (catIsNotExisting) {
				docManager.addCategorie(lab);
			}
			
			// add document 
			try {
				Patient patient = Patient.load(pat.getId());
				docManager.addDocument(new GenericDocument(patient, title, lab, data,
					date.toString(TimeTool.DATE_GER), null, mimeType));
			} catch (IOException | ElexisException e) {
				logger.error("Error saving document received via hl7 in local document manager", e);
			}
		}
	}
	
	@Override
	public ILabResult createLabResult(IPatient patient, TimeTool date, ILabItem labItem,
		String result, String comment, String refVal, IContact origin, String subId,
		ILabOrder labOrder, String orderId, String mandantId, TimeTool time, String groupName){
		
		Patient pat = Patient.load(patient.getId());
		LabItem item = LabItem.load(labItem.getId());
		Labor labor = Labor.load(origin.getId());
		logger.debug("Creating result with patient [" + pat.getId() + "] labitem [" + item.getId()
			+ "] origin [" + labor.getId() + "]");
		
		LabResult labResult = LabResult.createLabResultAndAssertLabOrder(pat, date, item, result,
			comment, labor, refVal, labOrder, orderId, mandantId, time, groupName);
		
		if (subId != null) {
			labResult.setDetail(LabResult.EXTINFO_HL7_SUBID, subId);
		}
		// TODO LockHook too early
		return labResult;
	}
	
	@Override
	public void updateLabResult(ILabResult iLabResult, TransientLabResult transientLabResult){
		if (iLabResult != null) {
			((LabResult) iLabResult).setDetail(LabResult.EXTINFO_HL7_SUBID,
				transientLabResult.getSubId());
		}
	}
	
}
