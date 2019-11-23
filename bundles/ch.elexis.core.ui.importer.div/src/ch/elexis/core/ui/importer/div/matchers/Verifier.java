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
package ch.elexis.core.ui.importer.div.matchers;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;

import ch.rgw.tools.StringTool;
public class Verifier {
	
	public static Kontakt verify(Kontakt k, String t, String m){
		if (SWTHelper.askYesNo(t, m)) {
			return k;
		}
		KontaktSelektor ksl =
			new KontaktSelektor(Hub.getActiveShell(), k.getClass(), t,
				Messages.Verifier_PleaseSelectCorrectContact, Kontakt.DEFAULT_SORT);
		if (ksl.open() == Dialog.OK) {
			return (Kontakt) ksl.getSelection();
		}
		return null;
	}
	
	public static Kontakt resolveAmbiguity(List<Kontakt> list, String t, String m){
		Resolver resolver = new Resolver(Hub.getActiveShell(), list);
		resolver.setTitle(t);
		resolver.setMessage(m);
		if (resolver.open() == Dialog.OK) {
			return resolver.result;
		} else {
			return null;
		}
	}
	
	static class Resolver extends TitleAreaDialog {
		List<Kontakt> list;
		Kontakt result;
		Table table;
		
		public Resolver(Shell shell, List<Kontakt> kont){
			super(shell);
			list = kont;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			parent.setLayout(new FillLayout());
			table = new Table(parent, SWT.NONE);
			TableColumn c1 = new TableColumn(table, SWT.NONE);
			c1.setText(Messages.Verifier_NameHeading);
			c1.setWidth(200);
			TableColumn c2 = new TableColumn(table, SWT.NONE);
			c2.setText(Messages.Verifier_AddressHeading);
			c2.setWidth(200);
			for (Kontakt k : list) {
				TableItem it = new TableItem(table, SWT.NONE);
				it.setText(0, k.getLabel(true));
				it.setText(1, k.get("Strasse") + StringTool.space + k.get("Ort")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return table;
		}
		
		@Override
		public void create(){
			super.create();
		}
		
		@Override
		protected void okPressed(){
			int ix = table.getSelectionIndex();
			if (ix != -1) {
				result = list.get(ix);
			} else {
				result = null;
			}
			super.okPressed();
		}
		
	}
}
