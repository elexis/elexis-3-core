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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.Result.msg;
import ch.rgw.tools.TimeTool;

public class BillingProposalView extends ViewPart {
	public static final String ID = "ch.elexis.core.ui.views.rechnung.BillingProposalView"; //$NON-NLS-1$
	
	private TableViewer viewer;
	private BillingProposalViewerComparator comparator;
	
	private Color lightRed = UiDesk.getColorFromRGB("ff8d8d");
	private Color lightGreen = UiDesk.getColorFromRGB("a6ffaa");
	
	@Override
	public void createPartControl(Composite parent){
		viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI | SWT.VIRTUAL);
		
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(new BillingInformationContentProvider(viewer));
		comparator = new BillingProposalViewerComparator();
		viewer.setComparator(comparator);
		
		TableViewerColumn patNameColumn = new TableViewerColumn(viewer, SWT.NONE);
		patNameColumn.getColumn().setWidth(175);
		patNameColumn.getColumn().setText("Patient");
		patNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).getPatientName();
				} else {
					return super.getText(element);
				}
			}
		});
		patNameColumn.getColumn().addSelectionListener(getSelectionAdapter(0));
		
		TableViewerColumn patNrColumn = new TableViewerColumn(viewer, SWT.NONE);
		patNrColumn.getColumn().setWidth(50);
		patNrColumn.getColumn().setText("PatNr");
		patNrColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BillingInformation) {
					return Integer.toString(((BillingInformation) element).getPatientNr());
				} else {
					return super.getText(element);
				}
			}
		});
		patNrColumn.getColumn().addSelectionListener(getSelectionAdapter(1));
		
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
		dateColumn.getColumn().addSelectionListener(getSelectionAdapter(2));
		
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
			public Color getBackground(Object element){
				if (element instanceof BillingInformation) {
					return ((BillingInformation) element).isOk() ? lightGreen : lightRed;
				} else {
					return super.getForeground(element);
				}
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
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
		
		viewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e){
				if (e.keyCode == SWT.F5) {
					refresh();
				}
			}
		});
		
		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);
		getSite().setSelectionProvider(viewer);
	}
	
	private SelectionAdapter getSelectionAdapter(int index){
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}
	
	@Override
	public void setFocus(){
		viewer.getControl().setFocus();
	}
	
	/**
	 * Set a {@link List} of {@link Konsultation} as the input for the viewer.
	 * 
	 * @param proposal
	 */
	public void setInput(List<Konsultation> proposal){
		viewer.setInput(proposal);
	}
	
	/**
	 * Refresh the current content of the viewer.
	 */
	public void refresh(){
		((BillingInformationContentProvider) viewer.getContentProvider()).refresh();
		viewer.refresh();
	}
	
	/**
	 * Get a {@link List} of all {@link Konsultation} instances of the viewer, erroneous and valid.
	 */
	public List<Konsultation> getToBill(){
		List<BillingInformation> content =
			((BillingInformationContentProvider) viewer.getContentProvider()).getCurrentContent();
		if (content != null && !content.isEmpty()) {
			return content.parallelStream().map(bi -> bi.getKonsultation())
				.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
	public void removeToBill(List<BillingInformation> list){
		((BillingInformationContentProvider) viewer.getContentProvider()).removeContent(list);
		viewer.refresh();
	}
	
	public ProposalLetter getToPrint(){
		BillingInformationContentProvider contentProvider =
			((BillingInformationContentProvider) viewer.getContentProvider());
		contentProvider.resolveAll();
		
		List<BillingInformation> content = contentProvider.getCurrentContent();
		BillingProposalViewerComparator comparator =
			(BillingProposalViewerComparator) viewer.getComparator();
		content.sort(comparator);
		
		ProposalLetter ret = new ProposalLetter();
		if (content != null && !content.isEmpty()) {
			ret.setProposal(content);
		} else {
			ret.setProposal(Collections.emptyList());
		}
		return ret;
	}
	
	@XmlRootElement
	public static class ProposalLetter {
		private List<BillingInformation> proposal;
		
		public ProposalLetter(){
		}
		
		public void setProposal(List<BillingInformation> proposal){
			this.proposal = proposal;
		}
		
		public List<BillingInformation> getProposal(){
			return proposal;
		}
	}
	
	public static class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
		
		private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		
		@Override
		public String marshal(LocalDate date) throws Exception{
			return formatter.format(date);
		}
		
		@Override
		public LocalDate unmarshal(String string) throws Exception{
			return LocalDate.parse(string, formatter);
		}
		
	}
	
	/**
	 * View specific model class, including multi threaded property loading.
	 * 
	 * @author thomas
	 *
	 */
	@XmlRootElement(name = "proposal")
	public static class BillingInformation {
		@XmlTransient
		private static ExecutorService executorService = Executors.newFixedThreadPool(8);
		@XmlTransient
		private volatile boolean resolved;
		@XmlTransient
		private volatile boolean resolving;
		@XmlTransient
		private StructuredViewer viewer;
		@XmlTransient
		private Fall fall;
		@XmlTransient
		private Konsultation konsultation;
		@XmlElement
		@XmlJavaTypeAdapter(LocalDateAdapter.class)
		private LocalDate date;
		@XmlElement
		private int patientNr = -1;
		@XmlElement
		private String patientName;
		@XmlElement
		private String insurerName;
		@XmlElement
		private String accountingSystem;
		@XmlElement
		private String amountTotal;
		@XmlElement
		private String checkResultMessage;
		@XmlElement
		private boolean checkResult;
		
		public BillingInformation(){
		}
		
		public BillingInformation(StructuredViewer viewer, Fall fall, Konsultation konsultation){
			this.viewer = viewer;
			this.fall = fall;
			this.konsultation = konsultation;
			
			resolved = false;
			resolving = false;
			date = new TimeTool(konsultation.getDatum()).toLocalDate();
		}
		
		public String getPatientName(){
			if (patientName == null && fall != null) {
				patientName = fall.getPatient().getPersonalia();
			}
			return patientName;
		}
		
		public Konsultation getKonsultation(){
			return konsultation;
		}
		
		public Integer getPatientNr(){
			if (patientNr == -1) {
				try {
					patientNr = Integer.parseInt(fall.getPatient().getPatCode());
				} catch (NumberFormatException e) {
					patientNr = -1;
				}
			}
			return patientNr;
		}
		
		public LocalDate getDate(){
			return date;
		}
		
		public synchronized boolean isResolved(){
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
			return resolved;
		}
		
		public void resolve(){
			executorService.execute(new ResolveLazyFieldsRunnable(null, this));
			while (!isResolved()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		
		public synchronized void refresh(){
			resolved = false;
		}
		
		public String getCheckResultMessage(){
			if (!isResolved()) {
				return "...";
			} else {
				return checkResultMessage;
			}
		}
		
		public Boolean isOk(){
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
				item.resolving = false;
				if (viewer != null) {
					updateViewer();
				}
			}
			
			private void resolveCheckResult(){
				Result<Konsultation> result = BillingUtil.getBillableResult(item.konsultation);
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
				Kontakt costBearer = item.fall.getCostBearer();
				if (costBearer != null) {
					item.insurerName = costBearer.getLabel(true);
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
					control.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run(){
							if (!control.isDisposed() && control.isVisible()) {
								viewer.refresh(item, true);
							}
						}
					});
				}
			}
		}
	}
	
	/**
	 * View specific {@link IStructuredContentProvider} implementation for mapping a list of
	 * {@link Konsultation} to a list of {@link BillingInformation}.
	 * 
	 * @author thomas
	 *
	 */
	private class BillingInformationContentProvider implements IStructuredContentProvider {
		
		private StructuredViewer viewer;
		private List<BillingInformation> currentContent;
		
		public BillingInformationContentProvider(StructuredViewer viewer){
			this.viewer = viewer;
		}
		
		public void removeContent(List<BillingInformation> list){
			currentContent.removeAll(list);
		}
		
		public void resolveAll(){
			currentContent.parallelStream().forEach(bi -> {
				// touch patient number
				bi.getPatientNr();
				if (!bi.isResolved()) {
					bi.resolve();
				}
			});
		}
		
		/**
		 * Refresh the current list of {@link BillingInformation}
		 */
		public void refresh(){
			if (currentContent != null) {
				currentContent = currentContent.parallelStream()
					.filter(bi -> bi.getKonsultation().getRechnung() == null)
					.collect(Collectors.toList());
				currentContent.parallelStream().forEach(bi -> bi.refresh());
			}
		}
		
		@Override
		public Object[] getElements(Object inputElement){
			if (inputElement instanceof List<?>) {
				return currentContent.toArray();
			}
			return Collections.emptyList().toArray();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			if (newInput instanceof List<?>) {
				currentContent = ((List<Konsultation>) newInput).parallelStream()
					.map(k -> new BillingInformation(this.viewer, k.getFall(), k))
					.collect(Collectors.toList());
			}
		}
		
		public List<BillingInformation> getCurrentContent(){
			if (currentContent != null) {
				return currentContent;
			}
			return Collections.emptyList();
		}
		
		@Override
		public void dispose(){
			viewer = null;
			currentContent = null;
		}
	}
	
	/**
	 * View specific {@link ViewerComparator} implementation.
	 * 
	 * @author thomas
	 *
	 */
	private class BillingProposalViewerComparator extends ViewerComparator
			implements Comparator<BillingInformation> {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;
		
		public BillingProposalViewerComparator(){
			this.propertyIndex = 0;
			direction = DESCENDING;
		}
		
		public int getDirection(){
			return direction == 1 ? SWT.DOWN : SWT.UP;
		}
		
		public void setColumn(int column){
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}
		
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			BillingInformation left = (BillingInformation) e1;
			BillingInformation right = (BillingInformation) e2;
			return compare(left, right);
		}
		
		@Override
		public int compare(BillingInformation left, BillingInformation right){
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = right.getPatientName().compareTo(left.getPatientName());
				break;
			case 1:
				rc = right.getPatientNr().compareTo(left.getPatientNr());
				break;
			case 2:
				rc = right.getDate().compareTo(left.getDate());
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
	}
}
