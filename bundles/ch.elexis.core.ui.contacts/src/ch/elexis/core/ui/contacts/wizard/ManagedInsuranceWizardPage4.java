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
import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class ManagedInsuranceWizardPage4 extends WizardPage {

	private ManagedInsuranceModel currentManagedInsuranceModel;

	private List<IOrganization> notAssignedOrganizations;

	private Label amountLabel;

	private Label mappedLabel;

	private Label ignoredLabel;

	private Label todoLabel;

	private Button applyBtn;

	protected ManagedInsuranceWizardPage4(String pageName, List<IOrganization> notAssignedOrganizations,
			ManagedInsuranceModel currentManagedInsuranceModel) {
		super(pageName);
		setTitle(pageName);
		setMessage(
				"Wenn keine ausstehenden Organisationen mehr vorhanden sind kann können die Änderungen durch geführt werden.");
		setImageDescriptor(ImageDescriptor
				.createFromURI(URI.create("platform:/plugin/ch.elexis.core.ui.contacts/rsc/mngd_wizard.png")));
		this.currentManagedInsuranceModel = currentManagedInsuranceModel;
		this.notAssignedOrganizations = notAssignedOrganizations;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.WRAP);
		label.setText(
				"Die zugewiesenen Versicherungen können bei den Fällen anstelle der Organisationen gesetzt werde, wenn alle gefundenen entweder ignoriert sind, oder eine Zuweisung gemacht wurde.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		label = new Label(composite, SWT.WRAP);
		label.setText(
				"Die getätigten Änderungen sind gespeichert. Der Dialog kann geschlossen werden, und zu einem späteren Zeitpunkt weiter gearbeitet werden.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		label = new Label(composite, SWT.NONE);
		label.setText("Anzahl Organisationen:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		amountLabel = new Label(composite, SWT.NONE);
		amountLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(composite, SWT.NONE);
		label.setText("Bestätigt:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		mappedLabel = new Label(composite, SWT.NONE);
		mappedLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(composite, SWT.NONE);
		label.setText("Ignoriert:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		ignoredLabel = new Label(composite, SWT.NONE);
		ignoredLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(composite, SWT.NONE);
		label.setText("Ausstehend:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		todoLabel = new Label(composite, SWT.NONE);
		todoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		applyBtn = new Button(composite, SWT.PUSH);
		applyBtn.setText("Änderungen übernhemen");
		applyBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		applyBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
				try {
					progressDialog.run(true, false, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							monitor.beginTask("Organisationen und Versicherungen Änderungen werden übernommen.",
									currentManagedInsuranceModel.getMapping().size());
							currentManagedInsuranceModel.getMapping().forEach((o, i) -> {
								IOrganization organization = CoreModelServiceHolder.get().load(o, IOrganization.class)
										.get();
								IOrganization insurance = CoreModelServiceHolder.get().load(i, IOrganization.class)
										.get();
								ChangeOrganizationToInsurance change = new ChangeOrganizationToInsurance(organization,
										insurance);
								change.run();
							});
						}
					});
				} catch (InvocationTargetException | InterruptedException ex) {
					MessageDialog.openError(getShell(), "Organisationen und Versicherungen",
							"Organisationen und Versicherungen Änderungen konnten nicht übernommen werden.");
					LoggerFactory.getLogger(getClass()).error("Error apply changes", ex);
				}
			}
		});

		setControl(composite);

		refresh();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		refresh();
	}

	private void refresh() {
		amountLabel.setText(Integer.toString(notAssignedOrganizations.size()));

		mappedLabel.setText(Integer.toString(currentManagedInsuranceModel.getConfirmed().size()));

		ignoredLabel.setText(Integer.toString(currentManagedInsuranceModel.getIgnored().size()));

		todoLabel.setText(
				Integer.toString(notAssignedOrganizations.size() - (currentManagedInsuranceModel.getConfirmed().size()
						+ currentManagedInsuranceModel.getIgnored().size())));

		if (isPageComplete()) {
			applyBtn.setToolTipText("");
			applyBtn.setEnabled(true);
		} else {
			applyBtn.setEnabled(false);
		}

		todoLabel.getParent().layout();
	}

	@Override
	public boolean isPageComplete() {
		return currentManagedInsuranceModel.getConfirmed().size()
				+ currentManagedInsuranceModel.getIgnored().size() == notAssignedOrganizations.size();
	}

	public boolean finish() {
		// TODO Auto-generated method stub
		return false;
	}
}
