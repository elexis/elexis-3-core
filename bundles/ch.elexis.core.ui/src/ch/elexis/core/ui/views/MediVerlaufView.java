/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * A view to display all medikaments administered or prescribed.
 * 
 * @author Gerry
 * 
 */
public class MediVerlaufView extends ViewPart implements IActivationListener {
	TableViewer tv;
	ArrayList<MediAbgabe> mListe = new ArrayList<MediAbgabe>();
	private static final String[] columns = {
		Messages.MediVerlaufView_dateFrom, Messages.MediVerlaufView_dateUntil,
		Messages.MediVerlaufView_medicament, Messages.MediVerlaufView_dosage
	}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final int[] colwidth = {
		90, 90, 300, 200
	};
	
	private ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		
		@Override
		public void runInUi(ElexisEvent ev){
			reload();
		}
	};
	private static int sortCol = 0;
	private static boolean revert = false;
	public static final String TOOPEN = " ... ";
	MediSorter sorter = new MediSorter();
	
	public MediVerlaufView(){
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void createPartControl(final Composite parent){
		parent.setLayout(new FillLayout());
		tv = new TableViewer(parent, SWT.NONE);
		Table table = tv.getTable();
		for (int i = 0; i < columns.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.NONE);
			tc.setText(columns[i]);
			tc.setWidth(colwidth[i]);
			tc.setData(i);
			tc.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(final SelectionEvent e){
					int i = (Integer) ((TableColumn) e.getSource()).getData();
					if (sortCol == i) {
						revert = !revert;
					} else {
						revert = false;
					}
					sortCol = i;
					reload();
				}
				
			});
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tv.setUseHashlookup(true);
		tv.setContentProvider(new MediVerlaufContentProvider());
		tv.setLabelProvider(new MediVerlaufLabelProvider());
		GlobalEventDispatcher.addActivationListener(this, getViewSite().getPart());
		tv.setSorter(sorter);
		tv.setInput(getViewSite());
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, getViewSite().getPart());
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	class MediVerlaufContentProvider implements IStructuredContentProvider {
		
		@Override
		public Object[] getElements(final Object inputElement){
			return mListe.toArray();
		}
		
		@Override
		public void dispose(){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput){
			// TODO Auto-generated method stub
			
		}
		
	}
	
	static class MediVerlaufLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		@Override
		public Image getColumnImage(final Object element, final int columnIndex){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getColumnText(final Object element, final int columnIndex){
			if (element instanceof MediAbgabe) {
				MediAbgabe ma = (MediAbgabe) element;
				switch (columnIndex) {
				case 0:
					return ma.von;
				case 1:
					return ma.bis;
				case 2:
					return ma.medi;
				case 3:
					return ma.dosis;
				default:
					return "??"; //$NON-NLS-1$
				}
			}
			return "?"; //$NON-NLS-1$
		}
		
	}
	
	public void clearEvent(final Class<? extends PersistentObject> template){
		// TODO Auto-generated method stub
		
	}
	
	public void reload(){
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(),
				new IRunnableWithProgress() {
					@Override
					public void run(final IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException{
						Patient sp = ElexisEventDispatcher.getSelectedPatient();
						if (sp == null) {
							return;
						}
						
						monitor.beginTask(Messages.MediVerlaufView_reading,
							IProgressMonitor.UNKNOWN); //$NON-NLS-1$
						monitor.subTask(Messages.MediVerlaufView_findPrescriptions); //$NON-NLS-1$
						Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
						qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, sp.getId());
						List<Prescription> list = qbe.execute();
						mListe.clear();
						monitor.subTask(Messages.MediVerlaufView_findMedicaments); //$NON-NLS-1$
						try {
							for (Prescription p : list) {
								Map<TimeTool, String> terms = p.getTerms();
								TimeTool[] tts = terms.keySet().toArray(new TimeTool[0]);
								for (int i = 0; i < tts.length - 1; i++) {
									if (i < tts.length - 1) {
										mListe
											.add(new MediAbgabe(tts[i].toString(TimeTool.DATE_GER),
												tts[i + 1].toString(TimeTool.DATE_GER), p));
									} else {
										mListe.add(new MediAbgabe(
											tts[i].toString(TimeTool.DATE_GER), TOOPEN, p)); //$NON-NLS-1$
									}
								}
								mListe.add(new MediAbgabe(
									tts[tts.length - 1].toString(TimeTool.DATE_GER), TOOPEN, p)); //$NON-NLS-1$
							}
						} catch (Exception ex) {
							ExHandler.handle(ex);
						}
						tv.refresh();
						monitor.done();
					}
				}, null);
		} catch (Throwable ex) {
			ExHandler.handle(ex);
		}
	}
	
	public void selectionEvent(final PersistentObject obj){
		if (obj instanceof Patient) {
			reload();
		}
		
	}
	
	private static class MediAbgabe implements Comparable<MediAbgabe> {
		String von, bis;
		String medi;
		String dosis;
		
		private static TimeTool compare = new TimeTool();
		private static TimeTool compareTo = new TimeTool();
		
		MediAbgabe(final String v, final String b, final Prescription p){
			von = v;
			bis = b;
			medi = p.getSimpleLabel();
			dosis = p.getDosis();
		}
		
		@Override
		public int compareTo(final MediAbgabe o){
			int ret = 0;
			switch (sortCol) {
			case 0:
				compare.set(von);
				compareTo.set(o.von);
				ret = compare.compareTo(compareTo);
				break;
			case 1:
				if (bis.equals(TOOPEN) && !o.bis.equals(TOOPEN)) {
					ret = 1;
					break;
				} else if (!bis.equals(TOOPEN) && o.bis.equals(TOOPEN)) {
					ret = -1;
					break;
				} else if (bis.equals(TOOPEN) && o.bis.equals(TOOPEN)) {
					ret = 0;
					break;
				}
				compare.set(bis);
				compareTo.set(o.bis);
				ret = compare.compareTo(compareTo);
				break;
			case 2:
				ret = medi.compareTo(o.medi);
				break;
			case 3:
				ret = dosis.compareTo(o.dosis);
				break;
			}
			if (revert) {
				if (ret > 0) {
					ret = -1;
				} else if (ret < 0) {
					ret = 1;
				}
			}
			return ret;
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj instanceof MediAbgabe) {
				return compareTo((MediAbgabe) obj) == 0;
			}
			return false;
		}
	}
	
	@Override
	public void activation(final boolean mode){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void visible(final boolean mode){
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
			reload();
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		}
		
	}
	
	private static class MediSorter extends ViewerSorter {
		
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2){
			return ((MediAbgabe) e1).compareTo((MediAbgabe) e2);
		}
		
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
