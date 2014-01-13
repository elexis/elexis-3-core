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
import ch.elexis.core.ui.importer.div.importers.LabImportUtil.TransientLabResult;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class HL7Parser {
	private static final Logger logger = LoggerFactory.getLogger(HL7Parser.class);
	
	private static final String COMMENT_NAME = Messages.HL7Parser_CommentName;
	private static final String COMMENT_CODE = Messages.HL7Parser_CommentCode;
	private static final String COMMENT_GROUP = Messages.HL7Parser_CommentGroup;
	
	public String myLab = "?"; //$NON-NLS-1$
	private boolean testMode = false;
	
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
	
	public Result<Object> parse(final HL7 hl7, boolean createPatientIfNotFound){
		return parse(hl7, null, createPatientIfNotFound);
	}
	
	public Result<Object> parse(final HL7 hl7, ILabItemResolver labItemResolver,
		boolean createPatientIfNotFound){
		final TimeTool transmissionTime = new TimeTool();
		final Labor labor = LabImportUtil.getOrCreateLabor(myLab);
		Result<Object> r2 = hl7.getPatient(createPatientIfNotFound);
		if (!r2.isOK()) {
			return r2;
		}
		if (labItemResolver == null) {
			labItemResolver = new DefaultLabItemResolver();
		}
		Patient pat = (Patient) r2.get();
		
		HL7.OBR obr = hl7.firstOBR();
		
		int nummer = 0;
		List<TransientLabResult> results = new ArrayList<TransientLabResult>();
		while (obr != null) {
			HL7.OBX obx = obr.firstOBX();
			while (obx != null) {
				LabItem labItem = LabImportUtil.getLabItem(obx.getItemCode(), (Labor) labor);
				if (labItem == null) {
					LabItem.typ typ = LabItem.typ.NUMERIC;
					if (obx.isFormattedText() || obx.isPlainText()) {
						typ = LabItem.typ.TEXT;
					}
					labItem =
						new LabItem(obx.getItemCode(), labItemResolver.getTestName(obx), labor,
							obx.getRefRange(), obx.getRefRange(), obx.getUnits(), typ,
							labItemResolver.getTestGroupName(obx),
							labItemResolver.getNextTestGroupSequence(obx));
				}
				
				boolean importAsLongText = (obx.isFormattedText() || obx.isPlainText());
				if (importAsLongText) {
					if (obx.isNumeric())
						importAsLongText = false;
				}
				if (importAsLongText) {
					if (obx.getResultValue().length() < 20)
						importAsLongText = false;
				}
				if (importAsLongText) {
					TransientLabResult importedResult =
						new TransientLabResult.Builder(pat, labor, labItem, "text")
							.date(obr.getDate())
							.comment(obx.getResultValue() + "\n" + obx.getComment())
							.flags(obx.isPathologic() ? LabResult.PATHOLOGIC : 0)
							.unit(obx.getUnits()).ref(obx.getRefRange())
							.observationTime(obx.getObservationTime())
							.transmissionTime(transmissionTime).build();
					results.add(importedResult);
					logger.debug(importedResult.toString());
				} else {
					TransientLabResult importedResult =
						new TransientLabResult.Builder(pat, labor, labItem, obx.getResultValue())
							.date(obr.getDate()).comment(obx.getComment())
							.flags(obx.isPathologic() ? LabResult.PATHOLOGIC : 0)
							.unit(obx.getUnits()).ref(obx.getRefRange())
							.observationTime(obx.getObservationTime())
							.transmissionTime(transmissionTime).build();
					results.add(importedResult);
					logger.debug(importedResult.toString());
				}
				obx = obr.nextOBX(obx);
			}
			obr = obr.nextOBR(obr);
		}
		if (testMode) {
			LabImportUtil.importLabResults(results, new OverwriteAllImportUiHandler());
		} else {
			LabImportUtil.importLabResults(results, new DefaultLabImportUiHandler());
		}
		
		// add comments as a LabResult
		
		String comments = hl7.getComments();
		if (!StringTool.isNothing(comments)) {
			obr = hl7.firstOBR();
			if (obr != null) {
				TimeTool commentsDate = obr.getDate();
				
				// find LabItem
				Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
				qbe.add("LaborID", "=", labor.getId()); //$NON-NLS-1$ //$NON-NLS-2$
				qbe.add("titel", "=", COMMENT_NAME); //$NON-NLS-1$ //$NON-NLS-2$
				qbe.add("kuerzel", "=", COMMENT_CODE); //$NON-NLS-1$ //$NON-NLS-2$
				List<LabItem> list = qbe.execute();
				LabItem li = null;
				if (list.size() < 1) {
					// LabItem doesn't yet exist
					LabItem.typ typ = LabItem.typ.TEXT;
					li = new LabItem(COMMENT_CODE, COMMENT_NAME, labor, "", "", //$NON-NLS-1$ //$NON-NLS-2$
						"", typ, COMMENT_GROUP, Integer.toString(nummer++)); //$NON-NLS-1$
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
					new LabResult(pat, commentsDate, li, "Text", comments, labor); //$NON-NLS-1$
				}
			}
		}
		
		return new Result<Object>("OK"); //$NON-NLS-1$
	}
	
	/**
	 * Import the given HL7 file. Optionally, move the file into the given archive directory
	 * 
	 * @param file
	 *            the file to be imported (full path)
	 * @param archiveDir
	 *            a directory where the file should be moved to on success, or null if it should not
	 *            be moved.
	 * @return the result as type Result
	 */
	public Result<?> importFile(final File file, final File archiveDir,
		boolean bCreatePatientIfNotExists){
		HL7 hl7 = new HL7("Labor " + myLab, myLab); //$NON-NLS-1$
		Result<Object> r = hl7.load(file.getAbsolutePath());
		if (r.isOK()) {
			Result<?> ret = parse(hl7, bCreatePatientIfNotExists);
			// move result to archive
			if (ret.isOK()) {
				if (archiveDir != null) {
					if (archiveDir.exists() && archiveDir.isDirectory()) {
						if (file.exists() && file.isFile() && file.canRead()) {
							File newFile = new File(archiveDir, file.getName());
							if (!file.renameTo(newFile)) {
								SWTHelper.showError(Messages.HL7Parser_ErrorArchiving,
									Messages.HL7Parser_TheFile + file.getAbsolutePath()
										+ Messages.HL7Parser_CouldNotMoveToArchive);
							}
						}
					}
				}
			} else {
				ResultAdapter.displayResult(ret, Messages.HL7Parser_ErrorReading);
			}
			ElexisEventDispatcher.reload(LabItem.class);
			return ret;
		}
		return r;
		
	}
	
	/**
	 * Import the given HL7 file. Optionally, move the file into the given archive directory
	 * 
	 * @param file
	 *            the file to be imported (full path)
	 * @param archiveDir
	 *            a directory where the file should be moved to on success, or null if it should not
	 *            be moved.
	 * @return the result as type Result
	 */
	public Result<?> importFile(final File file, final File archiveDir, ILabItemResolver resolver,
		boolean bCreatePatientIfNotExists){
		HL7 hl7 = new HL7("Labor " + myLab, myLab); //$NON-NLS-1$
		Result<Object> r = hl7.load(file.getAbsolutePath());
		if (r.isOK()) {
			Result<?> ret = parse(hl7, resolver, bCreatePatientIfNotExists);
			// move result to archive
			if (ret.isOK()) {
				if (archiveDir != null) {
					if (archiveDir.exists() && archiveDir.isDirectory()) {
						if (file.exists() && file.isFile() && file.canRead()) {
							File newFile = new File(archiveDir, file.getName());
							if (!file.renameTo(newFile)) {
								SWTHelper.showError(Messages.HL7Parser_ErrorArchiving,
									Messages.HL7Parser_TheFile + file.getAbsolutePath()
										+ Messages.HL7Parser_CouldNotMoveToArchive);
							}
						}
					}
				}
			} else {
				ResultAdapter.displayResult(ret, Messages.HL7Parser_ErrorReading);
			}
			ElexisEventDispatcher.reload(LabItem.class);
			return ret;
		}
		return r;
		
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
}
