/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    Niklaus Giger - moved doc -> doc/import.textile
 * 
 *******************************************************************************/

package ch.elexis.core.ui.importer.div.importers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.core.ui.exchange.KontaktMatcher.CreateMode;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Person;
import ch.rgw.tools.BinConverter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VCard;

/**
 * See also doc/import.textile
 * 
 * @author Gerry
 * 
 */
public class KontaktImporterBlatt extends Composite {
	String filename;
	Label lbFileName;
	Combo cbMethods;
	boolean bKeepID, mediportInsuererList;
	int method;
	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	static final String[] methods = new String[] {
		"XLS", "CSV", Messages.KontaktImporterBlatt_kklistHeading}; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String PRESET_RUSSI = "e3ad14dc49e27dbcc4771b41b34cdd902f9cfcc6"; //$NON-NLS-1$
	private static final String PRESET_UNIVERSAL = "275789de20bc918890cc753c49931e72166a4bc0"; //$NON-NLS-1$
	private static final String PRESET_HERTEL = "a4a9f3bd410443399ee05d5e033d94513a64239b"; //$NON-NLS-1$
	
	public KontaktImporterBlatt(final Composite parent){
		super(parent, SWT.NONE);
		setLayout(new GridLayout(2, false));
		new Label(this, SWT.NONE).setText(Messages.KontaktImporterBlatt_DateiTyp);
		new Label(this, SWT.NONE).setText(Messages.KontaktImporterBlatt_Datei);
		cbMethods = new Combo(this, SWT.SINGLE);
		cbMethods.setItems(methods);
		cbMethods.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent arg0){
				method = cbMethods.getSelectionIndex();
			}
		});
		cbMethods.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button bLoad = new Button(this, SWT.PUSH);
		
		bLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				String file = fd.open();
				lbFileName.setText(file == null ? "" : file); //$NON-NLS-1$
				filename = lbFileName.getText();
			}
		});
		bLoad.setText(Messages.KontaktImporterBlatt_ChoseFile);
		lbFileName = new Label(this, SWT.NONE);
		bLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lbFileName.setText(Messages.KontaktImporterBlatt_PleaseChooseTypeAndFile);
		lbFileName.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		
		final Button btnMediportInsuranceList = new Button(this, SWT.CHECK);
		btnMediportInsuranceList.setText(Messages.KontaktImporterBlatt_MediportInsurer);
		btnMediportInsuranceList.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		btnMediportInsuranceList.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				mediportInsuererList = btnMediportInsuranceList.getSelection();
			}
		});
		
		final Button bKeep = new Button(this, SWT.CHECK);
		bKeep.setText(Messages.KontaktImporterBlatt_KeepID);
		bKeep.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		bKeep.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				bKeepID = bKeep.getSelection();
			}
			
		});
	}
	
	public boolean doImport(final IProgressMonitor moni){
		if (filename != null && filename.length() > 0) {
			switch (method) {
			case 0:
				return importExcel(filename, moni);
			case 1:
				return importCSV(filename);
			case 2:
				return importKK(filename);
			}
		}
		return false;
	}
	
	public boolean importKK(final String file){
		ExcelWrapper exw = new ExcelWrapper();
		exw.setFieldTypes(new Class[] {
			Integer.class, String.class, String.class, String.class, String.class, Integer.class,
			Integer.class
		});
		exw.load(file, 0);
		
		String[] row;
		for (int i = exw.getFirstRow() + 1; i <= exw.getLastRow(); i++) {
			row = exw.getRow(i).toArray(new String[0]);
			if (row == null) {
				continue;
			}
			if (row.length != 7) {
				continue;
			}
			log.info(Messages.KontaktImporterBlatt_Importing + StringTool.join(row, " "));
			// Please keep in sync with doc/import.textile !!
			String bagnr = StringTool.getSafe(row, 0);
			String name = StringTool.getSafe(row, 1);
			String zweig = StringTool.getSafe(row, 2);
			String adresse = StringTool.getSafe(row, 3);
			String typ = StringTool.getSafe(row, 4);
			String EANInsurance = StringTool.getSafe(row, 5);
			String EANReceiver = StringTool.getSafe(row, 6);
			String[] adr = splitAdress(adresse);
			Organisation kk =
				KontaktMatcher.findOrganisation(name, null, adr[0], adr[1], adr[2],
					CreateMode.CREATE);
			if (kk == null) {
				return false;
			}
			kk.setInfoElement("EAN", EANInsurance); //$NON-NLS-1$
			kk.setInfoElement("BAGNr", bagnr); //$NON-NLS-1$
			kk.set("Bezeichnung2", zweig); //$NON-NLS-1$
			kk.set("Kuerzel", StringTool.limitLength(Messages.KontaktImporterBlatt_KKKuerzel //$NON-NLS-1$
				+ StringTool.getFirstWord(name), 39));
		}
		return true;
	}
	
	String[] splitAdress(final String adr){
		String[] ret = new String[3];
		String[] m1 = adr.split("\\s*,\\s*"); //$NON-NLS-1$
		String[] plzOrt = m1[m1.length - 1].split(" ", 2); //$NON-NLS-1$
		if (m1.length == 1) {
			ret[0] = ""; //$NON-NLS-1$
			
		} else {
			ret[0] = m1[0];
		}
		ret[1] = plzOrt[0];
		ret[2] = plzOrt.length > 1 ? plzOrt[1] : ""; //$NON-NLS-1$
		return ret;
	}
	
	public boolean importExcel(final String file, final IProgressMonitor moni){
		ExcelWrapper exw = new ExcelWrapper();
		exw.load(file, 0);
		// Please keep in sync with doc/import.textile !!
		List<String> row = exw.getRow(exw.getFirstRow()); // we load the first
		// row to figure out
		// whether we know
		// the format
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1"); //$NON-NLS-1$
			for (String field : row) {
				digest.update(field.getBytes("iso-8859-1")); //$NON-NLS-1$
			}
			byte[] dg = digest.digest();
			String vgl = BinConverter.bytesToHexStr(dg);
			log.info(Messages.KontaktImporterBlatt_Importing + " SHA1 war " + vgl + //$NON-NLS-1$
				"\nFirst row was: " + row); //$NON-NLS-1$
			
			if (vgl.equals(PRESET_RUSSI)) {
				return Presets.importRussi(exw, bKeepID, moni);
			} else if (vgl.equals(PRESET_UNIVERSAL)) {
				return Presets.importUniversal(exw, bKeepID, moni);
			} else if (vgl.equals(PRESET_HERTEL)) {
				return Presets.importHertel(exw, bKeepID, moni);
			} else {
				SWTHelper.showError(Messages.KontaktImporterBlatt_DatatypeErrorHeading,
					Messages.KontaktImporterBlatt_DatatypeErrorText,
					Messages.KontaktImporterBlatt_DatatypeErrorExplanation + " SHA1 was " + vgl); //$NON-NLS-1$
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		
		return false;
		
	}
	
	public boolean importXML(final String file){
		// Please keep in sync with doc/import.textile !!
		SWTHelper.showError(Messages.KontaktImporterBlatt_DatatypeErrorHeading,
			Messages.KontaktImporterBlatt_DatatypeErrorText,
			Messages.KontaktImporterBlatt_xmlImportNotSupported);
		return false;
	}
	
	public boolean importCSV(final String file){
		// Please keep in sync with doc/import.textile !!
		if (mediportInsuererList) {
			if (file == null) {
				log.warn("No file selected");
				SWTHelper.showError(Messages.KontaktImporterBlatt_ChoseFile,
					Messages.KontaktImporterBlatt_ChoseFile,
					Messages.KontaktImporterBlatt_PleaseChooseTypeAndFile);
				return false;
			}
			
			try {
				// check if it's actually a csv
				String filename = file.toLowerCase();
				if (!filename.endsWith("csv")) {
					SWTHelper.showError(Messages.KontaktImporterBlatt_DateiTyp, MessageFormat
						.format(Messages.KontaktImporterBlatt_DatatypeErrorNoCSV, file));
					return false;
				}
				
				// read csv file
				List<Organisation> importedInsurer =
					MediportInsurerImporter.importCSVFromStream(new FileInputStream(file));
				SWTHelper.showInfo(Messages.KontaktImporterBlatt_csvImportMediportInsurerDone,
					MessageFormat.format(Messages.KontaktImporterBlatt_csvImportMediportInsurerMsg,
						importedInsurer.size()));
				return true;
			} catch (FileNotFoundException e) {
				log.error("Error parsing expected mediport insurer csv file [" + file + "]", e);
				return false;
			}
		} else {
			SWTHelper.showError(Messages.KontaktImporterBlatt_DatatypeErrorHeading,
				Messages.KontaktImporterBlatt_DatatypeErrorText,
				Messages.KontaktImporterBlatt_csvImportNotSupported);
			return false;
		}
	}
	
	public boolean importVCard(final String file){
		try {
			// Please keep in sync with doc/import.textile !!
			VCard vcard = new VCard(new FileInputStream(file));
			String name, vorname, tel, email, title;
			String gebdat = ""; //$NON-NLS-1$
			String strasse = ""; //$NON-NLS-1$
			String plz = ""; //$NON-NLS-1$
			String ort = ""; //$NON-NLS-1$
			String fqname = vcard.getElement("N"); //$NON-NLS-1$
			if (fqname == null) {
				return false;
			}
			String[] names = vcard.getValue(fqname).split(";"); //$NON-NLS-1$
			email = vcard.getElementValue("EMAIL"); //$NON-NLS-1$
			String address = vcard.getElementValue("ADR"); //$NON-NLS-1$
			title = vcard.getElementValue("TITLE"); //$NON-NLS-1$
			tel = vcard.getElementValue("TEL"); //$NON-NLS-1$
			if (address != null) {
				String[] adr = address.split(";"); //$NON-NLS-1$
				strasse = adr[2];
				plz = adr[5];
				ort = adr[3];
			}
			name = names[0];
			vorname = names[1];
			Kontakt k = KontaktImporter.queryKontakt(name, vorname, strasse, plz, ort, false);
			if (k == null) {
				k = new Person(name, vorname, gebdat, Person.MALE);
				k.set("Title", title); //$NON-NLS-1$
			}
			return true;
		} catch (Exception ex) {
			
		}
		return false;
	}
}
