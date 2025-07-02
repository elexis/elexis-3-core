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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Patient;

public class PatientDetailView2 extends ViewPart {

	@Inject
	IContextService contextService;

	public static final String ID = "ch.elexis.PatDetail_v2"; //$NON-NLS-1$
	Patientenblatt2 pb;

	private RefreshingPartListener patientUpdateListener = new RefreshingPartListener(this) {
		public void partVisible(IWorkbenchPartReference partRef) {
			if (pb != null && !pb.isDisposed()) {
				contextService.getActivePatient().ifPresent(selectedPatient -> {
					if (pb.actPatient != null && !selectedPatient.equals(pb.actPatient.toIPatient())) {
						pb.setPatient((Patient) NoPoUtil.loadAsPersistentObject(selectedPatient));
						pb.refresh();
					}
				});
			}
		}

		public void partActivated(IWorkbenchPartReference partRef) {
			if (pb != null && !pb.isDisposed()) {
				pb.refreshUi();
			}
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
			if (pb != null && !pb.isDisposed() &&
				    ElexisServerServiceHolder.get().getConnectionStatus() == ConnectionStatus.STANDALONE) {
					pb.save();
				}
		}
	};

	@Override
	public void createPartControl(Composite parent) {
		setTitleImage(Images.IMG_VIEW_PATIENT_DETAIL.getImage());

		setPartName(Messages.Core_Patientdetails); // $NON-NLS-1$
		parent.setLayout(new FillLayout());
		pb = new Patientenblatt2(parent, getViewSite());

		getSite().getPage().addPartListener(patientUpdateListener);
	}

	@Override
	public void setFocus() {
		if (pb != null && !pb.isDisposed()) {
			pb.setFocus();
		}
	}

	@Override
	public void dispose() {
		if (pb != null) {
			pb.dispose();
		}
		getSite().getPage().removePartListener(patientUpdateListener);
		super.dispose();
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
