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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.controls.DaysOrDateSelectionComposite;
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
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

public class BillingProposalWizardDialog extends TitleAreaDialog {
	
	private List<Konsultation> proposal;
	
	private Button insurerOnly;
	private KontaktSelectionComposite insurerSelection;
	
	private Button timeSpanOnly;
	private TimeSpanSelectionComposite timeSpanSelection;
	
	private Button beforeTimeOnly;

	private ExcludeKonsByMoneyComposite excludeKonsByMoneyComposite;
	
	private DaysOrDateSelectionComposite beforeDaysOrDate;
	
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
		
		timeSpanOnly = new Button(content, SWT.CHECK);
		timeSpanOnly.setText("Offene Konsultationen innerhalb des Zeitraums");
		timeSpanOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (timeSpanOnly.getSelection()) {
					beforeTimeOnly.setSelection(false);
				}
			}
		});
		timeSpanSelection = new TimeSpanSelectionComposite(content, SWT.NONE);
		timeSpanSelection.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		timeSpanSelection.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					timeSpanOnly.setSelection(true);
					beforeTimeOnly.setSelection(false);
				} else {
					timeSpanOnly.setSelection(false);
				}
			}
		});
		LocalDate dateNow = LocalDate.now();
		timeSpanSelection
			.setTimeSpan(
				new TimeSpan(new TimeTool(dateNow.withDayOfMonth(1)), new TimeTool(dateNow)));
		
		beforeTimeOnly = new Button(content, SWT.CHECK);
		beforeTimeOnly.setText("Offene Behandlungsserien vor Tagen oder Datum");
		beforeTimeOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (beforeTimeOnly.getSelection()) {
					timeSpanOnly.setSelection(false);
				}
				excludeKonsByMoneyComposite.changeVisibility(beforeTimeOnly.getSelection());
			}
		});
		beforeDaysOrDate = new DaysOrDateSelectionComposite(content, SWT.NONE);
		beforeDaysOrDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		beforeDaysOrDate.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					beforeTimeOnly.setSelection(true);
					timeSpanOnly.setSelection(false);
				} else {
					beforeTimeOnly.setSelection(false);
				}
			}
		});
		beforeDaysOrDate.setDate(LocalDate.now().minusDays(30));

		excludeKonsByMoneyComposite = new ExcludeKonsByMoneyComposite(content);
		
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
		new Label(content, SWT.NONE);
		new Label(content, SWT.NONE);
		new Label(content, SWT.NONE); //GAPS
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
		
		private boolean addSeries;
		private Money excludeKonsByMoney;
		
		public QueryProposalRunnable(){
			query = new Query<>(Konsultation.class);
			query.add(Konsultation.FLD_BILL_ID, Query.EQUALS, null);
			query.add(Konsultation.FLD_BILLABLE, Query.EQUALS, "1");
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
			if (beforeTimeOnly.getSelection()) {
				addSeries = true;
				IStructuredSelection selection =
					(IStructuredSelection) beforeDaysOrDate.getSelection();
				if (selection != null && !selection.isEmpty()) {
					LocalDate fromDate = (LocalDate) selection.getFirstElement();
					TimeTool fromTool = new TimeTool(fromDate);
					query.add(Konsultation.DATE, Query.LESS_OR_EQUAL,
						fromTool.toString(TimeTool.DATE_COMPACT));					
					if (excludeKonsByMoneyComposite.isSelected()) {
						excludeKonsByMoney = excludeKonsByMoneyComposite.getMoney();
					}
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
			SubMonitor progress = SubMonitor.convert(monitor, 100);
			
			progress.setTaskName("Vorschlag filter");
			if (addSeries) {
				progress.setWorkRemaining(initialList.size() * 2);
			} else {
				progress.setWorkRemaining(initialList.size());
			}
			for (Konsultation konsultation : initialList) {
				if (progress.isCanceled()) {
					canceled = true;
					return;
				}
				if (!applyFilters(konsultation)) {
					progress.worked(1);
					continue;
				}
				proposal.add(konsultation);
				progress.worked(1);
			}
			if(addSeries) {
				progress.setTaskName("Behandlungsserien laden");
				HashSet<String> knownIds = new HashSet<>();
				knownIds.addAll(
					proposal.parallelStream().map(k -> k.getId()).collect(Collectors.toList()));
				ArrayList<Konsultation> proposalCopy = new ArrayList<>(proposal);
				proposalCopy.forEach(k -> {
					List<Konsultation> series = getSeries(k.getFall());
					Money totalForKonsSerie = new Money();
					
					series.forEach(sk -> {
						if (!knownIds.contains(sk.getId())) {
							// only look at sk once
							knownIds.add(sk.getId());
							if (applyFilters(sk)) {
								proposal.add(sk);
							}
						}
						// calculate money for kons series
						if (excludeKonsByMoney != null) {
							totalForKonsSerie.addMoney(BillingUtil.getTotal(sk));
						}
					});
					if (excludeKonsByMoney != null) {
						if (totalForKonsSerie.equals(excludeKonsByMoney)
							|| totalForKonsSerie.isMoreThan(excludeKonsByMoney)) {
							proposal.removeAll(series);
						}
					}
					progress.worked(1);
					if (progress.isCanceled()) {
						canceled = true;
						return;
					}
				});
			}
			monitor.done();
		}
		
		/**
		 * Apply the filters to the {@link Konsultation} return false if any filter matches, true if
		 * it passes all filters. Filters can have performance implications.
		 * 
		 * @param konsultation
		 * @return
		 */
		private boolean applyFilters(Konsultation konsultation){
			if (insurerOnlyFilter != null && !insurerOnlyFilter.select(konsultation)) {
				return false;
			}
			if (accountingOnlyFilter != null && !accountingOnlyFilter.select(konsultation)) {
				return false;
			}
			if (errorneousOnlyFilter != null && !errorneousOnlyFilter.select(konsultation)) {
				return false;
			}
			return true;
		}
		
		private List<Konsultation> getSeries(Fall fall){
			return Arrays.asList(fall.getBehandlungen(false)).parallelStream()
				.filter(k -> k.getRechnung() == null || !k.getRechnung().exists())
				.collect(Collectors.toList());
		}
		
		public boolean isCanceled(){
			return canceled;
		}
		
		public List<Konsultation> getProposal(){
			return proposal;
		}
	}
	
	private class ExcludeKonsByMoneyComposite extends Composite {
		private MoneyInput txtExcludeKonsByMoney;
		private Button checkExcludeKonsByMoney;
		
		public ExcludeKonsByMoneyComposite(Composite parent){
			super(parent, SWT.NONE);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			setLayout(SWTHelper.createGridLayout(true, 2));
		}
		
		private void createContents(){
			checkExcludeKonsByMoney = new Button(this, SWT.CHECK);
			checkExcludeKonsByMoney.setText("Offene Behandlungsserien ausschliessen");
			Composite c = new Composite(this, SWT.NONE);
			c.setLayout(new GridLayout(2, false));
			c.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
			Label lblBeforeTimeOnlyValueLessThen = new Label(c, SWT.NONE);
			lblBeforeTimeOnlyValueLessThen.setText("Betrag kleiner CHF");
		
			txtExcludeKonsByMoney = new MoneyInput(c);
			txtExcludeKonsByMoney.getControl().addListener(SWT.Verify, new Listener() {
				public void handleEvent(Event e){
					// limits only the input the value validation happens at focus lost
					String string = e.text;
					
					char[] chars = new char[string.length()];
					string.getChars(0, chars.length, chars, 0);
					for (int i = 0; i < chars.length; i++) {
						if (!( ('0' <= chars[i] && chars[i] <= '9') || (e.start > 0 && (chars[i] == '.'
								|| chars[i] == ',')))) {
							e.doit = false;
							return;
						}
					}
				}
			});
		}
		
		public void changeVisibility(boolean show){
			removeAllChildrens();
			if (show) {
				createContents();
			}
			getParent().layout(true, true);
		}
		
		private void removeAllChildrens(){
			for (Control c : getChildren()) {
				c.dispose();
				
			}
		}
		
		public boolean isSelected() {
			return isVisible() && checkExcludeKonsByMoney != null
					&& !checkExcludeKonsByMoney.isDisposed() && checkExcludeKonsByMoney.getSelection();
		}
		
		public Money getMoney(){
			if (isVisible() && txtExcludeKonsByMoney != null
					&& !txtExcludeKonsByMoney.isDisposed()) {
				return txtExcludeKonsByMoney.getMoney(true);
			}
			return null;
		}
	}
}
