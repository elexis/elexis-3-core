/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.dialogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.LabResult;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.text.IOpaqueDocument;
import ch.elexis.core.data.services.GlobalServiceDescriptors;
import ch.elexis.core.data.services.IDocumentManager;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.icons.ImageSize;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.io.FileTool;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

public class DisplayLabDokumenteDialog extends TitleAreaDialog {
	private final String title;
	private final java.util.List<LabResult> labResultList;
	private TimeTool date = null;
	private IDocumentManager docManager;
	
	public DisplayLabDokumenteDialog(Shell parentShell, String _title,
		java.util.List<LabResult> _labResultList){
		super(parentShell);
		title = _title;
		labResultList = _labResultList;
		if (labResultList != null && labResultList.size() > 0) {
			date = new TimeTool(labResultList.get(0).getDate());
		}
		initDocumentManager();
	}
	
	/**
	 * Initialisiert document manager (omnivore) falls vorhanden
	 */
	private void initDocumentManager(){
		TimeTool today = new TimeTool();
		today.setTime(new Date());
		Object os = Extensions.findBestService(GlobalServiceDescriptors.DOCUMENT_MANAGEMENT);
		if (os != null) {
			this.docManager = (IDocumentManager) os;
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		final List list = new List(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(list);
		for (LabResult lr : this.labResultList) {
			list.add(lr.getResult());
		}
		
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (docManager != null && list.getSelectionCount() > 0) {
					openDocument(list.getSelection()[0]);
				}
			}
		});
		
		SWTHelper.center(UiDesk.getTopShell(), getShell());
		return composite;
	}
	
	@Override
	public int open(){
		if (this.labResultList != null && this.labResultList.size() == 1) {
			openDocument(this.labResultList.get(0).getResult());
			return OK;
		} else {
			return super.open();
		}
	}
	
	/**
	 * Opens a document in a system viewer
	 * 
	 * @param document
	 */
	private void openDocument(String docName){
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		try {
			if (this.docManager != null) {
				java.util.List<IOpaqueDocument> documentList =
					this.docManager.listDocuments(patient, null, docName, null, new TimeSpan(
						this.date, this.date), null);
				if (documentList == null || documentList.size() == 0) {
					throw new IOException(MessageFormat.format("Dokument {0} nicht vorhanden!",
						docName));
				}
				int counter = 0;
				for (IOpaqueDocument document : documentList) {
					String ext = FileTool.getExtension(docName);
					File temp = File.createTempFile("lab" + counter, "doc." + ext); //$NON-NLS-1$ //$NON-NLS-2$
					temp.deleteOnExit();
					byte[] b = document.getContentsAsBytes();
					if (b == null) {
						throw new IOException("Dokument ist leer!");
					}
					FileOutputStream fos = new FileOutputStream(temp);
					fos.write(b);
					fos.close();
					Program proggie = Program.findProgram(FileTool.getExtension(ext));
					if (proggie != null) {
						proggie.execute(temp.getAbsolutePath());
					} else {
						if (Program.launch(temp.getAbsolutePath()) == false) {
							Runtime.getRuntime().exec(temp.getAbsolutePath());
						}
					}
					counter++;
				}
			}
		} catch (Exception ex) {
			SWTHelper.showError("Fehler beim Öffnen des Dokumentes", ex.getMessage());
		}
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(this.title);
		setTitle(ElexisEventDispatcher.getSelectedPatient().getLabel());
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		SWTHelper.center(getShell());
	}
	
}
