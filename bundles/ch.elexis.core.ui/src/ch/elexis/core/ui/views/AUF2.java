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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.commands.AufNewHandler;
import ch.elexis.core.ui.commands.AufPrintHandler;
import ch.elexis.core.ui.dialogs.EditAUFDialog;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;

/**
 * Arbeitsunfähigkeitszeugnisse erstellen und verwalten.
 * 
 * @author gerry
 * 
 */
public class AUF2 extends ViewPart implements IRefreshable {
	public static final String ID = "ch.elexis.auf"; //$NON-NLS-1$
	TableViewer tv;
	private Action newAUF, delAUF, modAUF, printAUF;
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);
	
	//	private ElexisEventListener eli_auf = new ElexisUiEventListenerImpl(AUF.class) {
	//		
	//		@Override
	//		public void runInUi(ElexisEvent ev){
	//			boolean bSelect = (ev.getType() == ElexisEvent.EVENT_SELECTED);
	//			modAUF.setEnabled(bSelect);
	//			delAUF.setEnabled(bSelect);
	//			
	//			if (bSelect && tv != null) {
	//				// refresh & select only if not already selected
	//				if (ev.getObject() instanceof AUF && !Objects.equals(tv.getStructuredSelection(),
	//					new StructuredSelection(ev.getObject()))) {
	//					tv.refresh(false);
	//					tv.setSelection(new StructuredSelection(ev.getObject()));
	//				}
	//			}
	//		}
	//	};
	
	@Inject
	void activeCertificate(@Optional
	ISickCertificate certificate){
		Display.getDefault().asyncExec(() -> {
			boolean bSelect = certificate != null;
			modAUF.setEnabled(bSelect);
			delAUF.setEnabled(bSelect);
			
			if (bSelect && tv != null) {
				// refresh & select only if not already selected
				if (!Objects.equals(tv.getStructuredSelection(),
					new StructuredSelection(certificate))) {
					tv.refresh(false);
					tv.setSelection(new StructuredSelection(certificate));
				}
			}
		});
	}
	
	//	private ElexisEventListener eli_pat = new ElexisUiEventListenerImpl(Patient.class) {
	//		
	//		@Override
	//		public void runInUi(ElexisEvent ev){
	//			if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
	//				tv.refresh();
	//				ElexisEventDispatcher.clearSelection(AUF.class);
	//				newAUF.setEnabled(true);
	//			} else {
	//				newAUF.setEnabled(false);
	//				modAUF.setEnabled(false);
	//				delAUF.setEnabled(false);
	//				
	//			}
	//		}
	//	};
	
	@Inject
	void activePatient(@Optional
	IPatient patient){
		Display.getDefault().asyncExec(() -> {
			if (patient != null) {
				tv.refresh();
				ContextServiceHolder.get().getRootContext().removeTyped(ISickCertificate.class);
				newAUF.setEnabled(true);
			} else {
				newAUF.setEnabled(false);
				modAUF.setEnabled(false);
				delAUF.setEnabled(false);
				
			}
		});
	}
	
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
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getStructuredSelection().getFirstElement() instanceof ISickCertificate) {
					ContextServiceHolder.get().getRootContext()
						.setTyped(event.getStructuredSelection().getFirstElement());
				}
			}
		});
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
					ISickCertificate auf = (ISickCertificate) selection.getFirstElement();
					sb.append(StoreToStringServiceHolder.getStoreToString(auf)).append(","); //$NON-NLS-1$
				}
				event.data = sb.toString().replace(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		
		getSite().getPage().addPartListener(udpateOnVisible);
	}
	
	@Override
	public void dispose(){
		getSite().getPage().removePartListener(udpateOnVisible);
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
					if (createdAuf instanceof ISickCertificate) {
						ContextServiceHolder.get().getRootContext().setTyped(createdAuf);
					}
					refresh();
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
				ISickCertificate sel = getSelectedCertificate();
				if (sel != null) {
					if (MessageDialog.openConfirm(getViewSite().getShell(),
						Messages.AUF2_deleteReally, Messages.AUF2_doyoywantdeletereally)) { //$NON-NLS-1$ //$NON-NLS-2$
						CoreModelServiceHolder.get().delete(sel);
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
				ISickCertificate sel = getSelectedCertificate();
				if (sel != null) {
					new EditAUFDialog(getViewSite().getShell(), sel, sel.getCoverage()).open();
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
	
	private ISickCertificate getSelectedCertificate(){
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if ((sel == null) || (sel.isEmpty())) {
			return null;
		}
		return (ISickCertificate) sel.getFirstElement();
	}
	
	class AUFContentProvider implements IStructuredContentProvider {
		
		@Override
		public Object[] getElements(Object inputElement){
			// Patient pat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
			java.util.Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();
			
			if (patient.isPresent()) {
				INamedQuery<ISickCertificate> query =
					CoreModelServiceHolder.get().getNamedQuery(ISickCertificate.class, "patient");
				List<ISickCertificate> list =
					query.executeWithParameters(query.getParameterMap("patient", patient.get()));
				return list.toArray();
			}
			return new Object[0];
		}
		
		@Override
		public void dispose(){ /* leer */
		}
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			/* leer */
		}
		
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
	
	@Override
	public void refresh(){
		if (CoreUiUtil.isActiveControl(tv.getControl())) {
			tv.refresh();
		}
	}
}
