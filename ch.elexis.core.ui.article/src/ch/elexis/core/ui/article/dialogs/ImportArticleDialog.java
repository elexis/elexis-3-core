/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.article.dialogs;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import ch.elexis.core.importer.div.importers.ExcelWrapper;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Query;
import ch.elexis.data.Stock;

public class ImportArticleDialog extends TitleAreaDialog {

	private ComboViewer comboStockType;
	private Text tFilePath;
	
	public ImportArticleDialog(Shell parentShell){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(3, false));
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(ret, SWT.NONE).setText("Import in Lager");
		
		comboStockType = new ComboViewer(ret, SWT.BORDER | SWT.READ_ONLY);
		comboStockType.setContentProvider(ArrayContentProvider.getInstance());
		comboStockType.setInput(new Query<Stock>(Stock.class).execute());
		comboStockType.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					if (!selection.isEmpty()) {
						Object o = ((StructuredSelection) selection).getFirstElement();
					}
				}
			}
		});
		comboStockType.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof Stock) {
					Stock stock = (Stock) element;
					return stock.getLabel();
				}
				return super.getText(element);
			}
		});
		comboStockType.getCombo().setLayoutData(SWTHelper.getFillGridData(2, false, 1, false));
		
		comboStockType.setSelection(new StructuredSelection(Stock.load(Stock.DEFAULT_STOCK_ID)));
		
		new Label(ret, SWT.NONE).setText("Quelldatei auswählen");
		
		tFilePath = new Text(ret, SWT.BORDER);
		tFilePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tFilePath.setText("");
		Button btnBrowse = new Button(ret, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setFilterExtensions(new String[] {
					"*.xlsx", "*.*"
				});
				String selected = fd.open();
				tFilePath.setText(selected);
			}
		});
		btnBrowse.setText("auswählen..");
		
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Artikel Importieren"); //$NON-NLS-1$
		setMessage("Bitte wählen Sie die Quelle aus, aus dem Sie die Artikel importieren möchten."); //$NON-NLS-1$
		getShell().setText("Artikel Import"); //$NON-NLS-1$
		getShell().setImage(Images.IMG_PILL.getImage());
	}
	
	@Override
	protected void okPressed(){
		String path = tFilePath.getText();
		if (path != null && !path.isEmpty()) {
			try (FileInputStream is = new FileInputStream(tFilePath.getText())) {
				ExcelWrapper xl = new ExcelWrapper();
				if (xl.load(is, 0)) {
					ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell());
					try {
						progress.run(true, true, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor){
								monitor.beginTask("Artikel in Lager Import", 100);
								
								for (int i = xl.getFirstRow(); i <= xl.getLastRow(); i++) {
									List<String> row = xl.getRow(i);
									System.out.println(row.toArray(new String[0]));
									monitor.worked(1);
									if (monitor.isCanceled()) {
										break;
									}
									//Excelwrapper verwendet POI 3.0.2-> POIFSFileSystem only for 2003 --->Apache POI XSLX support ab >= 3.5 ->   Workbook workbook = new XSSFWorkbook(excelFile);
								}
							}
							
						});
					} catch (InvocationTargetException | InterruptedException e) {
						LoggerFactory.getLogger(ImportArticleDialog.class)
							.warn("Exception during article to lager import.", e);
					}
				}
			} catch (IOException e) {
				MessageDialog.openError(getShell(), "Import error",
					"Import fehlgeschlagen.\nDatei nicht lesbar: " + path);
				LoggerFactory.getLogger(ImportArticleDialog.class)
					.error("cannot import file at " + path,
					e);
			}
		}
		
		MessageDialog.openInformation(getShell(), "Import Ergebnis",
			"0 Einträge wurden importiert.\n0 Fehler");
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		super.createButtonsForButtonBar(parent);
		
		Button okBtn = super.getButton(IDialogConstants.OK_ID);
		if (okBtn != null) {
			okBtn.setText("Import");
		}
		
		Button closeBtn = super.getButton(IDialogConstants.CANCEL_ID);
		if (closeBtn != null) {
			closeBtn.setText("Schließen");
		}
	}
}
