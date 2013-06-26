/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.Brief;
import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.DoubleClickListener;
import ch.elexis.core.ui.util.viewers.DefaultContentProvider;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

public class BriefAuswahl extends ViewPart implements ch.elexis.core.data.events.ElexisEventListener, IActivationListener,
		ISaveablePart2 {
	
	public final static String ID = "ch.elexis.BriefAuswahlView"; //$NON-NLS-1$
	private final FormToolkit tk;
	private Form form;
	private Action briefNeuAction, briefLadenAction, editNameAction;
	private Action deleteAction;
	private ViewMenus menus;
	private ArrayList<sPage> pages = new ArrayList<sPage>();
	CTabFolder ctab;
	
	// private ViewMenus menu;
	// private IAction delBriefAction;
	public BriefAuswahl(){
		tk = UiDesk.getToolkit();
	}
	
	@Override
	public void createPartControl(final Composite parent){
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.getString("BriefAuswahlAllLetters")).append(Brief.UNKNOWN).append(",").append(Brief.AUZ) //$NON-NLS-1$
			.append(",").append(Brief.RP).append(",").append(Brief.LABOR);
		String cats = CoreHub.globalCfg.get(Preferences.DOC_CATEGORY, sb.toString());
		parent.setLayout(new GridLayout());
		
		form = tk.createForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form.setBackground(parent.getBackground());
		
		// Grid layout with zero margins
		GridLayout slimLayout = new GridLayout();
		slimLayout.marginHeight = 0;
		slimLayout.marginWidth = 0;
		
		Composite body = form.getBody();
		body.setLayout(slimLayout);
		body.setBackground(parent.getBackground());
		
		ctab = new CTabFolder(body, SWT.BOTTOM);
		ctab.setLayout(slimLayout);
		ctab.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ctab.setBackground(parent.getBackground());
		makeActions();
		menus = new ViewMenus(getViewSite());
		
		for (String cat : cats.split(",")) {
			CTabItem ct = new CTabItem(ctab, SWT.NONE);
			ct.setText(cat);
			sPage page = new sPage(ctab, cat);
			pages.add(page);
			menus.createViewerContextMenu(page.cv.getViewerWidget(), editNameAction, deleteAction);
			ct.setData(page.cv);
			ct.setControl(page);
			page.cv.addDoubleClickListener(new DoubleClickListener() {
				@Override
				public void doubleClicked(PersistentObject obj, CommonViewer cv){
					briefLadenAction.run();
				}
			});
		}
		
		ctab.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				relabel();
			}
			
		});
		
		GlobalEventDispatcher.addActivationListener(this, this);
		menus.createMenu(briefNeuAction, briefLadenAction, editNameAction, deleteAction);
		menus.createToolbar(briefNeuAction, briefLadenAction, deleteAction);
		ctab.setSelection(0);
		relabel();
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(this);
		GlobalEventDispatcher.removeActivationListener(this, this);
		
		for (sPage page : pages) {
			page.getCommonViewer().getConfigurer().getContentProvider().stopListening();
		}
	}
	
	@Override
	public void setFocus(){
		
	}
	
	public void relabel(){
		UiDesk.asyncExec(new Runnable() {
			public void run(){
				Patient pat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
				if (pat == null) {
					form.setText(Messages.getString("BriefAuswahlNoPatientSelected")); //$NON-NLS-1$
				} else {
					form.setText(pat.getLabel());
					CTabItem sel = ctab.getSelection();
					if (sel != null) {
						CommonViewer cv = (CommonViewer) sel.getData();
						cv.notify(CommonViewer.Message.update);
					}
				}
			}
		});
		
	}
	
	class sPage extends Composite {
		private final CommonViewer cv;
		private final ViewerConfigurer vc;
		
		public CommonViewer getCommonViewer(){
			return cv;
		}
		
		sPage(final Composite parent, final String cat){
			super(parent, SWT.NONE);
			setLayout(new GridLayout());
			cv = new CommonViewer();
			vc =
				new ViewerConfigurer(new DefaultContentProvider(cv, Brief.class, new String[] {
					Brief.FLD_DATE
				}, true) {
					
					@Override
					public Object[] getElements(final Object inputElement){
						Patient actPat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
						if (actPat != null) {
							Query<Brief> qbe = new Query<Brief>(Brief.class);
							qbe.add(Brief.FLD_PATIENT_ID, Query.EQUALS, actPat.getId());
							if (cat.equals(Messages.getString("BriefAuswahlAllLetters2"))) { //$NON-NLS-1$
								qbe.add(Brief.FLD_TYPE, Query.NOT_EQUAL, Brief.TEMPLATE);
							} else {
								qbe.add(Brief.FLD_TYPE, Query.EQUALS, cat);
							}
							cv.getConfigurer().getControlFieldProvider().setQuery(qbe);
							
							List<Brief> list = qbe.execute();
							return list.toArray();
						} else {
							return new Brief[0];
						}
					}
					
				}, new DefaultLabelProvider(), new DefaultControlFieldProvider(cv, new String[] {
					"Betreff=Titel"
				}), new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
					SimpleWidgetProvider.TYPE_LIST, SWT.V_SCROLL, cv));
			cv.create(vc, this, SWT.NONE, getViewSite());
			cv.getViewerWidget().setComparator(new ViewerComparator() {
				@Override
				public int compare(Viewer viewer, Object e1, Object e2){
					if (e1 instanceof Brief && e2 instanceof Brief) {
						TimeTool bt1 = new TimeTool(((Brief) e1).getDatum());
						TimeTool bt2 = new TimeTool(((Brief) e2).getDatum());
						return bt2.compareTo(bt1);
					}
					return 0;
				}
			});
			vc.getContentProvider().startListening();
			Button bLoad =
				tk.createButton(this, Messages.getString("BriefAuswahlLoadButtonText"), SWT.PUSH); //$NON-NLS-1$
			bLoad.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e){
					try {
						TextView tv = (TextView) getViewSite().getPage().showView(TextView.ID);
						Object[] o = cv.getSelection();
						if ((o != null) && (o.length > 0)) {
							Brief brief = (Brief) o[0];
							if (tv.openDocument(brief) == false) {
								SWTHelper.alert(Messages.getString("BriefAuswahlErrorHeading"), //$NON-NLS-1$
									Messages.getString("BriefAuswahlCouldNotLoadText")); //$NON-NLS-1$
							}
						} else {
							tv.createDocument(null, null);
						}
					} catch (Throwable ex) {
						ExHandler.handle(ex);
					}
				}
				
			});
			bLoad.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			
		}
	}
	
	private void makeActions(){
		briefNeuAction = new Action(Messages.getString("BriefAuswahlNewButtonText")) { //$NON-NLS-1$
				@Override
				public void run(){
					Patient pat = ElexisEventDispatcher.getSelectedPatient();
					if (pat == null) {
						MessageDialog.openInformation(UiDesk.getTopShell(),
							Messages.getString("BriefAuswahlNoPatientSelected"),
							Messages.getString("BriefAuswahlNoPatientSelected"));
						return;
					}
					
					Fall selectedFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
					if (selectedFall == null) {
						SelectFallDialog sfd = new SelectFallDialog(UiDesk.getTopShell());
						sfd.open();
						if (sfd.result != null) {
							ElexisEventDispatcher.fireSelectionEvent(sfd.result);
						} else {
							MessageDialog
								.openInformation(UiDesk.getTopShell(), Messages
									.getString("TextView.NoCaseSelected"), //$NON-NLS-1$
									Messages
										.getString("TextView.SaveNotPossibleNoCaseAndKonsSelected")); //$NON-NLS-1$
							return;
						}
					}
					
					Konsultation selectedKonsultation =
						(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
					if (selectedKonsultation == null) {
						Konsultation k = pat.getLetzteKons(false);
						if (k == null) {
							k =
								((Fall) ElexisEventDispatcher.getSelected(Fall.class))
									.neueKonsultation();
							k.setMandant(CoreHub.actMandant);
						}
						ElexisEventDispatcher.fireSelectionEvent(k);
					}
					
					TextView tv = null;
					try {
						tv = (TextView) getSite().getPage().showView(TextView.ID /*
																				 * ,StringTool.unique
																				 * ("textView")
																				 * ,IWorkbenchPage
																				 * .VIEW_ACTIVATE
																				 */);
						DocumentSelectDialog bs =
							new DocumentSelectDialog(getViewSite().getShell(), CoreHub.actMandant,
								DocumentSelectDialog.TYPE_CREATE_DOC_WITH_TEMPLATE);
						if (bs.open() == Dialog.OK) {
							// trick: just supply a dummy address for creating the doc
							Kontakt address = null;
							if (DocumentSelectDialog.getDontAskForAddresseeForThisTemplate(bs.getSelectedDocument()))
								address = Kontakt.load("-1");
							tv.createDocument(bs.getSelectedDocument(), bs.getBetreff(), address);
							tv.setName();
							CTabItem sel = ctab.getSelection();
							if (sel != null) {
								CommonViewer cv = (CommonViewer) sel.getData();
								cv.notify(CommonViewer.Message.update_keeplabels);
							}
							
						}
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
			};
		briefLadenAction = new Action(Messages.getString("BriefAuswahlOpenButtonText")) { //$NON-NLS-1$
				@Override
				public void run(){
					try {
						TextView tv = (TextView) getViewSite().getPage().showView(TextView.ID);
						CTabItem sel = ctab.getSelection();
						if (sel != null) {
							CommonViewer cv = (CommonViewer) sel.getData();
							Object[] o = cv.getSelection();
							if ((o != null) && (o.length > 0)) {
								Brief brief = (Brief) o[0];
								if (tv.openDocument(brief) == false) {
									SWTHelper.alert(Messages.getString("BriefAuswahlErrorHeading"), //$NON-NLS-1$
										Messages.getString("BriefAuswahlCouldNotLoadText")); //$NON-NLS-1$
								}
							} else {
								tv.createDocument(null, null);
							}
							cv.notify(CommonViewer.Message.update);
						}
					} catch (PartInitException e) {
						ExHandler.handle(e);
					}
					
				}
			};
		deleteAction = new Action(Messages.getString("BriefAuswahlDeleteButtonText")) { //$NON-NLS-1$
				@Override
				public void run(){
					CTabItem sel = ctab.getSelection();
					if ((sel != null)
						&& SWTHelper.askYesNo(
							Messages.getString("BriefAuswahlDeleteConfirmHeading"), //$NON-NLS-1$
							Messages.getString("BriefAuswahlDeleteConfirmText"))) { //$NON-NLS-1$
						CommonViewer cv = (CommonViewer) sel.getData();
						Object[] o = cv.getSelection();
						if ((o != null) && (o.length > 0)) {
							Brief brief = (Brief) o[0];
							brief.delete();
						}
						cv.notify(CommonViewer.Message.update);
					}
					
				}
			};
		editNameAction = new Action(Messages.getString("BriefAuswahlRenameButtonText")) { //$NON-NLS-1$
				@Override
				public void run(){
					CTabItem sel = ctab.getSelection();
					if (sel != null) {
						CommonViewer cv = (CommonViewer) sel.getData();
						Object[] o = cv.getSelection();
						if ((o != null) && (o.length > 0)) {
							Brief brief = (Brief) o[0];
							InputDialog id =
								new InputDialog(getViewSite().getShell(),
									Messages.getString("BriefAuswahlNewSubjectHeading"), //$NON-NLS-1$
									Messages.getString("BriefAuswahlNewSubjectText"), //$NON-NLS-1$
									brief.getBetreff(), null);
							if (id.open() == Dialog.OK) {
								brief.setBetreff(id.getValue());
							}
						}
						cv.notify(CommonViewer.Message.update);
					}
				}
			};
		/*
		 * importAction=new Action("Importieren..."){ public void run(){
		 * 
		 * } };
		 */
		briefLadenAction.setImageDescriptor(Images.IMG_DOCUMENT_TEXT.getImageDescriptor());
		briefLadenAction.setToolTipText(Messages.getString("BriefAuswahlOpenLetterForEdit")); //$NON-NLS-1$
		briefNeuAction.setImageDescriptor(Images.IMG_DOCUMENT_ADD.getImageDescriptor()); 
		briefNeuAction.setToolTipText(Messages.getString("BriefAuswahlCreateNewDocument")); //$NON-NLS-1$
		editNameAction.setImageDescriptor(Images.IMG_DOCUMENT_WRITE.getImageDescriptor());
		editNameAction.setToolTipText(Messages.getString("BriefAuswahlRenameDocument")); //$NON-NLS-1$
		deleteAction.setImageDescriptor(Images.IMG_DOCUMENT_REMOVE.getImageDescriptor());
		deleteAction.setToolTipText(Messages.getString("BriefAuswahlDeleteDocument")); //$NON-NLS-1$
	}
	
	public void activation(final boolean mode){
		// TODO Auto-generated method stub
		
	}
	
	public void visible(final boolean mode){
		if (mode == true) {
			ElexisEventDispatcher.getInstance().addListeners(this);
			relabel();
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(this);
		}
		
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(final IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	public void catchElexisEvent(ElexisEvent ev){
		relabel();
	}
	
	private static ElexisEvent template = new ElexisEvent(null, Patient.class,
		ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED);
	
	public ElexisEvent getElexisEventFilter(){
		return template;
	}
}
