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

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.commands.AufNewHandler;
import ch.elexis.core.ui.commands.AufPrintHandler;
import ch.elexis.core.ui.dialogs.EditAUFDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.AUF;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;

/**
 * Arbeitsunfähigkeitszeugnisse erstellen und verwalten.
 * 
 * @author gerry
 * 
 */
public class AUF2 extends ViewPart implements IActivationListener {
	public static final String ID = "ch.elexis.auf"; //$NON-NLS-1$
	TableViewer tv;
	private Action newAUF, delAUF, modAUF, printAUF;
	private ElexisEventListener eli_auf = new ElexisUiEventListenerImpl(AUF.class) {
		
		@Override
		public void runInUi(ElexisEvent ev){
			boolean bSelect = (ev.getType() == ElexisEvent.EVENT_SELECTED);
			modAUF.setEnabled(bSelect);
			delAUF.setEnabled(bSelect);
			
			if (bSelect && tv != null) {
				// refresh & select only if not already selected
				if (ev.getObject() instanceof AUF && !Objects.equals(tv.getStructuredSelection(),
					new StructuredSelection(ev.getObject()))) {
					tv.refresh(false);
					tv.setSelection(new StructuredSelection(ev.getObject()));
				}
			}
		}
	};
	private ElexisEventListener eli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		
		@Override
		public void runInUi(ElexisEvent ev){
			if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
				tv.refresh();
				ElexisEventDispatcher.clearSelection(AUF.class);
				newAUF.setEnabled(true);
			} else {
				newAUF.setEnabled(false);
				modAUF.setEnabled(false);
				delAUF.setEnabled(false);
				
			}
		}
	};
	
	public AUF2(){
		setTitleImage(Images.IMG_VIEW_WORK_INCAPABLE.getImage());
	}
	
	@Override
	public void createPartControl(Composite parent){
		// setTitleImage(Desk.getImage(ICON));
		setPartName(Messages.AUF2_certificate); //$NON-NLS-1$
		tv = new TableViewer(parent);
		tv.setLabelProvider(new DefaultLabelProvider());
		tv.setContentProvider(new AUFContentProvider());
		makeActions();
		ViewMenus menus = new ViewMenus(getViewSite());
		menus.createMenu(newAUF, delAUF, modAUF, printAUF);
		menus.createToolbar(newAUF, delAUF, printAUF);
		tv.setUseHashlookup(true);
		GlobalEventDispatcher.addActivationListener(this, this);
		tv.addSelectionChangedListener(GlobalEventDispatcher.getInstance().getDefaultListener());
		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				modAUF.run();
			}
		});
		tv.setInput(getViewSite());
		
		final Transfer[] dragTransferTypes = new Transfer[] {
			TextTransfer.getInstance()
		};
		
		tv.addDragSupport(DND.DROP_COPY, dragTransferTypes, new DragSourceAdapter() {
			
			@Override
			public void dragSetData(DragSourceEvent event){
				IStructuredSelection selection = (IStructuredSelection) tv.getSelection();
				StringBuilder sb = new StringBuilder();
				if (selection != null && !selection.isEmpty()) {
					AUF auf = (AUF) selection.getFirstElement();
					sb.append(auf.storeToString()).append(","); //$NON-NLS-1$
				}
				event.data = sb.toString().replace(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	private void makeActions(){
		newAUF = new Action(Messages.AUF2_new) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.AUF2_createNewCert); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				IHandlerService handlerService =
					(IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				try {
					Object createdAuf = handlerService.executeCommand(AufNewHandler.CMD_ID, null);
					if (createdAuf instanceof AUF) {
						ElexisEventDispatcher.fireSelectionEvent((AUF) createdAuf);
					}
				} catch (Exception e) {
					LoggerFactory.getLogger(BriefAuswahl.class).error("cannot execute cmd", e);
				}
			}
		};
		delAUF = new Action(Messages.AUF2_delete) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.AUF2_deleteCertificate); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				AUF sel = getSelectedAUF();
				if (sel != null) {
					if (MessageDialog.openConfirm(getViewSite().getShell(),
						Messages.AUF2_deleteReally, Messages.AUF2_doyoywantdeletereally)) { //$NON-NLS-1$ //$NON-NLS-2$
						sel.delete();
						tv.refresh(false);
					}
				}
			}
		};
		modAUF = new Action(Messages.AUF2_edit) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.AUF2_editCertificate); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				AUF sel = getSelectedAUF();
				if (sel != null) {
					new EditAUFDialog(getViewSite().getShell(), sel, sel.getFall()).open();
					tv.refresh(true);
				}
			}
		};
		printAUF = new Action(Messages.AUF2_print) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText(Messages.AUF2_createPrint); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				IHandlerService handlerService =
					(IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(AufPrintHandler.CMD_ID, null);
				} catch (Exception e) {
					LoggerFactory.getLogger(BriefAuswahl.class).error("cannot execute cmd", e);
				}
			}
		};
	}
	
	private ch.elexis.data.AUF getSelectedAUF(){
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if ((sel == null) || (sel.isEmpty())) {
			return null;
		}
		return (AUF) sel.getFirstElement();
	}
	
	class AUFContentProvider implements IStructuredContentProvider {
		
		@Override
		public Object[] getElements(Object inputElement){
			Patient pat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
			if (pat == null) {
				return new Object[0];
			}
			Query<AUF> qbe = new Query<AUF>(AUF.class);
			qbe.add(AUF.FLD_PATIENT_ID, Query.EQUALS, pat.getId());
			qbe.orderBy(true, AUF.FLD_DATE_FROM, AUF.FLD_DATE_UNTIL);
			List<AUF> list = qbe.execute();
			return list.toArray();
		}
		
		@Override
		public void dispose(){ /* leer */
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			/* leer */
		}
		
	}
	
	@Override
	public void activation(boolean mode){ /* egal */
	}
	
	@Override
	public void visible(boolean mode){
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eli_auf, eli_pat);
			eli_pat
				.catchElexisEvent(new ElexisEvent(ElexisEventDispatcher.getSelected(Patient.class),
					null, ElexisEvent.EVENT_SELECTED));
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eli_auf, eli_pat);
		}
	}
}
