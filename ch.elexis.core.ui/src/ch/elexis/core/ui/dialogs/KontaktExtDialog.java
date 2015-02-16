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

package ch.elexis.core.ui.dialogs;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;

/**
 * Dialog to view/modify identifiers such as EAN, AHV, SSN, OID on objects
 * 
 * @author Gerry
 * 
 */
public class KontaktExtDialog extends TitleAreaDialog {
	private Kontakt k;
	private String[] f;
	
	public KontaktExtDialog(Shell shell, Kontakt k, String[] defvalues){
		super(shell);
		this.k = k;
		f = defvalues;
		Arrays.sort(f, new Comparator<String>() {
			
			public int compare(String o1, String o2){
				return o1.compareTo(o2);
			}
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		ExtInfoTable ret = new ExtInfoTable(parent, f);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setKontakt(k);
		ret.pack();
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(k.getLabel());
		setMessage(Messages.KontaktExtDialog_pleaseENterDetails); //$NON-NLS-1$
		getShell().setText(Messages.KontaktExtDialog_indetityDetails); //$NON-NLS-1$
	}
	
	@Override
	protected void okPressed(){
		// TODO Automatisch erstellter Methoden-Stub
		super.okPressed();
	}
	
	public static class ExtInfoTable extends Composite {
		Kontakt actKontakt;
		TableCursor cursor;
		ControlEditor editor;
		String[] fields;
		Table table;
		private HashMap<String, String> xids;
		
		/**
		 * fields can be of the form {name1,name2...} or {name1=xiddomain1,name2,name3=Xiddomain3}
		 * 
		 * @param parent
		 * @param f
		 */
		public ExtInfoTable(Composite parent, String[] f){
			super(parent, SWT.NONE);
			xids = new HashMap<String, String>();
			setLayout(new FillLayout());
			// kontakt=k;
			fields = new String[f.length];
			
			for (int i = 0; i < f.length; i++) {
				String[] val = f[i].split("="); //$NON-NLS-1$
				fields[i] = val[0];
				if (val.length == 2) {
					xids.put(val[0], val[1]);
				}
			}
			table = new Table(this, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL);
			cursor = new TableCursor(table, SWT.NONE);
			editor = new ControlEditor(cursor);
			editor.grabHorizontal = true;
			editor.grabVertical = true;
			cursor.addSelectionListener(new SelectionAdapter() {
				// Tabellenauswahl soll dem Cursor folgen
				public void widgetSelected(SelectionEvent e){
					table.setSelection(new TableItem[] {
						cursor.getRow()
					});
				}
				
				// Eingabetaste
				public void widgetDefaultSelected(SelectionEvent e){
					TableItem row = cursor.getRow();
					int column = cursor.getColumn();
					doEdit(row.getText(column));
				}
			});
			// Sonstige Taste
			cursor.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e){
					if (e.character > 0x30) {
						StringBuilder sb = new StringBuilder();
						sb.append(e.character);
						doEdit(sb.toString());
					}
				}
			});
			
			table.setLinesVisible(true);
			TableColumn parms = new TableColumn(table, SWT.NONE);
			TableColumn vals = new TableColumn(table, SWT.NONE);
			parms.setText(Messages.KontaktExtDialog_parameter); //$NON-NLS-1$
			vals.setText(Messages.KontaktExtDialog_value); //$NON-NLS-1$
			parms.setWidth(150);
			vals.setWidth(150);
			table.setHeaderVisible(true);
			for (int i = 0; i < fields.length; i++) {
				new TableItem(table, SWT.NONE);
			}
			
		}
		
		public void setKontakt(Kontakt k){
			for (int i = 0; i < fields.length; i++) {
				TableItem it = table.getItem(i);
				it.setText(0, fields[i]);
				String val = ""; //$NON-NLS-1$
				String xid = xids.get(fields[i]);
				if (xid != null) {
					val = k.getXid(xid);
				}
				if (val.length() == 0) {
					val = (String) k.getInfoElement(fields[i]);
				}
				it.setText(1, val == null ? "" : val); //$NON-NLS-1$
			}
			actKontakt = k;
		}
		
		private void doEdit(String inp){
			final Text text = new Text(cursor, SWT.BORDER);
			text.setText(inp);
			text.setSelection(inp.length());
			text.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e){
					if ((e.character == SWT.CR) || (e.keyCode == SWT.ARROW_DOWN)) {
						TableItem it = cursor.getRow();
						int idx = cursor.getColumn(); // Spalte der Anzeige
						// String ntext=text.getText();
						it.setText(idx, text.getText());
						actKontakt.setInfoElement(it.getText(0), it.getText(1));
						String xid = xids.get(it.getText(0));
						if (xid != null) {
							actKontakt.addXid(xid, it.getText(1), true);
						}
						text.dispose();
						// cursorDown();
					}
					// close the text editor when the user hits "ESC"
					if (e.character == SWT.ESC) {
						text.dispose();
					}
				}
			});
			editor.setEditor(text);
			text.setFocus();
		}
		
	}
	
}
