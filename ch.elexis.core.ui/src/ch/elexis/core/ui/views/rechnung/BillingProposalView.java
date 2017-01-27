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
package ch.elexis.core.ui.views.rechnung;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.Result.msg;
import ch.rgw.tools.TimeTool;

public class BillingProposalView extends ViewPart {
	public static final String ID = "ch.elexis.core.ui.views.rechnung.BillingProposalView"; //$NON-NLS-1$
	
	private TableViewer viewer;
	
	@Override
	public void createPartControl(Composite parent){
		viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(new BillingInformationContentProvider(viewer));
		
		TableViewerColumn patNameColumn = new TableViewerColumn(viewer, SWT.NONE);
		patNameColumn.getColumn().setWidth(175);
		patNameColumn.getColumn().setText("Patient");
		patNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).getPatient().getLabel(true);
				} else {
					return super.getText(element);
				}
			}
		});
		
		TableViewerColumn patNrColumn = new TableViewerColumn(viewer, SWT.NONE);
		patNrColumn.getColumn().setWidth(50);
		patNrColumn.getColumn().setText("PatNr");
		patNrColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).getPatient().getPatCode();
				} else {
					return super.getText(element);
				}
			}
		});
		
		TableViewerColumn dateColumn = new TableViewerColumn(viewer, SWT.NONE);
		dateColumn.getColumn().setWidth(75);
		dateColumn.getColumn().setText("Datum");
		dateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).getDate()
						.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
				} else {
					return super.getText(element);
				}
			}
		});
		
		TableViewerColumn accountingSystemColumn = new TableViewerColumn(viewer, SWT.NONE);
		accountingSystemColumn.getColumn().setWidth(75);
		accountingSystemColumn.getColumn().setText("Fall");
		accountingSystemColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).getAccountingSystem();
				} else {
					return super.getText(element);
				}
			}
		});
		
		TableViewerColumn insurerColumn = new TableViewerColumn(viewer, SWT.NONE);
		insurerColumn.getColumn().setWidth(175);
		insurerColumn.getColumn().setText("Versicherer");
		insurerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).getInsurer();
				} else {
					return super.getText(element);
				}
			}
		});
		
		TableViewerColumn totalColumn = new TableViewerColumn(viewer, SWT.NONE);
		totalColumn.getColumn().setWidth(75);
		totalColumn.getColumn().setText("Total");
		totalColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).getTotal();
				} else {
					return super.getText(element);
				}
			}
		});
		
		TableViewerColumn checkResultColumn = new TableViewerColumn(viewer, SWT.NONE);
		checkResultColumn.getColumn().setWidth(200);
		checkResultColumn.getColumn().setText("Prüfergebnis");
		checkResultColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).getCheckResultMessage();
				} else {
					return super.getText(element);
				}
			}
			
			@Override
			public Color getForeground(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).isOk() ? UiDesk.getColor(UiDesk.COL_GREEN)
							: UiDesk.getColor(UiDesk.COL_RED);
				} else {
					return super.getForeground(element);
				}
			}
		});
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event){
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && !selection.isEmpty()) {
					if (selection.getFirstElement() instanceof BillingInformation) {
						Konsultation kons =
							((BillingInformation) selection.getFirstElement()).getKonsultation();
						ElexisEventDispatcher.fireSelectionEvent(kons);
					}
				}
			}
		});
	}
	
	@Override
	public void setFocus(){
		viewer.getControl().setFocus();
	}
	
	public void setInput(List<Konsultation> proposal){
		viewer.setInput(proposal);
	}
	
	private static class BillingInformation {
		
		private static ExecutorService executorService = Executors.newCachedThreadPool();
		
		private boolean resolved;
		private boolean resolving;
		
		private StructuredViewer viewer;
		private Fall fall;
		private Konsultation konsultation;
		
		private LocalDate date;
		
		private String insurerName;
		private String accountingSystem;
		private String amountTotal;
		private String checkResultMessage;
		private boolean checkResult;
		
		public BillingInformation(StructuredViewer viewer, Fall fall, Konsultation konsultation){
			this.viewer = viewer;
			this.fall = fall;
			this.konsultation = konsultation;
			
			resolved = false;
			resolving = false;
			date = new TimeTool(konsultation.getDatum()).toLocalDate();
		}
		
		public Konsultation getKonsultation(){
			return konsultation;
		}
		
		public Patient getPatient(){
			return fall.getPatient();
		}
		
		public LocalDate getDate(){
			return date;
		}
		
		public boolean isResolved(){
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
			return resolved;
		}
		
		public String getCheckResultMessage(){
			if (!isResolved()) {
				return "...";
			} else {
				return checkResultMessage;
			}
		}
		
		public boolean isOk(){
			if (!isResolved()) {
				return false;
			} else {
				return checkResult;
			}
		}
		
		public String getTotal(){
			if (!isResolved()) {
				return "...";
			} else {
				return amountTotal;
			}
		}
		
		public String getAccountingSystem(){
			if (!isResolved()) {
				return "...";
			} else {
				return accountingSystem;
			}
		}
		
		public String getInsurer(){
			if (!isResolved()) {
				return "...";
			} else {
				return insurerName;
			}
		}
		
		private static class ResolveLazyFieldsRunnable implements Runnable {
			private BillingInformation item;
			private StructuredViewer viewer;
			
			public ResolveLazyFieldsRunnable(StructuredViewer viewer, BillingInformation item){
				this.item = item;
				this.viewer = viewer;
			}
			
			@Override
			public void run(){
				resolveInsurer();
				resolveAccountingSystem();
				resolveTotal();
				resolveCheckResult();
				item.resolved = true;
				updateViewer();
			}
			
			private void resolveCheckResult(){
				Result<Konsultation> result = BillingUtil.check(item.konsultation);
				if (result.isOK()) {
					item.checkResultMessage = "Ok";
					item.checkResult = true;
				} else {
					StringBuilder sb = new StringBuilder();
					for (@SuppressWarnings("rawtypes")
					msg message : result.getMessages()) {
						if (message.getSeverity() != SEVERITY.OK) {
							if (sb.length() > 0) {
								sb.append(" / ");
							}
							sb.append(message.getText());
						}
					}
					item.checkResultMessage = sb.toString();
					item.checkResult = false;
				}
			}
			
			private void resolveTotal(){
				Money total = BillingUtil.getTotal(item.konsultation);
				item.amountTotal = total.getAmountAsString();
			}
			
			private void resolveInsurer(){
				String insurerId = (String) item.fall.getInfoElement("Kostenträger");
				if (insurerId != null && !insurerId.isEmpty()) {
					item.insurerName = Kontakt.load(insurerId).getLabel(true);
				} else {
					item.insurerName = "";
				}
			}

			private void resolveAccountingSystem(){
				item.accountingSystem = item.fall.getAbrechnungsSystem();
			}

			private void updateViewer(){
				Control control = viewer.getControl();
				if (control != null && !control.isDisposed()) {
					viewer.getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run(){
							if (control.isVisible()) {
								viewer.refresh(item, true);
							}
						}
					});
				}
			}
		}
	}
	
	private class BillingInformationContentProvider extends ArrayContentProvider {
		
		private StructuredViewer viewer;

		public BillingInformationContentProvider(StructuredViewer viewer){
			this.viewer = viewer;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement){
			if (inputElement instanceof List<?>) {
				List<BillingInformation> billingInfoList = ((List<Konsultation>) inputElement)
					.parallelStream()
					.map(k -> new BillingInformation(viewer, k.getFall(), k)).collect(Collectors.toList());
				return billingInfoList.toArray();
			}
			return super.getElements(inputElement);
		}
		
	}
}
