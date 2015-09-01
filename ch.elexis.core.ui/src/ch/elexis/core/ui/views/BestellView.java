/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    Joerg M. Sigle (js, jsigle) - bug fixes
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.DailyOrderDialog;
import ch.elexis.core.ui.dialogs.NeueBestellungDialog;
import ch.elexis.core.ui.dialogs.OrderImportDialog;
import ch.elexis.core.ui.dialogs.SelectBestellungDialog;
import ch.elexis.core.ui.exchange.IDataSender;
import ch.elexis.core.ui.exchange.XChangeException;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.LagerartikelUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Artikel;
import ch.elexis.data.Bestellung;
import ch.elexis.data.Bestellung.Item;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;

public class BestellView extends ViewPart implements ISaveablePart2 {
	public static final String ID = "ch.elexis.BestellenView"; //$NON-NLS-1$
	Form form;
	FormToolkit tk = UiDesk.getToolkit();
	// LabeledInputField.AutoForm tblArtikel;
	TableViewer tv;
	Bestellung actBestellung;
	ViewMenus viewmenus;
	private IAction removeAction, dailyWizardAction, wizardAction, countAction, loadAction,
			saveAction, printAction, sendAction, newAction;
	private IAction exportClipboardAction, checkInAction;
	
	@Override
	public void createPartControl(final Composite parent){
		parent.setLayout(new FillLayout());
		form = tk.createForm(parent);
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		Table table = new Table(body, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		TableColumn tc0 = new TableColumn(table, SWT.CENTER);
		tc0.setText(Messages.BestellView_Number); //$NON-NLS-1$
		tc0.setWidth(40);
		TableColumn tc1 = new TableColumn(table, SWT.LEFT);
		tc1.setText(Messages.BestellView_Article); //$NON-NLS-1$
		tc1.setWidth(280);
		TableColumn tc2 = new TableColumn(table, SWT.LEFT);
		tc2.setText(Messages.BestellView_Dealer); //$NON-NLS-1$
		tc2.setWidth(250);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tv = new TableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(final Object inputElement){
				if (actBestellung != null) {
					return actBestellung.asList().toArray();
				}
				return new Object[0];
			}
			
			public void dispose(){}
			
			public void inputChanged(final Viewer viewer, final Object oldInput,
				final Object newInput){}
			
		});
		tv.setLabelProvider(new BestellungLabelProvider());
		tv.setSorter(new ViewerSorter() {
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2){
				String s1 = ((Item) e1).art.getName();
				String s2 = ((Item) e2).art.getName();
				return s1.compareTo(s2);
			}
			
		});
		Transfer[] types = new Transfer[] {
			TextTransfer.getInstance()
		};
		tv.addDropSupport(DND.DROP_COPY, types, new DropTargetAdapter() {
			
			@Override
			public void dragEnter(final DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			
			@Override
			public void drop(final DropTargetEvent event){
				String drp = (String) event.data;
				String[] dl = drp.split(","); //$NON-NLS-1$
				if (actBestellung == null) {
					NeueBestellungDialog nbDlg =
						new NeueBestellungDialog(getViewSite().getShell(),
							Messages.BestellView_CreateNewOrder,
							Messages.BestellView_EnterOrderTitle);
					if (nbDlg.open() == Dialog.OK) {
						setBestellung(new Bestellung(nbDlg.getTitle(), CoreHub.actUser));
					} else {
						return;
					}
				}
				for (String obj : dl) {
					PersistentObject dropped = CoreHub.poFactory.createFromString(obj);
					if (dropped instanceof Artikel) {
						actBestellung.addItem((Artikel) dropped, 1);
					}
				}
				tv.refresh();
			}
			
		});
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.toList().size() > 1) {
					countAction.setEnabled(false);
				} else {
					countAction.setEnabled(true);
				}
			}
		});
		makeActions();
		viewmenus = new ViewMenus(getViewSite());
		viewmenus.createToolbar(newAction, dailyWizardAction, wizardAction, saveAction, loadAction,
			printAction, sendAction);
		viewmenus.createMenu(newAction, dailyWizardAction, wizardAction, saveAction, loadAction,
			printAction, sendAction, exportClipboardAction);
		viewmenus.createViewerContextMenu(tv, new IAction[] {
			removeAction, countAction
		});
		form.getToolBarManager().add(checkInAction);
		form.updateToolBar();
		setBestellung(null);
		tv.setInput(getViewSite());
	}
	
	private void setBestellung(final Bestellung b){
		actBestellung = b;
		if (b != null) {
			form.setText(b.getLabel());
			tv.refresh();
			saveAction.setEnabled(true);
			checkInAction.setEnabled(true);
		} else {
			saveAction.setEnabled(false);
			checkInAction.setEnabled(false);
		}
	}
	
	@Override
	public void dispose(){
		/*
		 * GlobalEvents.getInstance().removeSelectionListener(this);
		 * cv.getConfigurer().getContentProvider().stopListening();
		 */
		super.dispose();
	}
	
	@Override
	public void setFocus(){
		
	}
	
	class BestellungLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(final Object element, final int columnIndex){
			return null;
		}
		
		public String getColumnText(final Object element, final int columnIndex){
			if (element instanceof Bestellung.Item) {
				Item it = (Item) element;
				switch (columnIndex) {
				case 0:
					return Integer.toString(it.num);
				case 1:
					return it.art.getLabel();
				case 2:
					Kontakt k = it.art.getLieferant();
					return k.exists() ? k.getLabel() : Messages.BestellView_Unknown; //$NON-NLS-1$
				default:
					return "?"; //$NON-NLS-1$
				}
			}
			return "??"; //$NON-NLS-1$
		}
		
	}
	
	private void makeActions(){
		removeAction = new Action(Messages.BestellView_RemoveArticle) { //$NON-NLS-1$
				@Override
				public void run(){
					IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
					if ((sel != null) && (!sel.isEmpty())) {
						if (actBestellung != null) {
							List<Item> selections = sel.toList();
							for (Item item : selections) {
								actBestellung.removeItem(item);
							}
							tv.refresh();
						}
					}
				}
			};
		dailyWizardAction = new Action(Messages.BestellView_AutomaticDailyOrder) {
			{
				setToolTipText(Messages.BestellView_CreateAutomaticDailyOrder);
				setImageDescriptor(Images.IMG_WIZ_DAY.getImageDescriptor());
			}
			
			@Override
			public void run(){
				if (actBestellung == null) {
					setBestellung(new Bestellung(Messages.BestellView_AutomaticDaily,
						CoreHub.actUser)); //$NON-NLS-1$
				}
				
				DailyOrderDialog doDlg = new DailyOrderDialog(UiDesk.getTopShell(), actBestellung);
				doDlg.open();
				tv.refresh(true);
			}
			
			@Override
			public boolean isEnabled(){
				boolean considerOrdered =
					CoreHub.globalCfg.get(Preferences.INVENTORY_MARK_AS_ORDERED,
						Preferences.INVENTORY_MARK_AS_ORDERED_DEFAULT);
				return !considerOrdered;
			}
			
		};
		
		wizardAction = new Action(Messages.BestellView_AutomaticOrder) { //$NON-NLS-1$
				{
					setToolTipText(Messages.BestellView_CreateAutomaticOrder); //$NON-NLS-1$
					setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
				}
				
				@Override
				public void run(){
					if (actBestellung == null) {
						setBestellung(new Bestellung(Messages.BestellView_Automatic,
							CoreHub.actUser)); //$NON-NLS-1$
					}
					/*
					 * Query<Artikel> qbe=new Query<Artikel>(Artikel.class);
					 * qbe.add("Minbestand","<>","0"); List<Artikel> l=qbe.execute();
					 */
					
					int trigger =
						CoreHub.globalCfg.get(Preferences.INVENTORY_ORDER_TRIGGER,
							Preferences.INVENTORY_ORDER_TRIGGER_DEFAULT);
					
					List<Artikel> l = LagerartikelUtil.getAllLagerartikel();
					for (Artikel a : l) {
						if ((a == null) || (!a.exists())) {
							continue;
						}
						// String name = a.getLabel();
						int ist = a.getIstbestand();
						int min = a.getMinbestand();
						int max = a.getMaxbestand();
						
						boolean order = false;
						switch (trigger) {
						case Preferences.INVENTORY_ORDER_TRIGGER_BELOW:
							order = (ist < min);
							break;
						case Preferences.INVENTORY_ORDER_TRIGGER_EQUAL:
							order = (ist <= min);
							break;
						default:
							order = (ist < min);
						}
						if (order) {
							boolean considerOrdered =
								CoreHub.globalCfg.get(Preferences.INVENTORY_MARK_AS_ORDERED,
									Preferences.INVENTORY_MARK_AS_ORDERED_DEFAULT);
							
							boolean alreadyOrdered = false;
							if (considerOrdered) {
								alreadyOrdered =
									a.getExt(Bestellung.ISORDERED).equalsIgnoreCase("true");
							}
							
							int toOrder = max - ist;
							if (toOrder > 0 && !alreadyOrdered) {
								actBestellung.addItem(a, toOrder);
							}
						}
					}
					tv.refresh(true);
				}
			};
		countAction = new Action(Messages.BestellView_ChangeNumber) { //$NON-NLS-1$
				@Override
				public void run(){
					IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
					if ((sel != null) && (!sel.isEmpty())) {
						Item it = (Item) sel.getFirstElement();
						int old = it.num;
						InputDialog in =
							new InputDialog(getViewSite().getShell(),
								Messages.BestellView_ChangeNumber, //$NON-NLS-1$
								Messages.BestellView_EnterNewNumber, //$NON-NLS-1$
								Integer.toString(old), null);
						if (in.open() == Dialog.OK) {
							it.num = Integer.parseInt(in.getValue());
							tv.refresh(it, true);
						}
					}
				}
			};
		saveAction = new Action(Messages.BestellView_SavelIst) { //$NON-NLS-1$
				@Override
				public void run(){
					if (actBestellung != null) {
						actBestellung.save();
					}
				}
			};
		newAction = new Action(Messages.BestellView_CreateNewOrder) { //$NON-NLS-1$
				@Override
				public void run(){
					if (actBestellung != null) {
						actBestellung.save();
					}
					NeueBestellungDialog nbDlg =
						new NeueBestellungDialog(getViewSite().getShell(),
							Messages.BestellView_CreateNewOrder,
							Messages.BestellView_EnterOrderTitle);
					if (nbDlg.open() == Dialog.OK) {
						setBestellung(new Bestellung(nbDlg.getTitle(), CoreHub.actUser));
					} else {
						return;
					}
					tv.refresh();
				}
			};
		printAction = new Action(Messages.BestellView_PrintOrder) { //$NON-NLS-1$
				@Override
				public void run(){
					if (actBestellung != null) {
						actBestellung.save();
						// make backup of list
						Item[] bkpList = actBestellung.asList().toArray(new Item[0]);
						
						List<Item> list = actBestellung.asList();
						ArrayList<Item> best = new ArrayList<Item>();
						Kontakt adressat = null;
						Iterator<Item> iter = list.iterator();
						while (iter.hasNext()) {
							Item it = (Item) iter.next();
							if (adressat == null) {
								adressat = it.art.getLieferant();
								if (!adressat.exists()) {
									adressat = null;
									continue;
								}
							}
							if (it.art.getLieferant().getId().equals(adressat.getId())) {
								best.add(it);
								iter.remove();
							}
						}
						
						try {
							BestellBlatt bb =
								(BestellBlatt) getViewSite().getPage().showView(BestellBlatt.ID);
							bb.createOrder(adressat, best);
							tv.refresh();
							
							// mark ordered articles if preferences wish so
							boolean markAsOrdered =
								CoreHub.globalCfg.get(Preferences.INVENTORY_MARK_AS_ORDERED,
									Preferences.INVENTORY_MARK_AS_ORDERED_DEFAULT);
							if (markAsOrdered) {
								Bestellung.markAsOrdered(bkpList);
							}
						} catch (PartInitException e) {
							ExHandler.handle(e);
							
						}
					}
				}
			};
		sendAction = new Action(Messages.BestellView_SendOrder) { //$NON-NLS-1$
				@Override
				public void run(){
					if (actBestellung == null)
						return;
					actBestellung.save();
					
					// organise items in supplier and non-supplier lists
					List<Item> orderableItems = new ArrayList<Bestellung.Item>();
					List<Item> noSupplierItems = new ArrayList<Bestellung.Item>();
					for (Item item : actBestellung.asList()) {
						Kontakt supplier = item.art.getLieferant();
						if (supplier != null && supplier.exists()) {
							orderableItems.add(item);
						} else {
							noSupplierItems.add(item);
						}
					}
					
					boolean runOrder = true;
					if (!noSupplierItems.isEmpty()) {
						StringBuilder sb = new StringBuilder();
						for (Item noSupItem : noSupplierItems) {
							sb.append(noSupItem.art.getLabel());
							sb.append("\n");
						}
						runOrder =
							SWTHelper.askYesNo(
								Messages.BestellView_NoSupplierArticle,
								MessageFormat.format(Messages.BestellView_NoSupplierArticleMsg,
									sb.toString()));
					}
					
					if (runOrder) {
						List<IConfigurationElement> list =
							Extensions.getExtensions(ExtensionPointConstantsUi.TRANSPORTER); //$NON-NLS-1$
						for (IConfigurationElement ic : list) {
							String handler = ic.getAttribute("type"); //$NON-NLS-1$
							if (handler != null && handler.contains("ch.elexis.data.Bestellung")) { //$NON-NLS-1$
								try {
									IDataSender sender =
										(IDataSender) ic.createExecutableExtension("ExporterClass"); //$NON-NLS-1$
									
									sender.store(actBestellung);
									sender.finalizeExport();
									SWTHelper.showInfo(Messages.BestellView_OrderSentCaption, //$NON-NLS-1$
										Messages.BestellView_OrderSentBody); //$NON-NLS-1$
									tv.refresh();
									
									// mark ordered articles if prefs say so
									boolean markAsOrdered =
										CoreHub.globalCfg.get(
											Preferences.INVENTORY_MARK_AS_ORDERED,
											Preferences.INVENTORY_MARK_AS_ORDERED_DEFAULT);
									if (markAsOrdered) {
										Item[] bkpList =
											orderableItems.toArray(new Item[orderableItems.size()]);
										Bestellung.markAsOrdered(bkpList);
									}
								} catch (CoreException ex) {
									ExHandler.handle(ex);
								} catch (XChangeException xx) {
									SWTHelper.showError(
										Messages.BestellView_OrderNotPossible, //$NON-NLS-1$
										Messages.BestellView_NoAutomaticOrderAvailable
											+ xx.getLocalizedMessage()); //$NON-NLS-1$
									
								}
							}
						}
					}
				}
			};
		loadAction = new Action(Messages.BestellView_OpenOrder) { //$NON-NLS-1$
				@Override
				public void run(){
					
					SelectBestellungDialog dlg =
						new SelectBestellungDialog(getViewSite().getShell());
					dlg.setMessage(Messages.BestellView_SelectOrder); //$NON-NLS-1$
					dlg.setTitle(Messages.BestellView_ReadOrder); //$NON-NLS-1$
					
					if (dlg.open() == Dialog.OK) {
						// js: Only a non-empty result should be used any further.
						if (dlg.getResult().length > 0) {
							Bestellung res = (Bestellung) dlg.getResult()[0];
							setBestellung(res);
						}
					}
				}
			};
		printAction.setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
		printAction.setToolTipText(Messages.BestellView_PrintOrder); //$NON-NLS-1$
		
		newAction.setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor()); //$NON-NLS-1$
		newAction.setToolTipText(Messages.BestellView_CreateNewOrder); //$NON-NLS-1$
		saveAction.setImageDescriptor(Images.IMG_DISK.getImageDescriptor()); //$NON-NLS-1$
		saveAction.setToolTipText(Messages.BestellView_saveOrder); //$NON-NLS-1$
		sendAction.setImageDescriptor(Images.IMG_NETWORK.getImageDescriptor());
		sendAction.setToolTipText(Messages.BestellView_transmitOrder); //$NON-NLS-1$
		loadAction.setImageDescriptor(Images.IMG_IMPORT.getImageDescriptor());
		loadAction.setToolTipText(Messages.BestellView_loadEarlierOrder); //$NON-NLS-1$
		
		exportClipboardAction = new Action(Messages.BestellView_copyToClipboard) { //$NON-NLS-1$
				{
					setToolTipText(Messages.BestellView_copyToClipBioardForGalexis); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					if (actBestellung != null) {
						List<Item> list = actBestellung.asList();
						ArrayList<Item> best = new ArrayList<Item>();
						Kontakt adressat = null;
						Iterator<Item> iter = list.iterator();
						while (iter.hasNext()) {
							Item it = (Item) iter.next();
							if (adressat == null) {
								adressat = it.art.getLieferant();
								if (!adressat.exists()) {
									adressat = null;
									continue;
								}
							}
							if (it.art.getLieferant().getId().equals(adressat.getId())) {
								best.add(it);
								iter.remove();
							}
						}
						
						StringBuffer export = new StringBuffer();
						for (Item item : best) {
							String pharmaCode = item.art.get(Artikel.FLD_PHARMACODE);
							int num = item.num;
							String name = item.art.getName();
							String line = pharmaCode + ", " + num + ", " + name; //$NON-NLS-1$ //$NON-NLS-2$
							
							export.append(line);
							export.append(System.getProperty("line.separator")); //$NON-NLS-1$
						}
						
						String clipboardText = export.toString();
						Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
						TextTransfer textTransfer = TextTransfer.getInstance();
						Transfer[] transfers = new Transfer[] {
							textTransfer
						};
						Object[] data = new Object[] {
							clipboardText
						};
						clipboard.setContents(data, transfers);
						clipboard.dispose();
					}
				}
			};
		checkInAction = new Action(Messages.BestellView_CheckInCaption) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_TICK.getImageDescriptor());
					setToolTipText(Messages.BestellView_CheckInBody); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					if (actBestellung != null && actBestellung.exists()) {
						OrderImportDialog dialog =
							new OrderImportDialog(getSite().getShell(), actBestellung);
						dialog.open();
					} else {
						SWTHelper.alert(Messages.BestellView_NoOrder, //$NON-NLS-1$
							Messages.BestellView_NoOrderLoaded); //$NON-NLS-1$
					}
				}
				
			};
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
	
	/**
	 * @return the current defined {@link Bestellung} in this view
	 */
	public Bestellung getActBestellung(){
		return actBestellung;
	}
	
	/**
	 * Find the default supplier. Shows a warning if supplier is null or inexisting
	 * 
	 * @param cfgSupplier
	 *            value delivered from the plugins configured supplier field
	 * @param selDialogTitle
	 *            title of the dialog
	 * @return the supplier or null if none could be resolved.
	 */
	public static Kontakt resolveDefaultSupplier(String cfgSupplier, String selDialogTitle){
		Kontakt supplier = null;
		if (cfgSupplier != null && !cfgSupplier.isEmpty()) {
			supplier = Kontakt.load(cfgSupplier);
		}
		
		//warn that there is no supplier
		if (supplier == null || !supplier.exists()) {
			MessageDialog.openWarning(UiDesk.getTopShell(), selDialogTitle,
				Messages.BestellView_CantOrderNoSupplier);
		}
		return supplier;
	}
}
