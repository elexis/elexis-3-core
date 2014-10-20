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
 *  $Id: KonsZumVerrechnenView.java 6229 2010-03-18 14:03:16Z michael_imhof $
 *******************************************************************************/

package ch.elexis.core.ui.views.rechnung;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.commands.ErstelleRnnCommand;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.dialogs.KonsZumVerrechnenWizardDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.BasicTreeContentProvider;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.DoubleClickListener;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.KonsDetailView;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.LazyTree;
import ch.rgw.tools.LazyTree.LazyTreeListener;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.Tree;

import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * Anzeige aller Behandlungen, für die noch keine Rechnung erstellt wurde. Die Behandlungen werden
 * nach Patient und Fall gruppiert. Patienten, Fälle und Behandlungen können einzeln oder in Gruppen
 * in eine Auswahl übertragen werden, aus der später Rechnungen erstellt werden können.
 * 
 * @author Gerry
 * 
 */
public class KonsZumVerrechnenView extends ViewPart implements ISaveablePart2 {
	public static final String ID = "ch.elexis.BehandlungenVerrechnenView"; //$NON-NLS-1$
	CommonViewer cv;
	ViewerConfigurer vc;
	FormToolkit tk = UiDesk.getToolkit();
	Form left, right;
	@SuppressWarnings("unchecked")
	LazyTree tAll;
	@SuppressWarnings("unchecked")
	Tree tSelection;
	TreeViewer tvSel;
	LazyTreeListener ltl;
	ViewMenus menu;
	private IAction billAction, printAction, clearAction, wizardAction, refreshAction,
			detailAction;
	private IAction removeAction;
	private IAction expandSelAction;
	private IAction expandSelAllAction;
	private IAction selectByDateAction;
	KonsZumVerrechnenView self;
	
	public KonsZumVerrechnenView(){
		cv = new CommonViewer();
		ltl = new RLazyTreeListener();
		tSelection = new Tree<PersistentObject>(null, null);
		tAll = new LazyTree<PersistentObject>(null, null, ltl);
		self = this;
	}
	
	@Override
	public void dispose(){
		// GlobalEvents.getInstance().removeActivationListener(this,this);
		super.dispose();
	}
	
	@Override
	public void createPartControl(final Composite parent){
		vc =
			new ViewerConfigurer(new BasicTreeContentProvider(),
				new ViewerConfigurer.TreeLabelProvider() {
					// extend the TreeLabelProvider by getImage()
					
					@SuppressWarnings("unchecked")
					@Override
					public Image getImage(final Object element){
						if (element instanceof Tree) {
							Tree tree = (Tree) element;
							PersistentObject po = (PersistentObject) tree.contents;
							if (po instanceof Fall) {
								if (po.isValid()) {
									return Images.IMG_OK.getImage();
								} else {
									return Images.IMG_FEHLER.getImage();
								}
							}
						}
						return null;
					}
				}, null, // new DefaultControlFieldProvider(cv, new
				// String[]{"Datum","Name","Vorname","Geb. Dat"}),
				new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
					SimpleWidgetProvider.TYPE_TREE, SWT.MULTI | SWT.V_SCROLL, cv));
		SashForm sash = new SashForm(parent, SWT.NULL);
		left = tk.createForm(sash);
		Composite cLeft = left.getBody();
		left.setText(Messages.KonsZumVerrechnenView_allOpenCons); //$NON-NLS-1$
		cLeft.setLayout(new GridLayout());
		cv.create(vc, cLeft, SWT.NONE, tAll);
		cv.getViewerWidget().setComparator(new KonsZumVerrechnenViewViewerComparator());
		
		cv.addDoubleClickListener(new DoubleClickListener() {
			@Override
			public void doubleClicked(PersistentObject obj, CommonViewer cv){
				if (obj instanceof Patient) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Patient) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(UiResourceConstants.PatientDetailView2_ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else if (obj instanceof Fall) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Fall) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(FallDetailView.ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else if (obj instanceof Konsultation) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Konsultation) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(KonsDetailView.ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		cv.getViewerWidget().addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Patient selPatient = (Patient) ((Tree) selection.getFirstElement()).contents;
				
				for (TreeItem i : tvSel.getTree().getItems()) {
					Patient p = (Patient) ((Tree) i.getData()).contents;
					if (p.getId().equals(selPatient.getId())) {
						tvSel.getTree().setSelection(i);
					}
				}
				tvSel.refresh();
			}
		});
		
		right = tk.createForm(sash);
		Composite cRight = right.getBody();
		right.setText(Messages.KonsZumVerrechnenView_selected); //$NON-NLS-1$
		cRight.setLayout(new GridLayout());
		tvSel = new TreeViewer(cRight, SWT.V_SCROLL | SWT.MULTI);
		// tvSel.getControl().setLayoutData(SWTHelper.getFillGridData(1,true,t,true));
		tvSel.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tvSel.setContentProvider(new BasicTreeContentProvider());
		tvSel.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(final Object element){
				return ((PersistentObject) ((Tree) element).contents).getLabel();
			}
			
		});
		tvSel.setComparator(new KonsZumVerrechnenViewViewerComparator());
		tvSel.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] {
			TextTransfer.getInstance()
		}, new DropTargetAdapter() {
			
			@Override
			public void dragEnter(final DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			
			@Override
			public void drop(final DropTargetEvent event){
				String drp = (String) event.data;
				String[] dl = drp.split(","); //$NON-NLS-1$
				for (String obj : dl) {
					PersistentObject dropped = CoreHub.poFactory.createFromString(obj);
					if (dropped instanceof Patient) {
						selectPatient((Patient) dropped, tAll, tSelection);
					} else if (dropped instanceof Fall) {
						selectFall((Fall) dropped, tAll, tSelection);
					} else if (dropped instanceof Konsultation) {
						selectBehandlung((Konsultation) dropped, tAll, tSelection);
					}
					
				}
				tvSel.refresh(true);
				
			}
			
		});
		tvSel.addSelectionChangedListener(GlobalEventDispatcher.getInstance().getDefaultListener());
		tvSel.setInput(tSelection);
		// GlobalEvents.getInstance().addActivationListener(this,this);
		sash.setWeights(new int[] {
			60, 40
		});
		makeActions();
		MenuManager selMenu = new MenuManager();
		selMenu.setRemoveAllWhenShown(true);
		selMenu.addMenuListener(new IMenuListener() {
			
			public void menuAboutToShow(final IMenuManager manager){
				manager.add(removeAction);
				manager.add(expandSelAction);
				manager.add(expandSelAllAction);
				
			}
			
		});
		tvSel.getControl().setMenu(selMenu.createContextMenu(tvSel.getControl()));
		
		tvSel.getControl().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event){
				org.eclipse.swt.widgets.Tree theWidget =
					(org.eclipse.swt.widgets.Tree) (event.widget);
				TreeItem obj = theWidget.getSelection()[0];
				TreeItem parent = obj.getParentItem();
				String viewID = "";
				if (parent == null) {
					// no parent at all -> must be patient
					viewID = UiResourceConstants.PatientDetailView2_ID;
				} else {
					// may be case or cons
					TreeItem grandpa = parent.getParentItem();
					if (grandpa == null) {
						// must be case
						viewID = FallDetailView.ID;
					} else {
						// must be cons
						viewID = KonsDetailView.ID;
					}
				}
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(viewID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
		
		menu = new ViewMenus(getViewSite());
		menu.createToolbar(refreshAction, wizardAction, printAction, clearAction, null, billAction);
		menu.createMenu(wizardAction, selectByDateAction);
		menu.createViewerContextMenu(cv.getViewerWidget(), detailAction);
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	class RLazyTreeListener implements LazyTreeListener {
		final LazyTreeListener self = this;
		
		@SuppressWarnings("unchecked")
		public boolean fetchChildren(final LazyTree l){
			PersistentObject cont = (PersistentObject) l.contents;
			final Stm stm = PersistentObject.getConnection().getStatement();
			if (cont == null) {
				IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
				try {
					progressService.runInUI(PlatformUI.getWorkbench().getProgressService(),
						new IRunnableWithProgress() {
							public void run(final IProgressMonitor monitor){
								monitor.beginTask(Messages.KonsZumVerrechnenView_findCons, 100); //$NON-NLS-1$
								monitor.subTask(Messages.KonsZumVerrechnenView_databaseRequest); //$NON-NLS-1$
								String sql =
									"SELECT distinct PATIENTID FROM FAELLE " + //$NON-NLS-1$
										"JOIN BEHANDLUNGEN ON BEHANDLUNGEN.FALLID=FAELLE.ID WHERE BEHANDLUNGEN.deleted='0' AND BEHANDLUNGEN.RECHNUNGSID is null "; //$NON-NLS-1$
								if (CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL) == false) {
									sql += "AND BEHANDLUNGEN.MANDANTID=" //$NON-NLS-1$
										+ CoreHub.actMandant.getWrappedId();
								}
								ResultSet rs = stm.query(sql);
								monitor.worked(10);
								monitor.subTask(Messages.KonsZumVerrechnenView_readIn); //$NON-NLS-1$
								try {
									while ((rs != null) && rs.next()) {
										String s = rs.getString(1);
										Patient p = Patient.load(s);
										if (p.exists() && (tSelection.find(p, false) == null)) {
											new LazyTree(l, p, self);
										}
										monitor.worked(1);
									}
									monitor.done();
								} catch (SQLException e) {
									ExHandler.handle(e);
								}
							}
						}, null);
				} catch (Throwable ex) {
					ExHandler.handle(ex);
				}
				
			} else {
				ResultSet rs = null;
				String sql;
				try {
					if (cont instanceof Patient) {
						sql =
							"SELECT distinct FAELLE.ID FROM FAELLE join BEHANDLUNGEN ON BEHANDLUNGEN.FALLID=FAELLE.ID " + //$NON-NLS-1$
								"WHERE BEHANDLUNGEN.RECHNUNGSID is null AND BEHANDLUNGEN.DELETED='0' AND FAELLE.PATIENTID=" //$NON-NLS-1$
								+ cont.getWrappedId(); //$NON-NLS-1$
						if (CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL) == false) {
							sql +=
								" AND BEHANDLUNGEN.MANDANTID=" + CoreHub.actMandant.getWrappedId(); //$NON-NLS-1$
						}
						rs = stm.query(sql);
						while ((rs != null) && rs.next()) {
							String s = rs.getString(1);
							Fall f = Fall.load(s);
							if (f.exists() && (tSelection.find(f, true) == null)) {
								new LazyTree(l, f, this);
							}
						}
					} else if (cont instanceof Fall) {
						sql =
							"SELECT ID FROM BEHANDLUNGEN WHERE RECHNUNGSID is null AND deleted='0' AND FALLID=" + cont.getWrappedId(); //$NON-NLS-1$
						if (CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL) == false) {
							sql += " AND MANDANTID=" + CoreHub.actMandant.getWrappedId(); //$NON-NLS-1$
						}
						rs = stm.query(sql);
						while ((rs != null) && rs.next()) {
							String s = rs.getString(1);
							Konsultation b = Konsultation.load(s);
							if (b.exists() && (tSelection.find(b, true) == null)) {
								new LazyTree(l, b, this);
							}
						}
					}
					if (rs != null) {
						rs.close();
					}
				} catch (Exception e) {
					ExHandler.handle(e);
				} finally {
					PersistentObject.getConnection().releaseStatement(stm);
				}
			}
			return false;
		}
		
		@SuppressWarnings("unchecked")
		public boolean hasChildren(final LazyTree l){
			Object po = l.contents;
			if (po instanceof Konsultation) {
				return false;
			}
			return true;
		}
		
	}
	
	public void selectKonsultation(final Konsultation k){
		selectBehandlung(k, tAll, tSelection);
	}
	
	/**
	 * Patienten in von tAll nach tSelection verschieben bzw. falls noch nicht vorhanden, neu
	 * anlegen.
	 */
	@SuppressWarnings("unchecked")
	private Tree selectPatient(final Patient pat, final Tree tSource, final Tree tDest){
		Tree pSource = tSource.find(pat, false);
		Tree pDest = tDest.find(pat, false);
		if (pDest == null) {
			if (pSource == null) {
				pDest = tDest.add(pat);
			} else {
				pDest = pSource.move(tDest);
			}
		} else {
			if (pSource != null) {
				List<Tree> fs = (List<Tree>) pSource.getChildren();
				for (Tree t : fs) {
					selectFall((Fall) t.contents, tSource, tDest);
				}
			}
		}
		cv.getViewerWidget().refresh(tSource);
		return pDest;
	}
	
	@SuppressWarnings("unchecked")
	private Tree selectFall(final Fall f, final Tree tSource, final Tree tDest){
		Patient pat = f.getPatient();
		Tree tPat = tDest.find(pat, false);
		if (tPat == null) {
			tPat = tDest.add(pat);
		}
		Tree tFall = tSource.find(f, true);
		if (tFall == null) {
			tFall = tPat.add(f);
		} else {
			Tree tOld = tFall.getParent();
			tPat.merge(tFall);
			if (tOld.getFirstChild() == null) {
				tSource.remove(tOld);
			}
			cv.getViewerWidget().refresh(tOld);
		}
		return tFall;
	}
	
	@SuppressWarnings("unchecked")
	private Tree selectBehandlung(final Konsultation bh, final Tree tSource, final Tree tDest){
		Fall f = bh.getFall();
		Patient pat = f.getPatient();
		Tree tPat = tDest.find(pat, false);
		if (tPat == null) {
			tPat = tDest.add(pat);
		}
		Tree tFall = tPat.find(f, false);
		if (tFall == null) {
			tFall = tPat.add(f);
		}
		Tree tBeh = tFall.find(bh, false);
		if (tBeh == null) {
			tBeh = tFall.add(bh);
		}
		
		Tree tps = tSource.find(pat, false);
		if (tps != null) {
			Tree tfs = tps.find(f, false);
			if (tfs != null) {
				Tree tbs = tfs.find(bh, false);
				if (tbs != null) {
					tfs.remove(tbs);
					cv.getViewerWidget().refresh(tfs);
				}
				if (tfs.hasChildren() == false) {
					tps.remove(tfs);
					cv.getViewerWidget().refresh(tps);
				}
			}
			if (tps.hasChildren() == false) {
				tSource.remove(tps);
				cv.getViewerWidget().refresh(tSource);
			}
		}
		return tBeh;
	}
	
	private void makeActions(){
		billAction = new Action(Messages.KonsZumVerrechnenView_createInvoices) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_BILL.getImageDescriptor()); //$NON-NLS-1$
					setToolTipText(Messages.KonsZumVerrechnenView_createInvoices); //$NON-NLS-1$
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public void run(){
					if (((StructuredSelection) tvSel.getSelection()).size() > 0) {
						if (!SWTHelper.askYesNo(
							Messages.KonsZumVerrechnenView_RealleCreateBillsCaption, //$NON-NLS-1$
							Messages.KonsZumVerrechnenView_ReallyCreateBillsBody)) { //$NON-NLS-1$
							return;
						}
					}
					// Handler.execute(getViewSite(), "bill.create", tSelection);
					ErstelleRnnCommand.ExecuteWithParams(getViewSite(), tSelection);
					/*
					 * IHandlerService handlerService = (IHandlerService)
					 * getViewSite().getService(IHandlerService.class); ICommandService cmdService =
					 * (ICommandService) getViewSite().getService(ICommandService.class); try {
					 * Command command = cmdService.getCommand("bill.create"); Parameterization px =
					 * new Parameterization(command.getParameter
					 * ("ch.elexis.RechnungErstellen.parameter" ), new
					 * TreeToStringConverter().convertToString(tSelection)); ParameterizedCommand
					 * parmCommand = new ParameterizedCommand(command, new Parameterization[] { px
					 * });
					 * 
					 * handlerService.executeCommand(parmCommand, null);
					 * 
					 * } catch (Exception ex) { throw new RuntimeException("add.command not found");
					 * }
					 */
					
					tvSel.refresh();
				}
			};
		clearAction = new Action(Messages.KonsZumVerrechnenView_clearSelection) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_REMOVEITEM.getImageDescriptor()); //$NON-NLS-1$
					setToolTipText(Messages.KonsZumVerrechnenView_deleteList); //$NON-NLS-1$
					
				}
				
				@Override
				public void run(){
					tSelection.clear();
					tvSel.refresh();
				}
			};
		refreshAction = new Action(Messages.KonsZumVerrechnenView_reloadAction) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
					setToolTipText(Messages.KonsZumVerrechnenView_reloadToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					tAll.clear();
					cv.notify(CommonViewer.Message.update);
					tvSel.refresh(true);
				}
			};
		wizardAction = new Action(Messages.KonsZumVerrechnenView_autoAction) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
					setToolTipText(Messages.KonsZumVerrechnenView_autoToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					KonsZumVerrechnenWizardDialog kzvd =
						new KonsZumVerrechnenWizardDialog(getViewSite().getShell());
					if (kzvd.open() == Dialog.OK) {
						IProgressService progressService =
							PlatformUI.getWorkbench().getProgressService();
						try {
							progressService.runInUI(progressService,
								new Rechnungslauf(self, kzvd.bMarked, kzvd.ttFirstBefore,
									kzvd.ttLastBefore, kzvd.mAmount, kzvd.bQuartal, kzvd.bSkip,
									kzvd.ttFrom, kzvd.ttTo, kzvd.accountSys), null);
						} catch (Throwable ex) {
							ExHandler.handle(ex);
						}
						tvSel.refresh();
						cv.notify(CommonViewer.Message.update);
					}
				}
			};
		printAction = new Action(Messages.KonsZumVerrechnenView_printSelection) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
					setToolTipText(Messages.KonsZumVerrechnenView_printToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					new SelectionPrintDialog(getViewSite().getShell()).open();
					
				}
			};
		removeAction = new Action(Messages.KonsZumVerrechnenView_removeFromSelection) { //$NON-NLS-1$
				@SuppressWarnings("unchecked")
				@Override
				public void run(){
					IStructuredSelection sel = (IStructuredSelection) tvSel.getSelection();
					if (!sel.isEmpty()) {
						for (Object o : sel.toList()) {
							if (o instanceof Tree) {
								Tree t = (Tree) o;
								if (t.contents instanceof Patient) {
									selectPatient((Patient) t.contents, tSelection, tAll);
								} else if (t.contents instanceof Fall) {
									selectFall((Fall) t.contents, tSelection, tAll);
								} else if (t.contents instanceof Konsultation) {
									selectBehandlung((Konsultation) t.contents, tSelection, tAll);
								}
							}
						}
						tvSel.refresh();
						cv.notify(CommonViewer.Message.update);
					}
				}
			};
		
		// expand action for tvSel
		expandSelAction = new Action(Messages.KonsZumVerrechnenView_expand) { //$NON-NLS-1$
				@SuppressWarnings("unchecked")
				@Override
				public void run(){
					IStructuredSelection sel = (IStructuredSelection) tvSel.getSelection();
					if (!sel.isEmpty()) {
						for (Object o : sel.toList()) {
							if (o instanceof Tree) {
								Tree t = (Tree) o;
								tvSel.expandToLevel(t, TreeViewer.ALL_LEVELS);
							}
						}
					}
				}
			};
		// expandAll action for tvSel
		expandSelAllAction = new Action(Messages.KonsZumVerrechnenView_expandAll) { //$NON-NLS-1$
				@Override
				public void run(){
					tvSel.expandAll();
				}
			};
		
		selectByDateAction = new Action(Messages.KonsZumVerrechnenView_selectByDateAction) { //$NON-NLS-1$
				TimeTool fromDate;
				TimeTool toDate;
				
				{
					setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
					setToolTipText(Messages.KonsZumVerrechnenView_selectByDateActionToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					// select date
					SelectDateDialog dialog = new SelectDateDialog(getViewSite().getShell());
					if (dialog.open() == TitleAreaDialog.OK) {
						fromDate = dialog.getFromDate();
						toDate = dialog.getToDate();
						
						IProgressService progressService =
							PlatformUI.getWorkbench().getProgressService();
						try {
							progressService.runInUI(PlatformUI.getWorkbench().getProgressService(),
								new IRunnableWithProgress() {
									public void run(final IProgressMonitor monitor){
										doSelectByDate(monitor, fromDate, toDate);
									}
								}, null);
						} catch (Throwable ex) {
							ExHandler.handle(ex);
						}
						tvSel.refresh();
						cv.notify(CommonViewer.Message.update);
					}
				}
				
			};
		detailAction =
			new RestrictedAction(AccessControlDefaults.LSTG_VERRECHNEN,
				Messages.KonsZumVerrechnenView_billingDetails) { //$NON-NLS-1$
				@SuppressWarnings("unchecked")
				@Override
				public void doRun(){
					Object[] sel = cv.getSelection();
					if ((sel != null) && (sel.length > 0)) {
						new VerrDetailDialog(getViewSite().getShell(), (Tree) sel[0]).open();
					}
				}
			};
	}
	
	/**
	 * Auwahl der Konsultationen, die verrechnet werden sollen, nach Datum. Es erscheint ein Dialog,
	 * wo man den gewünschten Bereich wählen kann.
	 */
	@SuppressWarnings("unchecked")
	private void doSelectByDate(final IProgressMonitor monitor, final TimeTool fromDate,
		final TimeTool toDate){
		TimeTool actDate = new TimeTool();
		
		// set dates to midnight
		TimeTool date1 = new TimeTool(fromDate);
		TimeTool date2 = new TimeTool(toDate);
		date1.chop(3);
		date2.add(TimeTool.DAY_OF_MONTH, 1);
		date2.chop(3);
		
		List<Tree> lAll = (List<Tree>) tAll.getChildren();
		monitor.beginTask(Messages.KonsZumVerrechnenView_selectByDateTask, lAll.size() + 1); //$NON-NLS-1$
		for (Tree tP : lAll) {
			monitor.worked(1);
			for (Tree tF : (List<Tree>) tP.getChildren()) {
				List<Tree> tK = (List<Tree>) tF.getChildren();
				for (Tree tk : tK) {
					Konsultation k = (Konsultation) tk.contents;
					actDate.set(k.getDatum());
					if (actDate.isAfterOrEqual(date1) && actDate.isBefore(date2)) {
						selectBehandlung((Konsultation) tk.contents, tAll, tSelection);
					}
				}
				if (monitor.isCanceled()) {
					monitor.done();
					return;
				}
			}
		}
		monitor.done();
	}
	
	/***********************************************************************************************
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
	
	/**
	 * SelectDateDialog
	 * 
	 * @author danlutz
	 */
	public class SelectDateDialog extends TitleAreaDialog {
		DatePickerCombo dpFromDate;
		DatePickerCombo dpToDate;
		
		TimeTool fromDate = null;
		TimeTool toDate = null;
		
		public SelectDateDialog(final Shell parentShell){
			super(parentShell);
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.SelectDateDialog_choosePeriodTitle); //$NON-NLS-1$
			setMessage(Messages.SelectDateDialog_choosePeriodMessage); //$NON-NLS-1$
			getShell().setText(Messages.SelectDateDialog_description); //$NON-NLS-1$
		}
		
		@Override
		protected Control createDialogArea(final Composite parent){
			Composite com = new Composite(parent, SWT.NONE);
			com.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			com.setLayout(new GridLayout(2, false));
			
			new Label(com, SWT.NONE).setText(Messages.SelectDateDialog_from); //$NON-NLS-1$
			new Label(com, SWT.NONE).setText(Messages.SelectDateDialog_to); //$NON-NLS-1$
			
			dpFromDate = new DatePickerCombo(com, SWT.NONE);
			dpToDate = new DatePickerCombo(com, SWT.NONE);
			
			return com;
		}
		
		/*
		 * (Kein Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
		 */
		@Override
		protected void okPressed(){
			Date date = dpFromDate.getDate();
			if (date == null) {
				fromDate = new TimeTool(TimeTool.BEGINNING_OF_UNIX_EPOCH);
			} else {
				fromDate = new TimeTool(date.getTime());
			}
			date = dpToDate.getDate();
			if (date == null) {
				toDate = new TimeTool(TimeTool.END_OF_UNIX_EPOCH);
			} else {
				toDate = new TimeTool(date.getTime());
			}
			super.okPressed();
		}
		
		public TimeTool getFromDate(){
			return fromDate;
		}
		
		public TimeTool getToDate(){
			return toDate;
		}
		
	}
	
	class SelectionPrintDialog extends TitleAreaDialog implements ICallback {
		private TextContainer text;
		
		public SelectionPrintDialog(final Shell shell){
			super(shell);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(final Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			text = new TextContainer(getShell());
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			text.getPlugin().createContainer(ret, this);
			text.getPlugin().showMenu(false);
			text.getPlugin().showToolbar(false);
			text.createFromTemplateName(null,
				"Liste", Brief.UNKNOWN, CoreHub.actUser, Messages.KonsZumVerrechnenView_billsTitle); //$NON-NLS-1$ //$NON-NLS-2$
			Tree[] all = (Tree[]) tSelection.getChildren().toArray(new Tree[0]);
			String[][] table = new String[all.length][];
			
			for (int i = 0; i < all.length; i++) {
				table[i] = new String[2];
				Tree tr = all[i];
				if (tr.contents instanceof Konsultation) {
					tr = tr.getParent();
				}
				if (tr.contents instanceof Fall) {
					tr = tr.getParent();
				}
				Patient p = (Patient) tr.contents;
				StringBuilder sb = new StringBuilder();
				sb.append(p.getLabel());
				for (Tree tFall : (Tree[]) tr.getChildren().toArray(new Tree[0])) {
					Fall fall = (Fall) tFall.contents;
					sb.append(Messages.KonsZumVerrechnenView_case).append(fall.getLabel()); //$NON-NLS-1$
					for (Tree tRn : (Tree[]) tFall.getChildren().toArray(new Tree[0])) {
						Konsultation k = (Konsultation) tRn.contents;
						sb.append(Messages.KonsZumVerrechnenView_kons).append(k.getLabel()); //$NON-NLS-1$
					}
				}
				table[i][0] = sb.toString();
			}
			text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9); //$NON-NLS-1$
			text.getPlugin().insertTable("[Liste]", 0, table, new int[] { //$NON-NLS-1$
					90, 10
				});
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.KonsZumVerrechnenView_billsList); //$NON-NLS-1$
			setTitle(Messages.KonsZumVerrechnenView_printListCaption); //$NON-NLS-1$
			setMessage(Messages.KonsZumVerrechnenView_printListMessage); //$NON-NLS-1$
			getShell().setSize(900, 700);
			SWTHelper.center(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(),
				getShell());
		}
		
		@Override
		protected void okPressed(){
			super.okPressed();
		}
		
		public void save(){
			// TODO Auto-generated method stub
			
		}
		
		public boolean saveAs(){
			// TODO Auto-generated method stub
			return false;
		}
	}
}
