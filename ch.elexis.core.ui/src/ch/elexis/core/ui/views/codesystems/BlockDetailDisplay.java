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

package ch.elexis.core.ui.views.codesystems;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.Eigenleistung;
import ch.elexis.core.data.Leistungsblock;
import ch.elexis.core.data.Mandant;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.datatypes.ICodeElement;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.commands.CreateEigenleistungUi;
import ch.elexis.core.ui.commands.EditEigenleistungUi;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.rgw.tools.StringTool;

public class BlockDetailDisplay implements IDetailDisplay {
	ScrolledForm form;
	FormToolkit tk;
	Text tName;
	Combo cbMandant;
	ListViewer lLst;
	Button bNew, bEigen;
	List<Mandant> lMandanten;
	private static Log log = Log.get("BlockDetail"); //$NON-NLS-1$
	private Action removeLeistung, moveUpAction, moveDownAction, editAction;
	IViewSite site;
	
	public Composite createDisplay(final Composite parent, final IViewSite site){
		tk = UiDesk.getToolkit();
		this.site = site;
		form = tk.createScrolledForm(parent);
		Composite body = form.getBody();
		body.setBackground(parent.getBackground());
		body.setLayout(new GridLayout(2, false));
		tk.createLabel(body, Messages.BlockDetailDisplay_name).setBackground(parent.getBackground()); //$NON-NLS-1$
		tName = tk.createText(body, "", SWT.BORDER); //$NON-NLS-1$
		tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.createLabel(body, StringConstants.MANDATOR).setBackground(parent.getBackground());
		cbMandant = new Combo(body, SWT.NONE);
		cbMandant.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.adapt(cbMandant);
		Query<Mandant> qm = new Query<Mandant>(Mandant.class);
		lMandanten = qm.execute();
		cbMandant.add(Messages.BlockDetailDisplay_all); //$NON-NLS-1$
		for (PersistentObject m : lMandanten) {
			cbMandant.add(m.getLabel());
		}
		cbMandant.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				int idx = cbMandant.getSelectionIndex();
				Leistungsblock lb =
					(Leistungsblock) ElexisEventDispatcher.getSelected(Leistungsblock.class);
				if (idx > 0) {
					PersistentObject m = lMandanten.get(idx - 1);
					lb.set(Leistungsblock.MANDANT_ID, m.getId());
				} else {
					lb.set(Leistungsblock.MANDANT_ID, StringConstants.EMPTY);
				}
				
			}
			
		});
		Group gList = new Group(body, SWT.BORDER);
		gList.setText(Messages.BlockDetailDisplay_services); //$NON-NLS-1$
		gList.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		gList.setLayout(new FillLayout());
		tk.adapt(gList);
		lLst = new ListViewer(gList, SWT.NONE);
		tk.adapt(lLst.getControl(), true, true);
		
		lLst.setContentProvider(new IStructuredContentProvider() {
			public void dispose(){}
			
			public void inputChanged(final Viewer viewer, final Object oldInput,
				final Object newInput){}
			
			public Object[] getElements(final Object inputElement){
				Leistungsblock lb =
					(Leistungsblock) ElexisEventDispatcher.getSelected(Leistungsblock.class);
				if (lb == null) {
					return new Object[0];
				}
				List<ICodeElement> lst = lb.getElements();
				if (lst == null) {
					return new Object[0];
				}
				return lst.toArray();
			}
			
		});
		lLst.setLabelProvider(new LabelProvider() {
			
			@Override
			public String getText(final Object element){
				ICodeElement v = (ICodeElement) element;
				return v.getCode() + StringConstants.SPACE + v.getText();
			}
			
		});
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] {
			textTransfer
		};
		lLst.addDropSupport(DND.DROP_COPY, types, new DropTargetListener() {
			public void dragEnter(final DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			
			public void dragLeave(final DropTargetEvent event){}
			
			public void dragOperationChanged(final DropTargetEvent event){}
			
			public void dragOver(final DropTargetEvent event){}
			
			public void drop(final DropTargetEvent event){
				String drp = (String) event.data;
				String[] dl = drp.split(","); //$NON-NLS-1$
				for (String obj : dl) {
					PersistentObject dropped = CoreHub.poFactory.createFromString(obj);
					if (dropped instanceof ICodeElement) {
						Leistungsblock lb =
							(Leistungsblock) ElexisEventDispatcher
								.getSelected(Leistungsblock.class);
						if (lb != null) {
							lb.addElement((ICodeElement) dropped);
							lLst.refresh();
							ElexisEventDispatcher.reload(Leistungsblock.class);
						}
					}
				}
				
			}
			
			public void dropAccept(final DropTargetEvent event){
				// TODO Automatisch erstellter Methoden-Stub
				
			}
			
		});
		bNew =
			tk.createButton(body,
				Messages.BlockDetailDisplay_addPredefinedServices, SWT.PUSH); //$NON-NLS-1$
		bNew.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bNew.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				try {
					site.getPage().showView(LeistungenView.ID);
				} catch (Exception ex) {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							"Fehler beim Starten des Leistungscodes " + ex.getMessage(), ex,
							ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		
		bEigen =
			tk.createButton(body,
				Messages.BlockDetailDisplay_addSelfDefinedServices, SWT.PUSH); //$NON-NLS-1$
		bEigen.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bEigen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				try {
					// execute the command
					IHandlerService handlerService =
						(IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getService(IHandlerService.class);
					
					handlerService.executeCommand(CreateEigenleistungUi.COMMANDID, null);
				} catch (Exception ex) {
					throw new RuntimeException(CreateEigenleistungUi.COMMANDID, ex);
				}
				lLst.refresh();
			}
		});
		makeActions();
		ViewMenus menus = new ViewMenus(site);
		menus.createControlContextMenu(lLst.getControl(), new ViewMenus.IMenuPopulator() {
			public IAction[] fillMenu(){
				IVerrechenbar iv =
					(IVerrechenbar) ((IStructuredSelection) lLst.getSelection()).getFirstElement();
				if (iv instanceof Eigenleistung) {
					return new IAction[] {
						moveUpAction, moveDownAction, null, removeLeistung, editAction
					};
				} else {
					return new IAction[] {
						moveUpAction, moveDownAction, null, removeLeistung
					};
				}
				
			}
		});
		// menus.createViewerContextMenu(lLst,moveUpAction,moveDownAction,null,removeLeistung,editAction);
		lLst.setInput(site);
		return body;
	}
	
	public Class<? extends PersistentObject> getElementClass(){
		return Leistungsblock.class;
	}
	
	public void display(final Object obj){
		if (obj == null) {
			bNew.setEnabled(false);
			tName.setText(StringConstants.EMPTY);
			cbMandant.select(0);
		} else {
			Leistungsblock lb = (Leistungsblock) obj;
			tName.setText(lb.get(Messages.BlockDetailDisplay_name)); //$NON-NLS-1$
			String mId = lb.get(Leistungsblock.MANDANT_ID);
			int sel = 0;
			if (!StringTool.isNothing(mId)) {
				String[] items = cbMandant.getItems();
				sel = StringTool.getIndex(items, Mandant.load(mId).getLabel());
			}
			cbMandant.select(sel);
			bNew.setEnabled(true);
		}
		lLst.refresh(true);
	}
	
	public String getTitle(){
		return Messages.BlockDetailDisplay_blocks; //$NON-NLS-1$
	}
	
	private void makeActions(){
		removeLeistung = new Action(Messages.BlockDetailDisplay_remove) { //$NON-NLS-1$
				@Override
				public void run(){
					Leistungsblock lb =
						(Leistungsblock) ElexisEventDispatcher.getSelected(Leistungsblock.class);
					if (lb != null) {
						IStructuredSelection sel = (IStructuredSelection) lLst.getSelection();
						Object o = sel.getFirstElement();
						if (o != null) {
							lb.removeElement((ICodeElement) o);
							lLst.refresh();
						}
					}
				}
			};
		moveUpAction = new Action(Messages.BlockDetailDisplay_moveUp) { //$NON-NLS-1$
				@Override
				public void run(){
					moveElement(-1);
				}
			};
		moveDownAction = new Action(Messages.BlockDetailDisplay_moveDown) { //$NON-NLS-1$
				@Override
				public void run(){
					moveElement(1);
				}
			};
		editAction = new Action(Messages.BlockDetailDisplay_changeAction) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
					setToolTipText(Messages.BlockDetailDisplay_changeActionTooltip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					IStructuredSelection sel = (IStructuredSelection) lLst.getSelection();
					PersistentObject parameter = (PersistentObject) sel.getFirstElement();
					EditEigenleistungUi.executeWithParams(parameter);
				}
			};
	}
	
	private void moveElement(final int off){
		Leistungsblock lb =
			(Leistungsblock) ElexisEventDispatcher.getSelected(Leistungsblock.class);
		if (lb != null) {
			IStructuredSelection sel = (IStructuredSelection) lLst.getSelection();
			Object o = sel.getFirstElement();
			if (o != null) {
				lb.moveElement((ICodeElement) o, off);
				lLst.refresh();
			}
		}
		
	}
}
