/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.importer.div.importers;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.importer.div.importers.LabImportUtil.TransientLabResult;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.HL7ReaderFactory;
import ch.elexis.hl7.model.IValueType;
import ch.elexis.hl7.model.LabResultData;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.TextData;
import ch.elexis.hl7.v26.HL7Constants;
import ch.elexis.hl7.v26.Messages;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.TimeTool;

public class HL7Parser {
	private static final Logger logger = LoggerFactory.getLogger(HL7Parser.class);
	
	private ImporterPatientResolver patientResolver = new ImporterPatientResolver();
	
	public String myLab = "?"; //$NON-NLS-1$
	private boolean testMode = false;
	public HL7Reader hl7Reader;
	private Patient pat;
	private TimeTool date;
	
	public HL7Parser(String mylab){
		myLab = mylab;
	}
	
	/**
	 * Method sets testMode which will prevent dialogs from opening. Should only be used by unit
	 * tests.
	 * 
	 * @param value
	 */
	public void setTestMode(boolean value){
		testMode = value;
	}
	
	public Result<Object> parse(final HL7Reader hl7Reader, boolean createPatientIfNotFound){
		return parse(hl7Reader, null, createPatientIfNotFound);
	}
	
	public Result<Object> parse(final HL7Reader hl7Reader, ILabItemResolver labItemResolver,
		boolean createPatientIfNotFound){
		return parse(hl7Reader, null, null, createPatientIfNotFound);
	}
	
	public Result<Object> parse(final HL7Reader hl7Reader, ILabItemResolver labItemResolver,
		ILabContactResolver labContactResolver, boolean createPatientIfNotFound){
		final TimeTool transmissionTime = new TimeTool();
		
		// assure resolvers are initialized
		if (labContactResolver == null) {
			labContactResolver = new DefaultLabContactResolver();
		}
		if (labItemResolver == null) {
			labItemResolver = new DefaultLabItemResolver();
		}
		
		try {
			Labor labor = labContactResolver.getLabContact(myLab, hl7Reader.getSender());
			// stop here if lab does not exist
			if (labor == null) {
				logger.info("Exiting parsing process as labor is null");
				return new Result<Object>("OK");
			}
			
			ObservationMessage obsMessage =
				hl7Reader.readObservation(patientResolver, createPatientIfNotFound);
			
			pat = hl7Reader.getPatient();
			if (pat == null) {
				return new Result<Object>(SEVERITY.ERROR, 2,
					Messages.getString("HL7_PatientNotInDatabase"), obsMessage.getPatientId(), true);
			}
			
			int number = 0;
			List<TransientLabResult> results = new ArrayList<TransientLabResult>();
			List<IValueType> observations = obsMessage.getObservations();
			
			for (IValueType iValueType : observations) {
				if (iValueType instanceof LabResultData) {
					LabResultData hl7LabResult = (LabResultData) iValueType;
					if (hl7LabResult.getDate() == null) {
						hl7LabResult.setDate(transmissionTime.getTime());
					}
					date = new TimeTool(hl7LabResult.getDate());
					if (hl7LabResult.getAlternativeDateTime() == null) {
						hl7LabResult.setAlternativeDateTime(transmissionTime
							.toString(TimeTool.TIMESTAMP));
					}
					
					LabItem labItem =
						LabImportUtil.getLabItem(hl7LabResult.getCode(), (Labor) labor);
					if (labItem == null) {
						LabItem.typ typ = LabItem.typ.NUMERIC;
						if (hl7LabResult.isNumeric() == false) {
							typ = LabItem.typ.TEXT;
						}
						labItem =
							new LabItem(hl7LabResult.getCode(), hl7LabResult.getName(), labor,
								hl7LabResult.getRange(), hl7LabResult.getRange(),
								hl7LabResult.getUnit(), typ, hl7LabResult.getGroup(),
								hl7LabResult.getSequence());
					}
					
					boolean importAsLongText =
						(hl7LabResult.isFormatedText() || hl7LabResult.isPlainText());
					if (importAsLongText) {
						if (hl7LabResult.isNumeric()) {
							importAsLongText = false;
						}
					}
					if (importAsLongText) {
						if (hl7LabResult.getValue().length() < 20) {
							importAsLongText = false;
						}
					}
					if (importAsLongText) {
						TimeTool baseDateTime = new TimeTool(hl7LabResult.getAlternativeDateTime());
						TimeTool obsDateTime = new TimeTool(hl7LabResult.getDate());
						TransientLabResult importedResult =
							new TransientLabResult.Builder(pat, labor, labItem, "text")
								.date(baseDateTime)
								.comment(hl7LabResult.getValue() + "\n" + hl7LabResult.getComment())
								.flags(hl7LabResult.isFlagged() ? LabResult.PATHOLOGIC : 0)
								.unit(hl7LabResult.getUnit()).ref(hl7LabResult.getRange())
								.observationTime(obsDateTime).transmissionTime(transmissionTime)
								.build();
						results.add(importedResult);
						logger.debug(importedResult.toString());
					} else {
						TimeTool baseDateTime = new TimeTool(hl7LabResult.getAlternativeDateTime());
						TimeTool obsDateTime = new TimeTool(hl7LabResult.getDate());
						TransientLabResult importedResult =
							new TransientLabResult.Builder(pat, labor, labItem,
								hl7LabResult.getValue()).date(baseDateTime)
								.comment(hl7LabResult.getComment())
								.flags(hl7LabResult.isFlagged() ? LabResult.PATHOLOGIC : 0)
								.unit(hl7LabResult.getUnit()).ref(hl7LabResult.getRange())
								.observationTime(obsDateTime).transmissionTime(transmissionTime)
								.build();
						results.add(importedResult);
						logger.debug(importedResult.toString());
					}
				}
				
				if (iValueType instanceof TextData) {
					TextData hl7TextData = (TextData) iValueType;
					
					// add comments as a LabResult
					if (hl7TextData.getName().equals(HL7Constants.COMMENT_NAME)) {
						createCommentsLabResult(hl7TextData, pat, labor, number);
						number++;
					}
				}
			}
			
			if (testMode) {
				LabImportUtil.importLabResults(results, new OverwriteAllImportUiHandler());
			} else {
				LabImportUtil.importLabResults(results, new DefaultLabImportUiHandler());
			}
		} catch (ElexisException e) {
			logger.error("Parsing HL7 failed", e);
			return new Result<Object>(SEVERITY.ERROR, 2,
				Messages.getString("HL7_ExceptionWhileProcessingData"), e.getMessage(), true);
		}
		
		return new Result<Object>("OK"); //$NON-NLS-1$
	}
	
	private void createCommentsLabResult(TextData hl7TextData, Patient pat, Labor labor, int number){
		if (hl7TextData.getDate() == null) {
			hl7TextData.setDate(new TimeTool().getTime());
		}
		TimeTool commentsDate = new TimeTool(hl7TextData.getDate());
		
		// find LabItem
		Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
		qbe.add("LaborID", "=", labor.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		qbe.add("titel", "=", HL7Constants.COMMENT_NAME); //$NON-NLS-1$ //$NON-NLS-2$
		qbe.add("kuerzel", "=", HL7Constants.COMMENT_CODE); //$NON-NLS-1$ //$NON-NLS-2$
		List<LabItem> list = qbe.execute();
		LabItem li = null;
		if (list.size() < 1) {
			// LabItem doesn't yet exist
			LabItem.typ typ = LabItem.typ.TEXT;
			li = new LabItem(HL7Constants.COMMENT_CODE, HL7Constants.COMMENT_NAME, labor, "", "", //$NON-NLS-1$ //$NON-NLS-2$
				"", typ, HL7Constants.COMMENT_GROUP, Integer.toString(number)); //$NON-NLS-1$
		} else {
			li = list.get(0);
		}
		
		// add LabResult
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.add("PatientID", "=", pat.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		qr.add("Datum", "=", commentsDate.toString(TimeTool.DATE_GER)); //$NON-NLS-1$ //$NON-NLS-2$
		qr.add("ItemID", "=", li.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		if (qr.execute().size() == 0) {
			// only add coments not yet existing
			new LabResult(pat, commentsDate, li, "Text", hl7TextData.getComment(), labor); //$NON-NLS-1$
		}
	}
	
	/**
	 * @see HL7Parser#importFile(File, File, ILabItemResolver, ILabContactResolver, boolean)
	 */
	public Result<?> importFile(final File file, final File archiveDir,
		boolean bCreatePatientIfNotExists){
		return importFile(file, archiveDir, null, null, bCreatePatientIfNotExists);
	}
	
	/**
	 * @see HL7Parser#importFile(File, File, ILabItemResolver, ILabContactResolver, boolean)
	 */
	public Result<?> importFile(final File file, final File archiveDir,
		ILabItemResolver labItemResolver, boolean bCreatePatientIfNotExists){
		return importFile(file, archiveDir, labItemResolver, null, bCreatePatientIfNotExists);
	}
	
	/**
	 * Import the given HL7 file. Optionally, move the file into the given archive directory
	 * 
	 * @param file
	 *            the file to be imported (full path)
	 * @param archiveDir
	 *            a directory where the file should be moved to on success, or null if it should not
	 *            be moved.
	 * @param labItemResolver
	 *            implementation of the {@link ILabItemResolver}, or null if
	 *            {@link DefaultLabItemResolver} should be used
	 * @param labContactResolver
	 *            implementation of {@link ILabContactResolver}, or null if
	 *            {@link DefaultLabContactResovler} should be used
	 * @param bCreatePatientIfNotExists
	 *            indicates whether a patient should be created if not existing
	 * @return the result as type Result
	 */
	public Result<?> importFile(final File file, final File archiveDir,
		ILabItemResolver labItemResolver, ILabContactResolver labContactResolver,
		boolean bCreatePatientIfNotExists){
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(file);
		
		for (HL7Reader hl7Reader : hl7Readers) {
			this.hl7Reader = hl7Reader;
			Result<?> ret =
				parse(hl7Reader, labItemResolver, labContactResolver, bCreatePatientIfNotExists);
			// move result to archive
			if (ret.isOK()) {
				if (archiveDir != null) {
					if (archiveDir.exists() && archiveDir.isDirectory()) {
						if (file.exists() && file.isFile() && file.canRead()) {
							File newFile = new File(archiveDir, file.getName());
							if (!file.renameTo(newFile)) {
								SWTHelper
									.showError(
										ch.elexis.core.ui.importer.div.importers.Messages.HL7Parser_ErrorArchiving,
										ch.elexis.core.ui.importer.div.importers.Messages.HL7Parser_TheFile
											+ file.getAbsolutePath()
											+ ch.elexis.core.ui.importer.div.importers.Messages.HL7Parser_CouldNotMoveToArchive);
							}
						}
					}
				}
			} else {
				ResultAdapter.displayResult(ret,
					ch.elexis.core.ui.importer.div.importers.Messages.HL7Parser_ErrorReading);
			}
			ElexisEventDispatcher.reload(LabItem.class);
			return ret;
		}
		return new Result<Object>("OK"); //$NON-NLS-1$
	}
	
	public void importFromDir(final File dir, final File archiveDir, Result<?> res,
		boolean bCreatePatientIfNotExists){
		File[] files = dir.listFiles(new FileFilter() {
			
			public boolean accept(File pathname){
				if (pathname.isDirectory()) {
					if (!pathname.getName().equalsIgnoreCase(archiveDir.getName())) {
						return true;
					}
				} else {
					if (pathname.getName().toLowerCase().endsWith(".hl7")) { //$NON-NLS-1$
						return true;
					}
				}
				return false;
			}
		});
		for (File file : files) {
			if (file.isDirectory()) {
				importFromDir(file, archiveDir, res, bCreatePatientIfNotExists);
			} else {
				Result<?> r = importFile(file, archiveDir, bCreatePatientIfNotExists);
				if (res == null) {
					res = r;
				} else {
					res.add(r.getSeverity(), 1, "", null, true); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * Equivalent to importFile(new File(file), null)
	 * 
	 * @param filepath
	 *            the file to be imported (full path)
	 * @return
	 */
	public Result<?> importFile(final String filepath, boolean bCreatePatientIfNotExists){
		return importFile(new File(filepath), null, bCreatePatientIfNotExists);
	}
	
	public Result<?> importMessage(String message, boolean bCreatePatientIfNotExists){
		HL7Reader hl7Reader = HL7ReaderFactory.INSTANCE.getReader(message);
		this.hl7Reader = hl7Reader;
		Result<?> ret = parse(hl7Reader, bCreatePatientIfNotExists);
		if (ret.isOK()) {
			return new Result<Object>("OK"); //$NON-NLS-1$
		} else {
			ResultAdapter.displayResult(ret,
				ch.elexis.core.ui.importer.div.importers.Messages.HL7Parser_ErrorReading);
		}
		ElexisEventDispatcher.reload(LabItem.class);
		return ret;
	}
	
	public Patient getPatient(){
		return pat;
	}
	
	public void setPatient(Patient pat){
		this.pat = pat;
	}
	
	public TimeTool getDate(){
		return date;
	}
	
	public void setDate(TimeTool date){
		this.date = date;
	}
}
