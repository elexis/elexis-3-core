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
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Leistungsblock;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Prescription;
import ch.elexis.core.data.Verrechnet;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.admin.AccessControlDefaults;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class VerrechnungsDisplay extends Composite {
	Table tVerr;
	TableViewer viewer;
	MenuManager contextMenuManager;
	private final Hyperlink hVer;
	private final PersistentObjectDropTarget dropTarget;
	private IAction chPriceAction, chCountAction, chTextAction, removeAction, removeAllAction;
	private static final String CHPRICE = Messages.getString("VerrechnungsDisplay.changePrice"); //$NON-NLS-1$
	private static final String CHCOUNT = Messages.getString("VerrechnungsDisplay.changeNumber"); //$NON-NLS-1$
	private static final String REMOVE = Messages.getString("VerrechnungsDisplay.removeElement"); //$NON-NLS-1$
	private static final String CHTEXT = Messages.getString("VerrechnungsDisplay.changeText"); //$NON-NLS-1$
	private static final String REMOVEALL = Messages.getString("VerrechnungsDisplay.removeAll"); //$NON-NLS-1$
	
	public VerrechnungsDisplay(final IWorkbenchPage page, Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout());
		hVer =
			UiDesk.getToolkit().createHyperlink(this,
				Messages.getString("VerrechnungsDisplay.billing"), SWT.NONE); //$NON-NLS-1$
		hVer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		hVer.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e){
				try {
					if (StringTool.isNothing(LeistungenView.ID)) {
						SWTHelper.alert(
							Messages.getString("VerrechnungsDisplay.error"), "LeistungenView.ID"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					page.showView(LeistungenView.ID);
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
				} catch (Exception ex) {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							Messages.getString("VerrechnungsDisplay.errorStartingCodeWindow")
								+ ex.getMessage(), ex, ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		makeActions();
		tVerr = UiDesk.getToolkit().createTable(this, SWT.SINGLE);
		tVerr.setLayoutData(new GridData(GridData.FILL_BOTH));
		tVerr.setMenu(createVerrMenu());
		// dummy table viewer needed for SelectionsProvider for Menu
		viewer = new TableViewer(tVerr);
		// add selection event to table which provides selection to ElexisEventDispatcher
		tVerr.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				TableItem[] selection = tVerr.getSelection();
				Verrechnet verrechnet = (Verrechnet) selection[0].getData();
				ElexisEventDispatcher.fireSelectionEvent(verrechnet);
			}
		});
		dropTarget =
			new PersistentObjectDropTarget(
				Messages.getString("VerrechnungsDisplay.doBill"), tVerr, new DropReceiver()); //$NON-NLS-1$
		// refresh the table if a update to a Verrechnet occurs
		ElexisEventDispatcher.getInstance().addListeners(
			new ElexisUiEventListenerImpl(Verrechnet.class, ElexisEvent.EVENT_UPDATE) {
				@Override
				public void runInUi(ElexisEvent ev){
					Konsultation actKons =
						(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
					setLeistungen(actKons);
				}
			});
	}
	
	public void clear(){
		tVerr.removeAll();
	}
	
	public void addPersistentObject(PersistentObject o){
		Konsultation actKons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		if (actKons != null) {
			// System.out.println(actKons.getLabel());
			if (o instanceof Prescription) {
				o = ((Prescription) o).getArtikel();
			}
			if (o instanceof IVerrechenbar) {
				if (CoreHub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN) == false) {
					SWTHelper.alert(Messages.getString("VerrechnungsDisplay.missingRightsCaption"), //$NON-NLS-1$
						Messages.getString("VerrechnungsDisplay.missingRightsBody")); //$NON-NLS-1$
				} else {
					Result<IVerrechenbar> result = actKons.addLeistung((IVerrechenbar) o);
					
					if (!result.isOK()) {
						SWTHelper
							.alert(
								Messages.getString("VerrechnungsDisplay.imvalidBilling"), result.toString()); //$NON-NLS-1$
					}
					setLeistungen(actKons);
				}
			}
		}
	}
	
	private final class DropReceiver implements PersistentObjectDropTarget.IReceiver {
		public void dropped(PersistentObject o, DropTargetEvent ev){
			addPersistentObject(o);
		}
		
		public boolean accept(PersistentObject o){
			if (ElexisEventDispatcher.getSelectedPatient() != null) {
				if (o instanceof IVerrechenbar) {
					return true;
				}
				if (o instanceof Leistungsblock) {
					return true;
				}
				if (o instanceof Prescription) {
					return true;
				}
			}
			return false;
		}
	}
	
	public void setLeistungen(Konsultation b){
		List<Verrechnet> lgl = b.getLeistungen();
		tVerr.setRedraw(false);
		tVerr.removeAll();
		StringBuilder sdg = new StringBuilder();
		Money sum = new Money(0);
		for (Verrechnet lst : lgl) {
			sdg.setLength(0);
			int z = lst.getZahl();
			Money preis = lst.getNettoPreis().multiply(z);
			sum.addMoney(preis);
			sdg.append(z).append(" ").append(lst.getCode()).append(" ").append(lst.getText()) //$NON-NLS-1$ //$NON-NLS-2$
				.append(" (").append(preis.getAmountAsString()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			TableItem ti = new TableItem(tVerr, SWT.WRAP);
			ti.setText(sdg.toString());
			ti.setData(lst);
		}
		tVerr.setRedraw(true);
		sdg.setLength(0);
		sdg.append(Messages.getString("VerrechnungsDisplay.billed")).append(sum.getAmountAsString()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		hVer.setText(sdg.toString());
	}
	
	private Menu createVerrMenu(){
		contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				int sel = tVerr.getSelectionIndex();
				if (sel != -1) {
					TableItem ti = tVerr.getItem(sel);
					Verrechnet v = (Verrechnet) ti.getData();
					manager.add(chPriceAction);
					manager.add(chCountAction);
					IVerrechenbar vbar = v.getVerrechenbar();
					List<IAction> itemActions = (List<IAction>)(List<?>)vbar.getActions(v);
					if ((itemActions != null) && (itemActions.size() > 0)) {
						manager.add(new Separator());
						for (IAction a : itemActions) {
							if (a != null) {
								manager.add(a);
							}
						}
					}
					manager.add(new Separator());
					manager.add(chTextAction);
					manager.add(removeAction);
					manager.add(new Separator());
					manager.add(removeAllAction);
				}
			}
		});
		return contextMenuManager.createContextMenu(tVerr);
	}
	
	private void makeActions(){
		removeAction = new Action(REMOVE) {
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Result<Verrechnet> result =
					((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class))
						.removeLeistung((Verrechnet) ti.getData());
				if (!result.isOK()) {
					SWTHelper.alert(
						Messages.getString("VerrechnungsDisplay.PositionCanootBeRemoved"), result //$NON-NLS-1$
							.toString());
				}
				setLeistungen((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
			}
		};
		removeAllAction = new Action(REMOVEALL) {
			@Override
			public void run(){
				TableItem[] items = tVerr.getItems();
				for (TableItem ti : items) {
					Result<Verrechnet> result =
						((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class))
							.removeLeistung((Verrechnet) ti.getData());
					if (!result.isOK()) {
						SWTHelper
							.alert(
								Messages.getString("VerrechnungsDisplay.PositionCanootBeRemoved"), result //$NON-NLS-1$
									.toString());
					}
				}
				setLeistungen((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
			}
		};
		chPriceAction = new Action(CHPRICE) {
			
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				Money oldPrice = v.getBruttoPreis();
				String p = oldPrice.getAmountAsString();
				InputDialog dlg =
					new InputDialog(UiDesk.getTopShell(),
						Messages.getString("VerrechnungsDisplay.changePriceForService"), //$NON-NLS-1$
						Messages.getString("VerrechnungsDisplay.enterNewPrice"), p, //$NON-NLS-1$
						null);
				if (dlg.open() == Dialog.OK) {
					try {
						String val = dlg.getValue().trim();
						Money newPrice = new Money(oldPrice);
						if (val.endsWith("%") && val.length() > 1) { //$NON-NLS-1$
							val = val.substring(0, val.length() - 1);
							double percent = Double.parseDouble(val);
							double factor = 1.0 + (percent / 100.0);
							v.setSecondaryScaleFactor(factor);
						} else {
							newPrice = new Money(val);
							v.setTP(newPrice.getCents());
							v.setSecondaryScaleFactor(1);
						}
						// v.setPreis(newPrice);
						setLeistungen((Konsultation) ElexisEventDispatcher
							.getSelected(Konsultation.class));
					} catch (ParseException ex) {
						SWTHelper.showError(
							Messages.getString("VerrechnungsDisplay.badAmountCaption"), //$NON-NLS-1$
							Messages.getString("VerrechnungsDisplay.badAmountBody")); //$NON-NLS-1$
					}
				}
			}
			
		};
		chCountAction = new Action(CHCOUNT) {
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				String p = Integer.toString(v.getZahl());
				InputDialog dlg =
					new InputDialog(UiDesk.getTopShell(),
						Messages.getString("VerrechnungsDisplay.changeNumberCaption"), //$NON-NLS-1$
						Messages.getString("VerrechnungsDisplay.changeNumberBody"), //$NON-NLS-1$
						p, null);
				if (dlg.open() == Dialog.OK) {
					try {
						String val = dlg.getValue();
						if (!StringTool.isNothing(val)) {
							if (val.indexOf('/') > 0) {
								String[] frac = val.split("/"); //$NON-NLS-1$
								v.changeAnzahl(1);
								double scale =
									Double.parseDouble(frac[0]) / Double.parseDouble(frac[1]);
								v.setSecondaryScaleFactor(scale);
								v.setText(v.getText()
									+ " (" + val + Messages.getString("VerrechnungsDisplay.Orininalpackungen")); //$NON-NLS-1$ //$NON-NLS-2$
							} else if (val.indexOf('.') > 0) {
								double scale = Double.parseDouble(val);
								v.changeAnzahl(1);
								v.setSecondaryScaleFactor(scale);
								v.setText(v.getText() + " (" + Double.toString(scale) + ")");
							} else {
								int neu = Integer.parseInt(dlg.getValue());
								v.changeAnzahl(neu);
								v.setSecondaryScaleFactor(1.0);
								v.setText(v.getVerrechenbar().getText());
							}
						}
						setLeistungen((Konsultation) ElexisEventDispatcher
							.getSelected(Konsultation.class));
						v.getVerrechenbar()
							.getOptifier()
							.optify(
								(Konsultation) ElexisEventDispatcher
									.getSelected(Konsultation.class));
					} catch (NumberFormatException ne) {
						SWTHelper.showError(
							Messages.getString("VerrechnungsDisplay.invalidEntryCaption"), //$NON-NLS-1$
							Messages.getString("VerrechnungsDisplay.invalidEntryBody")); //$NON-NLS-1$
					}
				}
			}
		};
		
		chTextAction = new Action(CHTEXT) {
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				String oldText = v.getText();
				InputDialog dlg =
					new InputDialog(UiDesk.getTopShell(),
						Messages.getString("VerrechnungsDisplay.changeTextCaption"), //$NON-NLS-1$
						Messages.getString("VerrechnungsDisplay.changeTextBody"), //$NON-NLS-1$
						oldText, null);
				if (dlg.open() == Dialog.OK) {
					String input = dlg.getValue();
					if (input.matches("[0-9\\.,]+")) { //$NON-NLS-1$
						if (!SWTHelper.askYesNo(
							Messages.getString("VerrechnungsDisplay.confirmChangeTextCaption"), //$NON-NLS-1$
							Messages.getString("VerrechnungsDisplay.confirmChangeTextBody"))) { //$NON-NLS-1$
							return;
						}
					}
					v.setText(input);
					setLeistungen((Konsultation) ElexisEventDispatcher
						.getSelected(Konsultation.class));
				}
			}
		};
	}
}
