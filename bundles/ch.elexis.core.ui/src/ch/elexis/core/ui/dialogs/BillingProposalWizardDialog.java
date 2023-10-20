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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.ui.e4.fieldassist.PatientSearchToken;
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
	private boolean series;

	private Button insurerOnly;
	private KontaktSelectionComposite insurerSelection;

	private Button timeSpanOnly;
	private TimeSpanSelectionComposite timeSpanSelection;

	private Button beforeTimeOnly;

	private FilterByMoneyComposite filterSeriesByMoneyComposite;

	private FilterByMoneyComposite filterKonsByMoneyComposite;

	private DaysOrDateSelectionComposite beforeDaysOrDate;

	private Button converageMarkedOnly;

	private Button mandatorOnly;
	private GenericSelectionComposite mandatorSelector;

	private Button patientOnly;
	private GenericSelectionComposite patientSelector;

	private Button accountingOnly;
	private GenericSelectionComposite accountingSelector;

	private Button errorneousOnly;

	public BillingProposalWizardDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Rechnungs-Vorschlag Auswahl");
		setMessage(Messages.KonsZumVerrechnenWizardDialog_createProposal);
		getShell().setText("Rechnungs-Vorschlag");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite ret = (Composite) super.createDialogArea(parent);
		Composite content = new Composite(ret, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		content.setLayoutData(new GridData(GridData.FILL_BOTH));

		timeSpanOnly = new Button(content, SWT.CHECK);
		timeSpanOnly.setText("Offene Konsultationen innerhalb des Zeitraums");
		timeSpanOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (timeSpanOnly.getSelection()) {
					beforeTimeOnly.setSelection(false);
				}
				filterKonsByMoneyComposite.changeVisibility(timeSpanOnly.getSelection());
			}
		});
		timeSpanSelection = new TimeSpanSelectionComposite(content, SWT.NONE);
		timeSpanSelection.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		timeSpanSelection.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					timeSpanOnly.setSelection(true);
					beforeTimeOnly.setSelection(false);
				} else {
					timeSpanOnly.setSelection(false);
				}
			}
		});
		LocalDate dateNow = LocalDate.now();
		timeSpanSelection.setTimeSpan(new TimeSpan(new TimeTool(dateNow.withDayOfMonth(1)), new TimeTool(dateNow)));

		filterKonsByMoneyComposite = new FilterByMoneyComposite(content, false);

		beforeTimeOnly = new Button(content, SWT.CHECK);
		beforeTimeOnly.setText("Offene Behandlungsserien vor Tagen oder Datum");
		beforeTimeOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (beforeTimeOnly.getSelection()) {
					timeSpanOnly.setSelection(false);
				}
				filterSeriesByMoneyComposite.changeVisibility(beforeTimeOnly.getSelection());
			}
		});
		beforeDaysOrDate = new DaysOrDateSelectionComposite(content, SWT.NONE);
		beforeDaysOrDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		beforeDaysOrDate.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					beforeTimeOnly.setSelection(true);
					timeSpanOnly.setSelection(false);
				} else {
					beforeTimeOnly.setSelection(false);
				}
			}
		});
		beforeDaysOrDate.setDate(LocalDate.now().minusDays(30));

		filterSeriesByMoneyComposite = new FilterByMoneyComposite(content, true);

		converageMarkedOnly = new Button(content, SWT.CHECK);
		converageMarkedOnly.setText(Messages.KonsZumVerrechnenWizardDialog_selectCasesToCharge);
		new Label(content, SWT.NONE);

		insurerOnly = new Button(content, SWT.CHECK);
		insurerOnly.setText("nur von folgendem Versicherer");
		insurerSelection = new KontaktSelectionComposite(content, SWT.NONE | SWT.MULTI);
		insurerSelection.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		insurerSelection.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					insurerOnly.setSelection(true);
				} else {
					insurerOnly.setSelection(false);
				}
			}
		});

		mandatorOnly = new Button(content, SWT.CHECK);
		mandatorOnly.setText("nur von folgenden Mandanten");
		mandatorSelector = new GenericSelectionComposite(content, SWT.NONE,
				ch.elexis.core.l10n.Messages.Core_Select_Mandator,
				ch.elexis.core.ui.actions.Messages.GlobalActions_ChangeMandator,
				ch.elexis.core.ui.actions.Messages.GlobalActions_ChangeMandatorMessage);
		mandatorSelector.setInput(new Query<>(Mandant.class).execute());
		mandatorSelector.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		mandatorSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					mandatorOnly.setSelection(true);
				} else {
					mandatorOnly.setSelection(false);
				}
			}
		});

		patientOnly = new Button(content, SWT.CHECK);
		patientOnly.setText("nur von folgenden Patienten");
		patientSelector = new GenericSelectionComposite(content, SWT.NONE,
				ch.elexis.core.l10n.Messages.Core_Select_Patient,
				ch.elexis.core.l10n.Messages.Core_Select_Patient, ch.elexis.core.l10n.Messages.Core_Select_Patient);
		Function<String, List<?>> inputFunction = (String s) -> {
			IQuery<IPatient> query = CoreModelServiceHolder.get().getQuery(IPatient.class);
			query.and(ModelPackage.Literals.ICONTACT__PATIENT, COMPARATOR.EQUALS, true);
			if (s != null && s.length() > 2) {
				List<PatientSearchToken> searchParts = PatientSearchToken
						.getPatientSearchTokens(s.toLowerCase().split(StringUtils.SPACE));
				searchParts.forEach(st -> st.apply(query));
			}
			query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION1, ORDER.ASC);
			query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION2, ORDER.ASC);
			return query.execute();
		};
		patientSelector.setInputFunction(inputFunction);
		patientSelector.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		patientSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() != null && !event.getSelection().isEmpty()) {
					patientOnly.setSelection(true);
				} else {
					patientOnly.setSelection(false);
				}
			}
		});

		accountingOnly = new Button(content, SWT.CHECK);
		accountingOnly.setText("nur von folgenden Abrechnungssystemen");
		accountingSelector = new GenericSelectionComposite(content, SWT.NONE,
				ch.elexis.core.l10n.Messages.GenericSearchSelectionDialog_BillingSystemSelection,
				ch.elexis.core.l10n.Messages.GenericSearchSelectionDialog_BillingSystemSelection,
				ch.elexis.core.l10n.Messages.GenericSearchSelectionDialog_ChangeBillingSystemMessage);
		accountingSelector.setInput(Arrays.asList(BillingSystem.getAbrechnungsSysteme()));
		accountingSelector.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		accountingSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
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
		new Label(content, SWT.NONE); // GAPS
		return ret;
	}

	@Override
	protected void okPressed() {
		ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell());
		QueryProposalRunnable runnable = new QueryProposalRunnable();
		try {
			progress.run(true, true, runnable);
			if (runnable.isCanceled()) {
				return;
			} else {
				proposal = runnable.getProposal();
				series = runnable.isSeries();
			}
		} catch (InvocationTargetException | InterruptedException e) {
			LoggerFactory.getLogger(BillingProposalWizardDialog.class).error("Error running proposal query", e); //$NON-NLS-1$
			MessageDialog.openError(getShell(), "Fehler", "Fehler beim Ausführen des Rechnungs-Vorschlags.");
			return;
		}

		super.okPressed();
	}

	public Optional<List<Konsultation>> getProposal() {
		return Optional.ofNullable(proposal);
	}

	public boolean isSeries() {
		return series;
	}

	private class QueryProposalRunnable implements IRunnableWithProgress {

		private boolean canceled = false;
		private List<Konsultation> proposal = new ArrayList<>();

		private IFilter insurerOnlyFilter;
		private IFilter accountingOnlyFilter;
		private IFilter errorneousOnlyFilter;
		private IFilter mandatorsOnlyFilter;
		private Query<Konsultation> query;

		private boolean addSeries;
		private Money filterSeriesByMoney;
		private Money filterKonsByMoney;
		private boolean filterByMoneyHigher;

		public QueryProposalRunnable() {
			query = new Query<>(Konsultation.class, "BEHANDLUNGEN", false, //$NON-NLS-1$
					new String[] { Konsultation.DATE, Konsultation.FLD_TIME, Konsultation.FLD_MANDATOR_ID,
							Konsultation.FLD_BILL_ID, Konsultation.FLD_CASE_ID });
			query.add(Konsultation.FLD_BILL_ID, Query.EQUALS, null);
			query.add(Konsultation.FLD_BILLABLE, Query.EQUALS, "1"); //$NON-NLS-1$
			if (timeSpanOnly.getSelection()) {
				IStructuredSelection selection = (IStructuredSelection) timeSpanSelection.getSelection();
				if (selection != null && !selection.isEmpty()) {
					TimeSpan timeSpan = (TimeSpan) selection.getFirstElement();
					query.add(Konsultation.DATE, Query.GREATER_OR_EQUAL, timeSpan.from.toString(TimeTool.DATE_COMPACT));
					query.add(Konsultation.DATE, Query.LESS_OR_EQUAL, timeSpan.until.toString(TimeTool.DATE_COMPACT));
				}
			}
			if (filterKonsByMoneyComposite.isSelected()) {
				filterKonsByMoney = filterKonsByMoneyComposite.getMoney();
				filterByMoneyHigher = filterKonsByMoneyComposite.isHigher();
			}
			if (beforeTimeOnly.getSelection()) {
				addSeries = true;
				IStructuredSelection selection = (IStructuredSelection) beforeDaysOrDate.getSelection();
				if (selection != null && !selection.isEmpty()) {
					LocalDate fromDate = (LocalDate) selection.getFirstElement();
					TimeTool fromTool = new TimeTool(fromDate);
					query.add(Konsultation.DATE, Query.LESS_OR_EQUAL, fromTool.toString(TimeTool.DATE_COMPACT));
					if (filterSeriesByMoneyComposite.isSelected()) {
						filterSeriesByMoney = filterSeriesByMoneyComposite.getMoney();
						filterByMoneyHigher = filterSeriesByMoneyComposite.isHigher();
					}
				}
			}
			if (converageMarkedOnly.getSelection()) {
				Query<Fall> coverageQuery = new Query<>(Fall.class);
				coverageQuery.add(Fall.FLD_RN_PLANUNG, Query.LESS_OR_EQUAL,
						new TimeTool().toString(TimeTool.DATE_COMPACT));
				coverageQuery.add(Fall.FLD_RN_PLANUNG, Query.NOT_EQUAL, StringUtils.EMPTY);
				List<Fall> coverages = coverageQuery.execute();
				if (coverages.isEmpty()) {
					query.addToken("1 = 2");
				} else {
					query.addToken(Konsultation.FLD_CASE_ID + " IN ("
							+ coverages.stream().map(c -> "'" + c.getId() + "'").collect(Collectors.joining(","))
							+ ")");
				}
			}
			if (mandatorOnly.getSelection()) {
				IStructuredSelection selection = (IStructuredSelection) mandatorSelector.getSelection();
				if (selection != null && !selection.isEmpty()) {
					@SuppressWarnings("unchecked")
					List<Mandant> mandators = selection.toList();
					query.startGroup();
					for (int i = 0; i < mandators.size(); i++) {
						if (i > 0) {
							query.or();
						}
						query.add(Konsultation.FLD_MANDATOR_ID, Query.EQUALS, mandators.get(i).getId());
					}
					query.endGroup();
				}
				// add filter for series
				mandatorsOnlyFilter = new IFilter() {

					private Set<String> mandatorIds = null;

					@SuppressWarnings("unchecked")
					@Override
					public boolean select(Object element) {
						if(mandatorIds == null) {
							mandatorIds = new HashSet<String>();
							IStructuredSelection selection = (IStructuredSelection) mandatorSelector.getSelection();
							if (selection != null && !selection.isEmpty()) {
								selection.forEach(o -> mandatorIds.add(((Mandant) o).getId()));
							}
						}
						return mandatorIds.contains(((Konsultation) element).getMandant().getId());
					}
				};
			}

			if (patientOnly.getSelection()) {
				IStructuredSelection selection = (IStructuredSelection) patientSelector.getSelection();
				if (selection != null && !selection.isEmpty()) {
					@SuppressWarnings("unchecked")
					List<IPatient> patients = selection.toList();
					List<ICoverage> coverages = patients.stream().flatMap(p -> p.getCoverages().stream())
							.collect(Collectors.toList());
					if (coverages.isEmpty()) {
						query.addToken("1 = 2");
					} else {
						query.addToken(Konsultation.FLD_CASE_ID + " IN ("
								+ coverages.stream().map(c -> "'" + c.getId() + "'").collect(Collectors.joining(","))
								+ ")");
					}
				}
			}

			if (insurerOnly.getSelection()) {
				insurerOnlyFilter = new IFilter() {

					@SuppressWarnings("unchecked")
					private List<Kontakt> insurers = (List<Kontakt>) ((IStructuredSelection) insurerSelection
							.getSelection()).toList();

					@Override
					public boolean select(Object element) {
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
					private List<String> accountings = ((IStructuredSelection) accountingSelector.getSelection())
							.toList();

					@Override
					public boolean select(Object element) {
						Fall fall = ((Konsultation) element).getFall();
						String accounting = fall.getAbrechnungsSystem();
						return accountings.contains(accounting);
					}
				};
			}
			if (errorneousOnly.getSelection()) {
				errorneousOnlyFilter = new IFilter() {
					@Override
					public boolean select(Object element) {
						return !BillingUtil.getBillableResult((Konsultation) element).isOK();
					}
				};
			}

		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
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
			if (addSeries) {
				progress.setTaskName("Behandlungsserien laden");
				HashSet<String> knownIds = new HashSet<>();
				knownIds.addAll(proposal.parallelStream().map(k -> k.getId()).collect(Collectors.toList()));
				ArrayList<Konsultation> proposalCopy = new ArrayList<>(proposal);
				proposalCopy.forEach(k -> {
					List<Konsultation> series = getSeries(k.getFall());
					// calculate money for kons series
					if (filterSeriesByMoney != null) {
						Money totalForKonsSerie = getSeriesTotal(series);
						if ((filterByMoneyHigher && filterSeriesByMoney.isMoreThan(totalForKonsSerie))
								|| (!filterByMoneyHigher && totalForKonsSerie.isMoreThan(filterSeriesByMoney))) {
							// exclude whole series
							knownIds.addAll(series.parallelStream().map(sk -> sk.getId()).collect(Collectors.toList()));
							proposal.remove(k);
						}
					}
					series.forEach(sk -> {
						if (!knownIds.contains(sk.getId())) {
							// only look at sk once
							knownIds.add(sk.getId());
							if (applyFilters(sk)) {
								proposal.add(sk);
							}
						}
					});
					progress.worked(1);
					if (progress.isCanceled()) {
						canceled = true;
						return;
					}
				});
			}
			if (filterKonsByMoney != null) {
				ArrayList<Konsultation> proposalCopy = new ArrayList<>(proposal);
				proposalCopy.forEach(k -> {
					IEncounter encounter = NoPoUtil.loadAsIdentifiable(k, IEncounter.class).orElse(null);
					if (encounter != null) {
						Money konsTotal = EncounterServiceHolder.get().getSales(encounter);
						if ((filterByMoneyHigher && filterKonsByMoney.isMoreThan(konsTotal))
								|| (!filterByMoneyHigher && konsTotal.isMoreThan(filterKonsByMoney))) {
							proposal.remove(k);
						}
					}
				});
			}
			// filter coverage billing date
			ArrayList<Konsultation> proposalCopy = new ArrayList<>(proposal);
			TimeTool now = new TimeTool();
			proposalCopy.forEach(k -> {
				TimeTool billingDate = k.getFall().getBillingDate();
				if (billingDate != null && billingDate.isAfter(now)) {
					proposal.remove(k);
				}
			});
			monitor.done();
		}

		private Money getSeriesTotal(List<Konsultation> series) {
			Money ret = new Money();
			series.forEach(sk -> ret.addMoney(BillingUtil.getTotal(sk)));
			return ret;
		}

		/**
		 * Apply the filters to the {@link Konsultation} return false if any filter
		 * matches, true if it passes all filters. Filters can have performance
		 * implications.
		 *
		 * @param konsultation
		 * @return
		 */
		private boolean applyFilters(Konsultation konsultation) {
			if (insurerOnlyFilter != null && !insurerOnlyFilter.select(konsultation)) {
				return false;
			}
			if (accountingOnlyFilter != null && !accountingOnlyFilter.select(konsultation)) {
				return false;
			}
			if (errorneousOnlyFilter != null && !errorneousOnlyFilter.select(konsultation)) {
				return false;
			}
			if (mandatorsOnlyFilter != null && !mandatorsOnlyFilter.select(konsultation)) {
				return false;
			}
			return true;
		}

		private List<Konsultation> getSeries(Fall fall) {
			return Arrays.asList(fall.getBehandlungen(false)).parallelStream().filter(k -> k.isBillable())
					.filter(k -> k.getRechnung() == null || !k.getRechnung().exists()).collect(Collectors.toList());
		}

		public boolean isCanceled() {
			return canceled;
		}

		public List<Konsultation> getProposal() {
			return proposal;
		}

		public boolean isSeries() {
			return addSeries;
		}
	}

	private class FilterByMoneyComposite extends Composite {
		private MoneyInput txtFilterKonsByMoney;
		private Button filterKonsByMoney;
		private ComboViewer lowerOrHigher;
		private boolean series;

		public FilterByMoneyComposite(Composite parent, boolean series) {
			super(parent, SWT.NONE);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			setLayout(SWTHelper.createGridLayout(true, 3));
			this.series = series;
		}

		private void createContents() {
			filterKonsByMoney = new Button(this, SWT.CHECK);
			if (series) {
				filterKonsByMoney.setText("Offene Behandlungsserien");
			} else {
				filterKonsByMoney.setText("Offene Behandlungen");
			}
			lowerOrHigher = new ComboViewer(this, SWT.BORDER);
			lowerOrHigher.setContentProvider(ArrayContentProvider.getInstance());
			lowerOrHigher.setInput(
					new String[] { ch.elexis.core.l10n.Messages.Core_Higher, ch.elexis.core.l10n.Messages.Core_Lower });
			lowerOrHigher.setSelection(new StructuredSelection(ch.elexis.core.l10n.Messages.Core_Higher));
			lowerOrHigher.getControl().setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

			Composite c = new Composite(this, SWT.NONE);
			c.setLayout(new GridLayout(2, false));
			c.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
			Label lblBeforeTimeOnlyValueLessThen = new Label(c, SWT.NONE);
			lblBeforeTimeOnlyValueLessThen.setText("Betrag CHF");

			txtFilterKonsByMoney = new MoneyInput(c);
			txtFilterKonsByMoney.getControl().addListener(SWT.Verify, new Listener() {
				public void handleEvent(Event e) {
					// limits only the input the value validation happens at focus lost
					String string = e.text;

					char[] chars = new char[string.length()];
					string.getChars(0, chars.length, chars, 0);
					for (int i = 0; i < chars.length; i++) {
						if (!(('0' <= chars[i] && chars[i] <= '9')
								|| (e.start > 0 && (chars[i] == '.' || chars[i] == ',')))) {
							e.doit = false;
							return;
						}
					}
				}
			});
		}

		public void changeVisibility(boolean show) {
			removeAllChildrens();
			if (show) {
				createContents();
			}
			getParent().layout(true, true);
		}

		private void removeAllChildrens() {
			for (Control c : getChildren()) {
				c.dispose();

			}
		}

		public boolean isSelected() {
			return isVisible() && filterKonsByMoney != null && !filterKonsByMoney.isDisposed()
					&& filterKonsByMoney.getSelection();
		}

		public boolean isHigher() {
			if (!lowerOrHigher.getStructuredSelection().isEmpty()) {
				return ((String) lowerOrHigher.getStructuredSelection().getFirstElement())
						.equals(ch.elexis.core.l10n.Messages.Core_Higher);
			}
			return true;
		}

		public Money getMoney() {
			if (isVisible() && txtFilterKonsByMoney != null && !txtFilterKonsByMoney.isDisposed()) {
				return txtFilterKonsByMoney.getMoney(true);
			}
			return null;
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
