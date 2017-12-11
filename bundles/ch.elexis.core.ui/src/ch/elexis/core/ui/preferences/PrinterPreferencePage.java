/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class PrinterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String ARROW = "->"; //$NON-NLS-1$
	private static final String PRINTERWITH = Messages.PrinterPreferencePage_PrinterWith;
	private static final String TRAYFOR = Messages.PrinterPreferencePage_TrayFor;
	private static final String LABELS = Messages.PrinterPreferencePage_Labels;
	private static final String PAPER_ESR = Messages.PrinterPreferencePage_PaperWithESR;
	private static final String PAPER_PLAIN_A4 = Messages.PrinterPreferencePage_PaperA4Plain;
	private static final String PAPER_PLAIN_A5 = Messages.PrinterPreferencePage_PaperA5Plain;
	private static final String SHEETFEEDER = Messages.PrinterPreferencePage_SheetFeeder;
	
	Text tEtiketten, tEtikettenschacht, tA5, tA5Schacht, tA4ESR, tA4ESRSchacht, tA4, tA4Schacht;
	Text tEinzelblatt;
	Text tEinzelblattSchacht;
	Button bEtiketten;
	Button cEtiketten;
	Button bClear;
	PrinterSelector psel;
	
	@Override
	protected Control createContents(Composite parent){
		psel = new PrinterSelector();
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(3, false));
		new Label(ret, SWT.NONE).setText(PRINTERWITH + LABELS);
		tEtiketten = new Text(ret, SWT.BORDER | SWT.READ_ONLY);
		tEtiketten.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tEtiketten.setData(Messages.PrinterPreferencePage_Labelrinter);
		bEtiketten = new Button(ret, SWT.PUSH);
		bEtiketten.setText(" ->"); //$NON-NLS-1$
		bEtiketten.setData(tEtiketten);
		bEtiketten.addSelectionListener(psel);
		new Label(ret, SWT.NONE).setText(TRAYFOR + LABELS);
		tEtikettenschacht = new Text(ret, SWT.BORDER);
		tEtikettenschacht.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		cEtiketten = new Button(ret, SWT.CHECK);
		cEtiketten.setText(Messages.PrinterPreferencePage_ChosePrinterAlways);
		cEtiketten.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		cEtiketten.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				setEtikettenSelection(cEtiketten.getSelection());
			}
		});
		
		new Label(ret, SWT.NONE).setText(PRINTERWITH + PAPER_ESR);
		tA4ESR = new Text(ret, SWT.BORDER | SWT.READ_ONLY);
		tA4ESR.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button b = new Button(ret, SWT.PUSH);
		b.setData(tA4ESR);
		b.addSelectionListener(psel);
		b.setText(ARROW);
		new Label(ret, SWT.NONE).setText(TRAYFOR + PAPER_ESR);
		tA4ESRSchacht = new Text(ret, SWT.BORDER);
		tA4ESRSchacht.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE);
		
		new Label(ret, SWT.NONE).setText(PRINTERWITH + PAPER_PLAIN_A4);
		tA4 = new Text(ret, SWT.BORDER | SWT.READ_ONLY);
		tA4.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		b = new Button(ret, SWT.PUSH);
		b.setData(tA4);
		b.addSelectionListener(psel);
		b.setText(ARROW);
		new Label(ret, SWT.NONE).setText(TRAYFOR + PAPER_PLAIN_A4);
		tA4Schacht = new Text(ret, SWT.BORDER);
		tA4Schacht.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE);
		
		new Label(ret, SWT.NONE).setText(PRINTERWITH + PAPER_PLAIN_A5);
		tA5 = new Text(ret, SWT.BORDER | SWT.READ_ONLY);
		tA5.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		b = new Button(ret, SWT.PUSH);
		b.setData(tA5);
		b.addSelectionListener(psel);
		b.setText(ARROW);
		new Label(ret, SWT.NONE).setText(TRAYFOR + PAPER_PLAIN_A5);
		tA5Schacht = new Text(ret, SWT.BORDER);
		tA5Schacht.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE);
		
		new Label(ret, SWT.NONE).setText(PRINTERWITH + SHEETFEEDER);
		tEinzelblatt = new Text(ret, SWT.BORDER | SWT.READ_ONLY);
		tEinzelblatt.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		b = new Button(ret, SWT.PUSH);
		b.setData(tEinzelblatt);
		b.addSelectionListener(psel);
		b.setText(ARROW);
		new Label(ret, SWT.NONE).setText(TRAYFOR + SHEETFEEDER);
		tEinzelblattSchacht = new Text(ret, SWT.BORDER);
		tEinzelblattSchacht.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE);
		
		tEtiketten.setText(CoreHub.localCfg.get("Drucker/Etiketten/Name", StringTool.leer)); //$NON-NLS-1$
		tEtikettenschacht.setText(CoreHub.localCfg
			.get("Drucker/Etiketten/Schacht", StringTool.leer)); //$NON-NLS-1$
		boolean selection = CoreHub.localCfg.get("Drucker/Etiketten/Choose", false); //$NON-NLS-1$
		cEtiketten.setSelection(selection); //$NON-NLS-1$
		setEtikettenSelection(selection);
		tA4ESR.setText(CoreHub.localCfg.get("Drucker/A4ESR/Name", StringTool.leer)); //$NON-NLS-1$
		tA4ESRSchacht.setText(CoreHub.localCfg.get("Drucker/A4ESR/Schacht", StringTool.leer)); //$NON-NLS-1$
		tA4.setText(CoreHub.localCfg.get("Drucker/A4/Name", StringTool.leer)); //$NON-NLS-1$
		tA4Schacht.setText(CoreHub.localCfg.get("Drucker/A4/Schacht", StringTool.leer)); //$NON-NLS-1$
		tA5.setText(CoreHub.localCfg.get("Drucker/A5/Name", StringTool.leer)); //$NON-NLS-1$
		tA5Schacht.setText(CoreHub.localCfg.get("Drucker/A5/Schacht", StringTool.leer)); //$NON-NLS-1$
		tEinzelblatt.setText(CoreHub.localCfg.get("Drucker/Einzelblatt/Name", StringTool.leer)); //$NON-NLS-1$
		tEinzelblattSchacht.setText(CoreHub.localCfg.get(
			"Drucker/Einzelblatt/Schacht", StringTool.leer)); //$NON-NLS-1$
		new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(3,
			true, 1, false));
		bClear = new Button(ret, SWT.PUSH);
		bClear.setText(Messages.PrinterPreferencePage_ClearPrinterSettings);
		bClear.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		bClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				tEtiketten.setText(StringTool.leer);
				tA4.setText(StringTool.leer);
				tA4ESR.setText(StringTool.leer);
				tA5.setText(StringTool.leer);
			}
		});
		return ret;
	}
	
	class PrinterSelector extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e){
			PrintDialog pd = new PrintDialog(getShell());
			PrinterData pdata = pd.open();
			if (pdata != null) {
				Text tx = (Text) ((Button) e.getSource()).getData();
				tx.setText(pdata.name);
				tx.setData(pdata);
			}
		}
		
	};
	
	private void setEtikettenSelection(boolean selection){
		if (selection) {
			tEtiketten.setText(StringTool.leer);
			tEtiketten.setData(null);
			tEtikettenschacht.setText(StringTool.leer);
		}
		
		tEtiketten.setEnabled(!selection);
		tEtikettenschacht.setEnabled(!selection);
		bEtiketten.setEnabled(!selection);
	}
	
	public void init(IWorkbench workbench){
		
	}
	
	@Override
	public boolean performOk(){
		CoreHub.localCfg.set("Drucker/Etiketten/Name", tEtiketten.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/Etiketten/Schacht", tEtikettenschacht.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/Etiketten/Choose", cEtiketten.getSelection()); //$NON-NLS-1$
		Object data = tEtiketten.getData();
		if (data instanceof PrinterData) {
			PrinterData pdata = (PrinterData) data;
			CoreHub.localCfg.set("Drucker/Etiketten/Driver", pdata.driver); //$NON-NLS-1$
		} else {
			CoreHub.localCfg.set("Drucker/Etiketten/Driver", StringTool.leer); //$NON-NLS-1$
		}
		
		CoreHub.localCfg.set("Drucker/A4ESR/Name", tA4ESR.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/A4ESR/Schacht", tA4ESRSchacht.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/A4/Name", tA4.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/A4/Schacht", tA4Schacht.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/A5/Name", tA5.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/A5/Schacht", tA5Schacht.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/Einzelblatt/Name", tEinzelblatt.getText()); //$NON-NLS-1$
		CoreHub.localCfg.set("Drucker/Einzelblatt/Schacht", tEinzelblattSchacht.getText()); //$NON-NLS-1$
		
		return super.performOk();
	}
	
}
