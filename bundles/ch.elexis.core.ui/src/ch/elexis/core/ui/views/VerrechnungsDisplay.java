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

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.ICodeElement;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.ILocalService;
import ch.elexis.core.model.IService;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.dialogs.ResultDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.LockDeniedNoActionLockHandler;
import ch.elexis.core.ui.services.BillingServiceHolder;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.Artikel;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class VerrechnungsDisplay extends Composite implements IUnlockable {
	
	private Label billedLabel;
	private Table table;
	private TableViewer viewer;
	private MenuManager contextMenuManager;
	private IEncounter actEncounter;
	private String defaultRGB;
	private IWorkbenchPage page;
	private final GenericObjectDropTarget dropTarget;
	private IAction applyMedicationAction, chPriceAction, chCountAction,
			chTextAction, removeAction,
			removeAllAction;
	private TableViewerFocusCellManager focusCellManager;
	private TableColumnLayout tableLayout;
		
	private static final String INDICATED_MEDICATION = Messages.VerrechnungsDisplay_indicatedMedication;
	private static final String APPLY_MEDICATION = Messages.VerrechnungsDisplay_applyMedication;
	private static final String CHPRICE = Messages.VerrechnungsDisplay_changePrice;
	private static final String CHCOUNT = Messages.VerrechnungsDisplay_changeNumber;
	private static final String REMOVE = Messages.VerrechnungsDisplay_removeElements;
	private static final String CHTEXT = Messages.VerrechnungsDisplay_changeText;
	private static final String REMOVEALL = Messages.VerrechnungsDisplay_removeAll;
	
	public VerrechnungsDisplay(final IWorkbenchPage p, Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		this.page = p;
		defaultRGB = UiDesk.createColor(new RGB(255, 255, 255));
		
		billedLabel = new Label(this, SWT.NONE);
		billedLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		
		ToolBarManager toolBarManager = new ToolBarManager(SWT.RIGHT);
		toolBarManager.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_NEW.getImageDescriptor();
			}
			
			@Override
			public void run(){
				try {
					if (StringTool.isNothing(LeistungenView.ID)) {
						SWTHelper.alert(Messages.VerrechnungsDisplay_error, "LeistungenView.ID"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					page.showView(LeistungenView.ID);
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
				} catch (Exception ex) {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							Messages.VerrechnungsDisplay_errorStartingCodeWindow + ex.getMessage(),
							ex, ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		ToolBar toolBar = toolBarManager.createControl(this);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		makeActions();
		tableLayout = new TableColumnLayout();
		Composite tableComposite = new Composite(this, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableComposite.setLayout(tableLayout);
		viewer = new TableViewer(tableComposite,
			SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table = viewer.getTable();
		table.setMenu(createVerrMenu());
		// dummy table viewer needed for SelectionsProvider for Menu
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createColumns();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (selection != null && !selection.isEmpty()
					&& (selection.getFirstElement() instanceof IBilled)) {
					ElexisEventDispatcher
						.fireSelectionEvent(
							Verrechnet.load(((IBilled) selection.getFirstElement()).getId()));
				}
			}
		});
		table.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e){}
			
			@Override
			public void keyPressed(KeyEvent e){
				if (e.keyCode == SWT.DEL) {
					if (table.getSelectionIndices().length >= 1 && removeAction != null) {
						removeAction.run();
					}
				}
			}
		});
		// connect double click on column to actions
		focusCellManager =
			new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				ViewerCell focusCell = focusCellManager.getFocusCell();
				int columnIndex = focusCell.getColumnIndex();
				if (columnIndex == 0) {
					chCountAction.run();
				} else if (columnIndex == 3) {
					chPriceAction.run();
				} else if (columnIndex == 4) {
					removeAction.run();
				}
			}
		});
		
		dropTarget =
			new GenericObjectDropTarget(Messages.VerrechnungsDisplay_doBill, table,
				new DropReceiver()); //$NON-NLS-1$
		
		// refresh the table if a update to a Verrechnet occurs
		ElexisEventDispatcher.getInstance().addListeners(
			new ElexisUiEventListenerImpl(Verrechnet.class, ElexisEvent.EVENT_UPDATE) {
				@Override
				public void runInUi(ElexisEvent ev){
					PersistentObject object = ev.getObject();
					if (object != null) {
						viewer.update(object, null);
					}
				}
			});
	}
	
	private void createColumns(){
		String[] titles = {
			"Anz.", "Code", "Bezeichnung", "Preis", ""
		};
		int[] weights = {
			8, 20, 50, 15, 7
		};
		
		TableViewerColumn col = createTableViewerColumn(titles[0], weights[0], 0, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					return Double.toString(billed.getAmount());
				}
				return "";
			}
			
			@Override
			public Image getImage(Object element){
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					IBillable billable = billed.getBillable();
					if (billable instanceof IArticle) {
						IArticle a = (IArticle) billable;
						int sellingSize = a.getSellingSize();
						if (sellingSize > 0 && sellingSize < a.getPackageSize()) {
							return Images.IMG_BLOCKS_SMALL.getImage();
						}
					}
				}
				return super.getImage(element);
			}
		});
		
		col = createTableViewerColumn(titles[1], weights[1], 1, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					return getServiceCode(billed);
				}
				return "";
			}
		});
		
		col = createTableViewerColumn(titles[2], weights[2], 2, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					return billed.getText();
				}
				return "";
			}
			
			@Override
			public Color getBackground(final Object element){
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					return getBackgroundColor(billed);
				}
				return null;
			}
		});
		
		col = createTableViewerColumn(titles[3], weights[3], 3, SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					Money price = billed.getPrice().multiply(billed.getAmount());
					return price.getAmountAsString();
				}
				return "";
			}
		});
		
		col = createTableViewerColumn(titles[4], weights[4], 4, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				return "";
			}
			
			@Override
			public Image getImage(Object element){
				return Images.IMG_DELETE.getImage();
			}
		});
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int weight, int colNumber,
		int style){
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, style);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(false);
		tableLayout.setColumnData(column, new ColumnWeightData(weight));
		return viewerColumn;
	}
	
	private Color getBackgroundColor(IBilled billed){
		IBillable billable = billed.getBillable();
		if (billable != null) {
			Color color = UiDesk.getColorFromRGB(defaultRGB);
			String codeName = billable.getCodeSystemName();
			
			if (codeName != null) {
				String rgbColor =
					CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_COLOR + codeName, defaultRGB);
				color = UiDesk.getColorFromRGB(rgbColor);
			}
			return color;
		}
		return null;
	}
	
	public void clear(){
		actEncounter = null;
		viewer.setInput(Collections.emptyList());
		updateBilledLabel();
	}
	
	private void updateBilledLabel(){
		if (actEncounter != null) {
			int sumMinutes = 0;
			Money sum = new Money(0);
			for (IBilled billed : actEncounter.getBilled()) {
				Money preis = billed.getPrice().multiply(billed.getAmount());
				sum.addMoney(preis);
				IBillable billable = billed.getBillable();
				if (billable instanceof IService) {
					sumMinutes += ((IService) billable).getMinutes();
				}
			}
			billedLabel.setText(Messages.PatHeuteView_accAmount + " " + sum.getAmountAsString()
				+ " / " + Messages.PatHeuteView_accTime + " " + sumMinutes);
		} else {
			billedLabel.setText("");
		}
		layout();
	}
	
	/**
	 * Add a {@link PersistentObject} to the encounter.
	 * 
	 * @param o
	 * @deprecated for {@link ch.elexis.core.model.ICodeElement} instances direct use of
	 *             {@link IBillingService} is recommended
	 */
	public void addPersistentObject(PersistentObject o){
		if (actEncounter != null) {
			if (o instanceof Leistungsblock) {
				Leistungsblock block = (Leistungsblock) o;
				List<ICodeElement> elements = block.getElements();
				for (ICodeElement element : elements) {
					if (element instanceof PersistentObject) {
						addPersistentObject((PersistentObject) element);
					}
				}
				List<ICodeElement> diff = block.getDiffToReferences(elements);
				if (!diff.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					diff.forEach(r -> {
						if (sb.length() > 0) {
							sb.append("\n");
						}
						sb.append(r);
					});
					MessageDialog.openWarning(getShell(), "Warnung",
						"Warnung folgende Leistungen konnten im aktuellen Kontext (Fall, Konsultation, Gesetz) nicht verrechnet werden.\n"
							+ sb.toString());
				}
			}
			if (o instanceof Prescription) {
				Prescription presc = (Prescription) o;
				o = presc.getArtikel();
			}
			if (o instanceof IVerrechenbar) {
				if (CoreHub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN) == false) {
					SWTHelper.alert(Messages.VerrechnungsDisplay_missingRightsCaption, //$NON-NLS-1$
						Messages.VerrechnungsDisplay_missingRightsBody); //$NON-NLS-1$
				} else {
					Result<IVerrechenbar> result =
						Konsultation.load(actEncounter.getId()).addLeistung((IVerrechenbar) o);
					
					if (!result.isOK()) {
						SWTHelper.alert(Messages.VerrechnungsDisplay_imvalidBilling,
							result.toString()); //$NON-NLS-1$
					}
					viewer.setInput(actEncounter.getBilled());
				}
			} else if (o instanceof IDiagnose) {
				Konsultation.load(actEncounter.getId()).addDiagnose((IDiagnose) o);
			}
		}
	}
	
	private final class DropReceiver implements GenericObjectDropTarget.IReceiver {
		
		public boolean accept(PersistentObject o){
			if (ElexisEventDispatcher.getSelectedPatient() != null) {
				if (o instanceof Artikel) {
					return !((Artikel) o).isProduct();
				}
				if (o instanceof IVerrechenbar) {
					return true;
				}
				if (o instanceof IDiagnose) {
					return true;
				}
				if (o instanceof Leistungsblock) {
					return true;
				}
				if (o instanceof Prescription) {
					Prescription p = ((Prescription) o);
					return (p.getArtikel() != null && !p.getArtikel().isProduct());
				}
			}
			return false;
		}
		
		private boolean accept(ch.elexis.core.model.ICodeElement codeElement){
			if (codeElement instanceof IArticle) {
				return !((IArticle) codeElement).isProduct();
			} else if (codeElement instanceof IService) {
				return true;
			} else if (codeElement instanceof IDiagnosis) {
				return true;
			}
			//			else if (codeElement instanceof IServiceBlock) {
			//				return true;
			//			}
			return false;
		}
		
		@Override
		public void dropped(List<Object> list, DropTargetEvent e){
			if (accept(list)) {
				for (Object object : list) {
					if (object instanceof PersistentObject) {
						addPersistentObject((PersistentObject) object);
					} else if (object instanceof IBillable) {
						IBillable billable = (IBillable) object;
						Result<IBilled> billResult =
								BillingServiceHolder.get().bill(billable, actEncounter, 1.0);
						if (!billResult.isOK()) {
							ResultDialog.show(billResult);
						}
					} else if (object instanceof IDiagnosis) {
						actEncounter.addDiagnosis((IDiagnosis) object);
					}
				}
			}
		}
		
		@Override
		public boolean accept(List<Object> list){
			for (Object object : list) {
				if (object instanceof PersistentObject) {
					if (!accept((PersistentObject) object)) {
						return false;
					}
				} else if (object instanceof ch.elexis.core.model.ICodeElement) {
					accept((ch.elexis.core.model.ICodeElement) object);
				}
			}
			return true;
		}
	}
	
	public void setEncounter(IEncounter encounter){
		actEncounter = encounter;
		if (actEncounter != null) {
			viewer.setInput(actEncounter.getBilled());
			updateBilledLabel();
		} else {
			viewer.setInput(Collections.emptyList());
			updateBilledLabel();
		}
	}
	
	/**
	 * Filter codes of {@link Verrechnet} where ID is used as code. This is relevant for {@link Eigenleistung} and Eigenartikel.
	 * 
	 * @param lst
	 * @return
	 */
	private String getServiceCode(IBilled billed){
		String ret = billed.getCode();
		IBillable billable = billed.getBillable();
		if (billable != null) {
			if (billable instanceof ILocalService || (billable instanceof IArticle
				&& ((IArticle) billable).getTyp() == ArticleTyp.EIGENARTIKEL)) {
				if (billable.getId().equals(ret)) {
					ret = "";
				}
			}
		}
		return ret;
	}
	
	private Menu createVerrMenu(){
		contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (selection.size() > 1) {
					manager.add(removeAction);
				} else {
					if (!selection.isEmpty()) {
						IBilled billed = (IBilled) selection.getFirstElement();
						IBillable billable = billed.getBillable();
						
						manager.add(chPriceAction);
						manager.add(chCountAction);
						// TODO WTF
						//						List<IAction> itemActions = (List<IAction>) (List<?>) vbar.getActions(v);
						//						if ((itemActions != null) && (itemActions.size() > 0)) {
						//							manager.add(new Separator());
						//							for (IAction a : itemActions) {
						//								if (a != null) {
						//									manager.add(a);
						//								}
						//							}
						//						}
						
						manager.add(new Separator());
						manager.add(chTextAction);
						manager.add(removeAction);
						manager.add(new Separator());
						manager.add(removeAllAction);
						if (billable instanceof IArticle) {
							manager.add(new Separator());
							manager.add(applyMedicationAction);
							// #8796
							manager.add(new Action(INDICATED_MEDICATION, Action.AS_CHECK_BOX) {
								@Override
								public void run(){
									IStructuredSelection selection =
										viewer.getStructuredSelection();
									for (Object selected : selection.toList()) {
										if (selected instanceof IBilled) {
											IBilled billed = (IBilled) selected;
											AcquireLockUi.aquireAndRun(billed,
												new LockDeniedNoActionLockHandler() {
													
													@Override
													public void lockAcquired(){
														if (isIndicated(billed)) {
															billed.setExtInfo(Verrechnet.INDICATED,
																"false");
														} else {
															billed.setExtInfo(Verrechnet.INDICATED,
																"true");
														}
													}
												});
										}
									}
								}
								
								private boolean isIndicated(IBilled billed){
									String value = (String) billed.getExtInfo(Verrechnet.INDICATED);
									return "true".equalsIgnoreCase(value);
								}
								
								@Override
								public boolean isChecked(){
									return isIndicated(billed);
								}
							});
						}
					}
				}
			}
		});
		return contextMenuManager.createContextMenu(table);
	}
	
	private void makeActions(){
		// #3278
		applyMedicationAction = new Action(APPLY_MEDICATION) {
			@Override
			public void run(){
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {
							@Override
							public void lockAcquired(){
								billed.setExtInfo(Verrechnet.VATSCALE, Double.toString(0.0));
								
								int packageSize =
									((IArticle) billed.getBillable()).getPackageSize();
								String proposal =
									(packageSize > 0) ? "1/" + packageSize : "1";
								changeQuantityDialog(proposal, billed);
								Object prescriptionId =
									billed.getExtInfo(Verrechnet.FLD_EXT_PRESC_ID);
								if (prescriptionId instanceof String) {
									Prescription prescription =
										Prescription.load((String) prescriptionId);
									if (prescription.getEntryType() == EntryType.SELF_DISPENSED) {
										prescription.setApplied(true);
									}
								}
							}
						});
					}
				}
			}
			
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_SYRINGE.getImageDescriptor();
			}
		};
		
		removeAction = new Action(REMOVE) {
			@Override
			public void run(){
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {
							@Override
							public void lockAcquired(){
								actEncounter.removeBilled(billed);
							}
						});
					}
				}
				setEncounter(actEncounter);
			}
		};
		
		removeAllAction = new Action(REMOVEALL) {
			@Override
			public void run(){
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				List<IBilled> allBilled = actEncounter.getBilled();
				for (IBilled billed : allBilled) {
					AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {
						@Override
						public void lockAcquired(){
							actEncounter.removeBilled(billed);
						}
					});
				}
				setEncounter(actEncounter);
			}
		};
		
		chPriceAction = new Action(CHPRICE) {
			
			@Override
			public void run(){
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {

							@Override
							public void lockAcquired(){
								Money oldPrice = billed.getPrice();
								String p = oldPrice.getAmountAsString();
								InputDialog dlg = new InputDialog(UiDesk.getTopShell(),
									Messages.VerrechnungsDisplay_changePriceForService, //$NON-NLS-1$
									Messages.VerrechnungsDisplay_enterNewPrice, p, //$NON-NLS-1$
									null);
								if (dlg.open() == Dialog.OK) {
									try {
										String val = dlg.getValue().trim();
										Money newPrice = new Money(oldPrice);
										if (val.endsWith("%") && val.length() > 1) { //$NON-NLS-1$
											val = val.substring(0, val.length() - 1);
											double percent = Double.parseDouble(val);
											double factor = 1.0 + (percent / 100.0);
											billed.setSecondaryScale((int) factor);
										} else {
											newPrice = new Money(val);
											billed.setPrice(newPrice);
											billed.setSecondaryScale(1);
											// mark as changed price
											billed.setExtInfo(Verrechnet.FLD_EXT_CHANGEDPRICE,
												"true");
										}
										CoreModelServiceHolder.get().save(billed);
										viewer.update(billed, null);
									} catch (ParseException ex) {
										SWTHelper.showError(
											Messages.VerrechnungsDisplay_badAmountCaption, //$NON-NLS-1$
											Messages.VerrechnungsDisplay_badAmountBody); //$NON-NLS-1$
									}
								}
							}
						});
					}
				}
				updateBilledLabel();
			}
		};
		
		chCountAction = new Action(CHCOUNT) {
			@Override
			public void run(){
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						String p = Double.toString(billed.getAmount());
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {
							
							@Override
							public void lockAcquired(){
								changeQuantityDialog(p, billed);
							}
						});
					}
				}
				updateBilledLabel();
			}
		};
		
		chTextAction = new Action(CHTEXT) {
			@Override
			public void run(){
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {
							@Override
							public void lockAcquired(){
								String oldText = billed.getText();
								InputDialog dlg = new InputDialog(UiDesk.getTopShell(),
									Messages.VerrechnungsDisplay_changeTextCaption, //$NON-NLS-1$
									Messages.VerrechnungsDisplay_changeTextBody, //$NON-NLS-1$
									oldText, null);
								if (dlg.open() == Dialog.OK) {
									String input = dlg.getValue();
									if (input.matches("[0-9\\.,]+")) { //$NON-NLS-1$
										if (!SWTHelper.askYesNo(
											Messages.VerrechnungsDisplay_confirmChangeTextCaption, //$NON-NLS-1$
											Messages.VerrechnungsDisplay_confirmChangeTextBody)) { //$NON-NLS-1$
											return;
										}
									}
									billed.setText(input);
									CoreModelServiceHolder.get().save(billed);
									viewer.update(billed, null);
								}
							}
						});
					}
				}
			}
		};
	}
	
	private void changeQuantityDialog(String p, IBilled billed){
		InputDialog dlg =
			new InputDialog(UiDesk.getTopShell(), Messages.VerrechnungsDisplay_changeNumberCaption, //$NON-NLS-1$
				Messages.VerrechnungsDisplay_changeNumberBody, //$NON-NLS-1$
				p, null);
		if (dlg.open() == Dialog.OK) {
			try {
				String val = dlg.getValue();
				if (!StringTool.isNothing(val)) {
					double changeAnzahl;
					IBillable billable = billed.getBillable();
					String text = billable.getText();
					if (val.indexOf(StringConstants.SLASH) > 0) {
						String[] frac = val.split(StringConstants.SLASH);
						changeAnzahl =
							Double.parseDouble(frac[0]) / Double.parseDouble(frac[1]);
						text = billed.getText() + " (" + val //$NON-NLS-1$
							+ Messages.VerrechnungsDisplay_Orininalpackungen;
					} else if (val.indexOf('.') > 0) {
						changeAnzahl =  Double.parseDouble(val);
						text = billed.getText() + " (" + Double.toString(changeAnzahl) + ")";
					} else {
						changeAnzahl = Integer.parseInt(dlg.getValue());
					}
					double diff = changeAnzahl - billed.getAmount();
					Result<IBilled> result =
						BillingServiceHolder.get().bill(billable, actEncounter, diff);
					if(!result.isOK()) {
						ResultDialog.show(result);
						return;
					}
					billed.setText(text);
					CoreModelServiceHolder.get().save(billed);
					viewer.update(billed, null);
				}
			} catch (NumberFormatException ne) {
				SWTHelper.showError(Messages.VerrechnungsDisplay_invalidEntryCaption, //$NON-NLS-1$
					Messages.VerrechnungsDisplay_invalidEntryBody); //$NON-NLS-1$
			}
		}
	}
	
	@Override
	public void setUnlocked(boolean unlocked) {
		setEnabled(unlocked);
		redraw();
	}
	
	public MenuManager getMenuManager(){
		return contextMenuManager;
	}
	
	public StructuredViewer getViewer(){
		return viewer;
	}
	
	public void adaptMenus(){
		table.getMenu().setEnabled(CoreHub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN));
	}
}
