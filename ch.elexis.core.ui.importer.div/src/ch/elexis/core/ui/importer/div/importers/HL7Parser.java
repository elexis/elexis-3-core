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
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.LabItem;
import ch.elexis.core.data.LabResult;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.importer.div.importers.dialog.QueryOverwriteDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class HL7Parser {
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
		Result<Kontakt> res = hl7.getLabor();
		if (!res.isOK()) {
			return new Result<Object>(Result.SEVERITY.ERROR, 1, Messages.HL7Parser_LabNotFound,
				hl7.getFilename(), true);
		}
		final Kontakt labor = res.get();
		Result<Object> r2 = hl7.getPatient(createPatientIfNotFound);
		if (!r2.isOK()) {
			return r2;
		}
		Patient pat = (Patient) r2.get();
		
		HL7.OBR obr = hl7.firstOBR();
		
		int nummer = 0;
		String dat = new TimeTool().toString(TimeTool.DATE_GER);
		while (obr != null) {
			boolean overWriteAll = false;
			HL7.OBX obx = obr.firstOBX();
			while (obx != null) {
				String itemname = obx.getItemName();
				Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
				qbe.add("LaborID", "=", labor.getId()); //$NON-NLS-1$ //$NON-NLS-2$
				// disabled, this would avoid renaming the title
				// qbe.add("titel", "=", itemname);
				qbe.add("kuerzel", "=", obx.getItemCode()); //$NON-NLS-1$ //$NON-NLS-2$
				List<LabItem> list = qbe.execute();
				LabItem li = null;
				if (list.size() < 1) {
					LabItem.typ typ = LabItem.typ.NUMERIC;
					if (obx.isFormattedText() || obx.isPlainText()) {
						typ = LabItem.typ.TEXT;
					}
					li =
						new LabItem(obx.getItemCode(), itemname, labor, obx.getRefRange(),
							obx.getRefRange(), obx.getUnits(), typ,
							Messages.HL7Parser_AutomaticAddedGroup + dat,
							Integer.toString(nummer++));
				} else {
					li = list.get(0);
				}
				LabResult lr;
				Query<LabResult> qr = new Query<LabResult>(LabResult.class);
				qr.add("PatientID", "=", pat.getId()); //$NON-NLS-1$ //$NON-NLS-2$
				qr.add("Datum", "=", obr.getDate().toString(TimeTool.DATE_GER)); //$NON-NLS-1$ //$NON-NLS-2$
				qr.add("ItemID", "=", li.getId()); //$NON-NLS-1$ //$NON-NLS-2$
				List<LabResult> qrr = qr.execute();
				if (qrr.size() != 0) {
					LabResult lrr = qrr.get(0);
					
					if (overWriteAll) {
						overWriteLabResult(lrr, obx);
						obx = obr.nextOBX(obx);
						continue;
					}
					
					int retVal;
					if (!testMode) {
						QueryOverwriteDialogRunnable runnable =
							new QueryOverwriteDialogRunnable(pat, lrr);
						Display.getDefault().syncExec(runnable);
						retVal = runnable.result;
					} else {
						retVal = IDialogConstants.YES_TO_ALL_ID;
					}
					
					if (retVal == IDialogConstants.YES_ID) {
						overWriteLabResult(lrr, obx);
						obx = obr.nextOBX(obx);
						continue;
					} else if (retVal == IDialogConstants.YES_TO_ALL_ID) {
						overWriteAll = true;
						overWriteLabResult(lrr, obx);
						obx = obr.nextOBX(obx);
						continue;
					} else {
						obx = obr.nextOBX(obx);
						continue;
					}
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
					lr = new LabResult(pat, obr.getDate(), li, "text", obx //$NON-NLS-1$
						.getResultValue() + "\n" + obx.getComment()); //$NON-NLS-1$
				} else {
					lr =
						new LabResult(pat, obr.getDate(), li, obx.getResultValue(),
							obx.getComment());
				}
				
				if (obx.isPathologic()) {
					lr.setFlag(LabResult.PATHOLOGIC, true);
				}
				obx = obr.nextOBX(obx);
			}
			obr = obr.nextOBR(obr);
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
					new LabResult(pat, commentsDate, li, "Text", comments); //$NON-NLS-1$
				}
			}
		}
		
		return new Result<Object>("OK"); //$NON-NLS-1$
	}
	
	/**
	 * Open overwrite dialog with a result value.
	 * 
	 * @author thomashu
	 */
	private class QueryOverwriteDialogRunnable implements Runnable {
		int result;
		private Patient pat;
		private LabResult lrr;
		
		public QueryOverwriteDialogRunnable(Patient pat, LabResult lrr){
			this.pat = pat;
			this.lrr = lrr;
		}
		
		@Override
		public void run(){
			QueryOverwriteDialog qod =
				new QueryOverwriteDialog(UiDesk.getTopShell(),
					Messages.HL7Parser_LabAlreadyImported + pat.getLabel(), lrr.getLabel()
						+ Messages.HL7Parser_AskOverwrite);
			result = qod.open();
		}
	}
	
	private void overWriteLabResult(LabResult labResult, HL7.OBX obx){
		// do some magic decision making if result is text ...
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
			labResult.set(LabResult.COMMENT, obx.getResultValue() + "\n" + obx.getComment());
		} else {
			labResult.set(LabResult.COMMENT, obx.getComment());
			labResult.set(LabResult.RESULT, obx.getResultValue());
		}
		
		if (obx.isPathologic()) {
			labResult.setFlag(LabResult.PATHOLOGIC, true);
		}
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
