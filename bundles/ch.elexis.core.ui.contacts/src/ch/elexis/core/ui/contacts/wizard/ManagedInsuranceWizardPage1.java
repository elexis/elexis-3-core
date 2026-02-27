/*******************************************************************************
 * Copyright (c) 2026 MEDEVIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     T. Huster - initial API and implementation
 *******************************************************************************/
package ch.elexis.core.ui.contacts.wizard;

import java.net.URI;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

public class ManagedInsuranceWizardPage1 extends WizardPage {

	protected ManagedInsuranceWizardPage1(String pageName) {
		super(pageName);
		setTitle(pageName);
		setImageDescriptor(ImageDescriptor
				.createFromURI(URI.create("platform:/plugin/ch.elexis.core.ui.contacts/rsc/mngd_wizard.png")));
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.WRAP);
		label.setText(
				"Folgende Organisations-Kontakte auf Fällen wurden gefunden, welche keine gültige EAN enthalten und nicht automatisch zugeordnet werden konnten.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label = new Label(composite, SWT.WRAP);
		label.setText(
				"Bitte wählen Sie den entdprechenden Kontakt von den verwalteten Versicherungen aus, welcher den gefundenen Organisations-Kontakt ersetzen soll.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label = new Label(composite, SWT.WRAP);
		label.setText(
				"Achtung! - Es können auch Organisationen die keien Versicherungen sind angezeigt werden!\nFür diese kann die Option \"ignorieren\" ausgewählt werden.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label = new Label(composite, SWT.WRAP);
		label.setText(
				"Bitte nehmen Sie sich für diesen Vorgang Zeit. Nach der Betätigung von \"Übernehmen\" sind die Änderungen permanent und können nicht rückgängig gemacht werden!");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label = new Label(composite, SWT.WRAP);
		label.setText("Weitere Informationen finden Sie unter:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Link link = new Link(composite, SWT.NONE);
		link.setText("<a href=\"https://www.support.medelexis.ch/migration_managed-contacts/\">Support Medelexis</a>");
		link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Event handling when users click on links.
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("https://www.support.medelexis.ch/migration_managed-contacts/");
			}
		});

		setControl(composite);
	}

	public boolean finish() {
		// TODO Auto-generated method stub
		return false;
	}
}
