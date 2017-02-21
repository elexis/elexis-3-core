/*******************************************************************************
 * Copyright (c) 2017 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.data.util.BillingUtil.IBillableCheck;

public class BillingChecksPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	
	@Override
	public void init(IWorkbench workbench){
		setTitle("Rechnungsprüfung");
		setDescription(
			"Hier können die Prüfungen beim Rechnungs-Vorschlag aktiviert, bzw. deaktiviert, werden. Achtung: bei der Rechnungserstellung wird unabhängig davon geprüft.");
	}
	
	@Override
	protected Control createContents(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		
		IBillableCheck[] checks = BillingUtil.billableChecks;
		for (IBillableCheck iBillableCheck : checks) {
			BillableCheckComposite composite = new BillableCheckComposite(ret, SWT.NONE);
			composite.setCheck(iBillableCheck);
		}
		
		return ret;
	}
	
	private class BillableCheckComposite extends Composite {
		
		private IBillableCheck check;
		
		private Button enabled;
		private Label description;
		
		public BillableCheckComposite(Composite parent, int style){
			super(parent, style);
			setLayout(new RowLayout());
			
			enabled = new Button(this, SWT.CHECK);
			description = new Label(this, SWT.NONE);
		}
		
		public void setCheck(IBillableCheck iBillableCheck){
			this.check = iBillableCheck;
			enabled.setSelection(BillingUtil.isCheckEnabled(check));
			enabled.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					BillingUtil.setCheckEnabled(check, enabled.getSelection());
				}
			});
			description.setText(check.getDescription());
			layout();
		}
	}
}
