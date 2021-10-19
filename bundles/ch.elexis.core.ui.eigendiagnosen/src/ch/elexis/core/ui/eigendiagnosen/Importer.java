/*******************************************************************************
 * Copyright (c) 2007-2018, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    T. Huster - updated
 *******************************************************************************/
package ch.elexis.core.ui.eigendiagnosen;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import com.opencsv.CSVReader;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.util.ResultAdapter;
import ch.elexis.core.importer.div.importers.ExcelWrapper;
import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.ui.util.ImporterPage;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public class Importer extends ImporterPage {
	/**
	 * Create the page that will let the user select a file to import. For simplicity, we use the
	 * default FileBasedImporter of our superclass.
	 */
	@Override
	public Composite createPage(final Composite parent){
		FileBasedImporter fbi = new FileBasedImporter(parent, this);
		fbi.setFilter(new String[] {
			"*.csv", "*.xls", "*" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}, new String[] {
			Messages.Eigendiagnosen_csvDescription, Messages.Eigendiagnosen_msExcelDescription,
			Messages.Eigendiagnosen_allFilesDescription
		});
		fbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return fbi;
	}
	
	/**
	 * The import process starts when the user has selected a file and clicked "OK". Warning: We can
	 * not read fields of the page created in createPage here! (The page is already disposed when
	 * doImport is called). If we have to transfer field values between createPage and doImport, we
	 * must override collect(). Our file based importer saves the user input in results[0]
	 */
	@Override
	public IStatus doImport(final IProgressMonitor monitor) throws Exception{
		File file = new File(results[0]);
		if (!file.canRead()) {
			log.log(Messages.Eigendiagnosen_CantRead + results[0], Log.ERRORS);
			return new Status(
				Status.ERROR,
				"ch.elexis.base.codeextension.eigendiagnosen", Messages.Eigendiagnosen_CantRead + results[0]); //$NON-NLS-1$
		}
		Result<String> res;
		if (results[0].endsWith(".xls")) { //$NON-NLS-1$
			res = importExcel(file.getAbsolutePath(), monitor);
		} else if (results[0].endsWith(".csv")) { //$NON-NLS-1$
			res = importCSV(file.getAbsolutePath(), monitor);
		} else {
			return new Status(
				Status.ERROR,
				"ch.elexis.base.codeextension.eigendiagnosen", Messages.Eigendiagnosen_UnsupportedFileFormat); //$NON-NLS-1$
		}
		if (res.isOK()) {
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD,
				IDiagnosisTree.class);
		}
		return ResultAdapter.getResultAsStatus(res);
	}
	
	/**
	 * return a description to display in the message area of the import dialog
	 */
	@Override
	public String getDescription(){
		return Messages.Eigendiagnosen_ImportFromCsvAndExcel;
	}
	
	/**
	 * return a title to display in the title bar of the import dialog
	 */
	@Override
	public String getTitle(){
		return Messages.Eigendiagnosen_CodeSystemName;
	}
	
	private Result<String> importExcel(final String file, final IProgressMonitor mon){
		ExcelWrapper xl = new ExcelWrapper();
		if (!xl.load(file, 0)) {
			return new Result<String>(Result.SEVERITY.ERROR, 1,
				Messages.Eigendiagnosen_BadFileFormat, file, true);
		}
		for (int i = xl.getFirstRow(); i <= xl.getLastRow(); i++) {
			List<String> row = xl.getRow(i);
			importLine(row.toArray(new String[0]));
		}
		return new Result<String>("OK"); //$NON-NLS-1$
	}
	
	private Result<String> importCSV(final String file, final IProgressMonitor mon){
		try {
			CSVReader cr = new CSVReader(new FileReader(file));
			String[] line;
			while ((line = cr.readNext()) != null) {
				importLine(line);
			}
			return new Result<String>("OK"); //$NON-NLS-1$
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return new Result<String>(Result.SEVERITY.ERROR, 1, Messages.Eigendiagnosen_CantRead
				+ file, ex.getMessage(), true);
		}
		
	}
	
	private void importLine(final String[] line){
		INamedQuery<IDiagnosisTree> query =
			ModelServiceHolder.get().getNamedQuery(IDiagnosisTree.class, "code");
		List<IDiagnosisTree> existing =
			query.executeWithParameters(query.getParameterMap("code", line[1]));
		IDiagnosisTree diag = null;
		if (!existing.isEmpty()) {
			diag = existing.get(0);
		} else {
			diag = ModelServiceHolder.get().create(IDiagnosisTree.class);
		}
		if (diag != null) {
			List<IDiagnosisTree> parent = query
				.executeWithParameters(query.getParameterMap("code", line[0]));
			if (!parent.isEmpty()) {
				diag.setParent(parent.get(0));
			}
			diag.setCode(line[1]);
			diag.setText(line[2]);
			diag.setDescription(line[3]);
			ModelServiceHolder.get().save(diag);
		}
	}
}
