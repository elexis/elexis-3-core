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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class ManagedInsuranceWizard extends Wizard {

	private ManagedInsuranceWizardPage1 page1;
	private ManagedInsuranceWizardPage2 page2;
	private ManagedInsuranceWizardPage3 page3;
	private ManagedInsuranceWizardPage4 page4;

	private ManagedInsuranceModel currentManagedInsuranceModel;

	private List<IOrganization> notAssignedOrganizations;

	private Map<String, Long> countCoveragesMap;

	public ManagedInsuranceWizard() {
		setWindowTitle("Versicherungen verwalten");
		setNeedsProgressMonitor(true);

		currentManagedInsuranceModel = ManagedInsuranceModel.load();
		transferDeletedToApplied();

		notAssignedOrganizations = new NotAssignedOrganizationsSupplier().get();

		countCoveragesMap = new HashMap<>();
		notAssignedOrganizations.parallelStream().forEach(o -> {
			countCoveragesMap.put(o.getId(), new CountCoverageSupplier(o).get());
		});
	}

	private void transferDeletedToApplied() {
		// transfer deleted from confirmed
		new ArrayList<>(currentManagedInsuranceModel.getConfirmed()).forEach(id -> {
			Optional<IOrganization> loaded = CoreModelServiceHolder.get().load(id, IOrganization.class);
			if (loaded.isEmpty()) {
				if (!currentManagedInsuranceModel.getApplied().contains(id)) {
					currentManagedInsuranceModel.getApplied().add(id);
				}
				currentManagedInsuranceModel.getConfirmed().remove(id);
			}
		});
		// transfer deleted from mapped
		new HashSet<String>(currentManagedInsuranceModel.getMapping().keySet()).forEach(id -> {
			Optional<IOrganization> loaded = CoreModelServiceHolder.get().load(id, IOrganization.class);
			if (loaded.isEmpty()) {
				if (!currentManagedInsuranceModel.getApplied().contains(id)) {
					currentManagedInsuranceModel.getApplied().add(id);
				}
				currentManagedInsuranceModel.getMapping().remove(id);
			}
		});
		currentManagedInsuranceModel.save();
	}

	@Override
	public void addPages() {
		super.addPages();
		page1 = new ManagedInsuranceWizardPage1("Versicherungen verwalten");
		addPage(page1);
		page2 = new ManagedInsuranceWizardPage2("Nicht zugewiesene Organisationen", notAssignedOrganizations,
				countCoveragesMap,
				currentManagedInsuranceModel);
		addPage(page2);
		page3 = new ManagedInsuranceWizardPage3("Zugewiesene Organisationen", currentManagedInsuranceModel);
		addPage(page3);
		page4 = new ManagedInsuranceWizardPage4("Status", notAssignedOrganizations, currentManagedInsuranceModel);
		addPage(page4);
	}

	@Override
	public boolean canFinish() {
		if (super.canFinish()) {
			return getContainer().getCurrentPage() == page4;
		}
		return false;
	}

	@Override
	public boolean performFinish() {
		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
		try {
			progressDialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Organisationen und Versicherungen Änderungen werden übernommen.",
							currentManagedInsuranceModel.getMapping().size());
					currentManagedInsuranceModel.getMapping().forEach((o, i) -> {
						if (currentManagedInsuranceModel.getConfirmed().contains(o)) {
							IOrganization organization = CoreModelServiceHolder.get().load(o, IOrganization.class)
									.get();
							IOrganization insurance = CoreModelServiceHolder.get().load(i, IOrganization.class).get();
							ChangeOrganizationToInsurance change = new ChangeOrganizationToInsurance(organization,
									insurance);
							change.run();
							monitor.worked(1);
						}
					});
					transferDeletedToApplied();
					monitor.done();
				}
			});
		} catch (InvocationTargetException | InterruptedException ex) {
			MessageDialog.openError(getShell(), "Organisationen und Versicherungen",
					"Organisationen und Versicherungen Änderungen konnten nicht übernommen werden.");
			LoggerFactory.getLogger(getClass()).error("Error apply changes", ex);
		}

		return true;
	}

}
