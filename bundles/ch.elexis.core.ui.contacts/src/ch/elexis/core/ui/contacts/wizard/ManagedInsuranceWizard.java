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

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import ch.elexis.core.model.IOrganization;

public class ManagedInsuranceWizard extends Wizard {

	private ManagedInsuranceWizardPage1 page1;
	private ManagedInsuranceWizardPage2 page2;
	private ManagedInsuranceWizardPage3 page3;
	private ManagedInsuranceWizardPage4 page4;

	private ManagedInsuranceModel currentManagedInsuranceModel;

	private List<IOrganization> notAssignedOrganizations;

	public ManagedInsuranceWizard() {
		setWindowTitle("Versicherungen verwalten");
		setNeedsProgressMonitor(true);

		currentManagedInsuranceModel = ManagedInsuranceModel.load();

		notAssignedOrganizations = new NotAssignedOrganizationsSupplier().get();
	}

	@Override
	public void addPages() {
		super.addPages();
		page1 = new ManagedInsuranceWizardPage1("Versicherungen verwalten");
		addPage(page1);
		page2 = new ManagedInsuranceWizardPage2("Nicht zugewiesene Organisationen", notAssignedOrganizations,
				currentManagedInsuranceModel);
		addPage(page2);
		page3 = new ManagedInsuranceWizardPage3("Zugewiesene Organisationen", currentManagedInsuranceModel);
		addPage(page3);
		page4 = new ManagedInsuranceWizardPage4("Status", notAssignedOrganizations, currentManagedInsuranceModel);
		addPage(page4);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
