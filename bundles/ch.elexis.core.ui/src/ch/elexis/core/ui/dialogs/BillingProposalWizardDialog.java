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
package ch.elexis.core.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.ui.views.controls.GenericSelectionComposite;
import ch.elexis.core.ui.views.controls.KontaktSelectionComposite;
import ch.elexis.core.ui.views.controls.TimeSpanSelectionComposite;
import ch.elexis.data.BillingSystem;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

public class BillingProposalWizardDialog extends TitleAreaDialog {
	
	private List<Konsultation> proposal;
	
	private Button insurerOnly;
	private KontaktSelectionComposite insurerSelection;
	
	private Button timeSpanOnly;
	private TimeSpanSelectionComposite timeSpanSelection;
	
	private Button mandatorOnly;
	private GenericSelectionComposite mandatorSelector;
	
	private Button accountingOnly;
	private GenericSelectionComposite accountingSelector;
	
	private Button errorneousOnly;
	
	public BillingProposalWizardDialog(Shell parentShell){
		super(parentShell);
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Rechnungs-Vorschlag Auswahl");
		setMessage(Messages.KonsZumVerrechnenWizardDialog_createProposal);
		getShell().setText("Rechnungs-Vorschlag");
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		Composite content = new Composite(ret, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		insurerOnly = new Button(content, SWT.CHECK);
		insurerOnly.setText("nur von folgendem Versicherer");
		insurerSelection = new KontaktSelectionComposite(content, SWT.NONE | SWT.MULTI);
		insurerSelection.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		insurerSelection.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					insurerOnly.setSelection(true);
				} else {
					insurerOnly.setSelection(false);
				}
			}
		});
		
		timeSpanOnly = new Button(content, SWT.CHECK);
		timeSpanOnly.setText("nur innerhalb des Zeitraums");
		timeSpanSelection = new TimeSpanSelectionComposite(content, SWT.NONE);
		timeSpanSelection.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		timeSpanSelection.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					timeSpanOnly.setSelection(true);
				} else {
					timeSpanOnly.setSelection(false);
				}
			}
		});
		LocalDate dateNow = LocalDate.now();
		timeSpanSelection
			.setTimeSpan(
				new TimeSpan(new TimeTool(dateNow.withDayOfMonth(1)), new TimeTool(dateNow)));
		
		mandatorOnly = new Button(content, SWT.CHECK);
		mandatorOnly.setText("nur von folgenden Mandanten");
		mandatorSelector = new GenericSelectionComposite(content, SWT.NONE);
		mandatorSelector.setInput(new Query<>(Mandant.class).execute());
		mandatorSelector.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		mandatorSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					mandatorOnly.setSelection(true);
				} else {
					mandatorOnly.setSelection(false);
				}
			}
		});
		
		accountingOnly = new Button(content, SWT.CHECK);
		accountingOnly.setText("nur von folgenden Abrechnungssystemen");
		accountingSelector = new GenericSelectionComposite(content, SWT.NONE);
		accountingSelector.setInput(Arrays.asList(BillingSystem.getAbrechnungsSysteme()));
		accountingSelector.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		accountingSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					accountingOnly.setSelection(true);
				} else {
					accountingOnly.setSelection(false);
				}
			}
		});
		
		errorneousOnly = new Button(content, SWT.CHECK);
		errorneousOnly.setText("nur fehlerhafte");
		new Label(content, SWT.NONE);
		
		return ret;
	}
	
	@Override
	protected void okPressed(){
		ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell());
		QueryProposalRunnable runnable = new QueryProposalRunnable();
		try {
			progress.run(true, true, runnable);
			if (runnable.isCanceled()) {
				return;
			} else {
				proposal = runnable.getProposal();
			}
		} catch (InvocationTargetException | InterruptedException e) {
			LoggerFactory.getLogger(BillingProposalWizardDialog.class)
				.error("Error running proposal query", e);
			MessageDialog.openError(getShell(), "Fehler",
				"Fehler beim Ausführen des Rechnungs-Vorschlags.");
			return;
		}
		
		super.okPressed();
	}
	
	public Optional<List<Konsultation>> getProposal() {
		return Optional.ofNullable(proposal);
	}
	
	private class QueryProposalRunnable implements IRunnableWithProgress {
		
		private boolean canceled = false;
		private List<Konsultation> proposal = new ArrayList<>();
		
		private IFilter insurerOnlyFilter;
		private IFilter accountingOnlyFilter;
		private IFilter errorneousOnlyFilter;
		private Query<Konsultation> query;
		
		public QueryProposalRunnable(){
			query = new Query<>(Konsultation.class);
			query.add(Konsultation.FLD_BILL_ID, Query.EQUALS, null);
			if (timeSpanOnly.getSelection()) {
				IStructuredSelection selection =
					(IStructuredSelection) timeSpanSelection.getSelection();
				if (selection != null && !selection.isEmpty()) {
					TimeSpan timeSpan = (TimeSpan) selection.getFirstElement();
					query.add(Konsultation.DATE, Query.GREATER_OR_EQUAL,
						timeSpan.from.toString(TimeTool.DATE_COMPACT));
					query.add(Konsultation.DATE, Query.LESS_OR_EQUAL,
						timeSpan.until.toString(TimeTool.DATE_COMPACT));
				}
			}
			if (mandatorOnly.getSelection()) {
				IStructuredSelection selection =
					(IStructuredSelection) mandatorSelector.getSelection();
				if (selection != null && !selection.isEmpty()) {
					@SuppressWarnings("unchecked")
					List<Mandant> mandators = selection.toList();
					query.startGroup();
					for (int i = 0; i < mandators.size(); i++) {
						if (i > 0) {
							query.or();
						}
						query.add(Konsultation.FLD_MANDATOR_ID, Query.EQUALS,
							mandators.get(i).getId());
					}
					query.endGroup();
				}
			}
			
			if (insurerOnly.getSelection()) {
				insurerOnlyFilter = new IFilter() {
					
					@SuppressWarnings("unchecked")
					private List<Kontakt> insurers =
						(List<Kontakt>) ((IStructuredSelection) insurerSelection.getSelection())
							.toList();
					
					@Override
					public boolean select(Object element){
						Fall fall = ((Konsultation) element).getFall();
						Kontakt costBearer = fall.getCostBearer();
						if (costBearer != null) {
							for (Kontakt insurer : insurers) {
								if (costBearer.getId().equals(insurer.getId())) {
									return true;
								}
							}
						}
						return false;
					}
				};
			}
			if (accountingOnly.getSelection()) {
				accountingOnlyFilter = new IFilter() {
					
					@SuppressWarnings("unchecked")
					private List<String> accountings =
						((IStructuredSelection) accountingSelector.getSelection()).toList();
					
					@Override
					public boolean select(Object element){
						Fall fall = ((Konsultation) element).getFall();
						String accounting = fall.getAbrechnungsSystem();
						return accountings.contains(accounting);
					}
				};
			}
			if (errorneousOnly.getSelection()) {
				errorneousOnlyFilter = new IFilter() {
					@Override
					public boolean select(Object element){
						return !BillingUtil.getBillableResult((Konsultation) element).isOK();
					}
				};
			}
			
		}
		
		@Override
		public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException{
			List<Konsultation> initialList = query.execute();
			monitor.beginTask("Vorschlag filter", initialList.size());
			for (Konsultation konsultation : initialList) {
				if (monitor.isCanceled()) {
					canceled = true;
					return;
				}
				
				if (insurerOnlyFilter != null && !insurerOnlyFilter.select(konsultation)) {
					monitor.worked(1);
					continue;
				}
				if (accountingOnlyFilter != null && !accountingOnlyFilter.select(konsultation)) {
					monitor.worked(1);
					continue;
				}
				if (errorneousOnlyFilter != null && !errorneousOnlyFilter.select(konsultation)) {
					monitor.worked(1);
					continue;
				}
				proposal.add(konsultation);
				monitor.worked(1);
			}
			monitor.done();
		}
		
		public boolean isCanceled(){
			return canceled;
		}
		
		public List<Konsultation> getProposal(){
			return proposal;
		}
	}
}
