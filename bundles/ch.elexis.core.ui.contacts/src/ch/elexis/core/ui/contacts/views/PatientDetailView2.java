/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.contacts.views;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Patient;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class PatientDetailView2 extends ViewPart implements IActivationListener {
	public static final String ID = "ch.elexis.PatDetail_v2"; //$NON-NLS-1$
	Patientenblatt2 pb;

	@Override
	public void createPartControl(Composite parent) {
		setTitleImage(Images.IMG_VIEW_PATIENT_DETAIL.getImage());

		setPartName(Messages.Core_Patientdetails); // $NON-NLS-1$
		parent.setLayout(new FillLayout());
		pb = new Patientenblatt2(parent, getViewSite());

		GlobalEventDispatcher.addActivationListener(this, this);
	}

	public void refresh() {
		pb.setPatient((Patient) ElexisEventDispatcher.getSelected(Patient.class));
		pb.refresh();
	}

	@Override
	public void setFocus() {
		if (pb != null && !pb.isDisposed()) {
			Patient selectedPatient = ElexisEventDispatcher.getSelectedPatient();
			if (selectedPatient != null && !selectedPatient.equals(pb.actPatient)) {
				pb.setPatient((Patient) ElexisEventDispatcher.getSelected(Patient.class));
				pb.refresh();
			}
		}
	}

	@Override
	public void dispose() {
		if (pb != null) {
			pb.dispose();
		}
		super.dispose();
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	@Override
	public void activation(boolean mode) {
		if (!mode) {
			// save does not happen via locking in standalone mode
			if (ElexisServerServiceHolder.get().getConnectionStatus() == ConnectionStatus.STANDALONE) {
				pb.save();
			}
		} else {
			pb.refreshUi();
		}
	}

	@Override
	public void visible(boolean mode) {
	}
}
