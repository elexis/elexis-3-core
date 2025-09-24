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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.commands.AufByFallFilter;
import ch.elexis.core.ui.commands.AufNewHandler;
import ch.elexis.core.ui.commands.AufPrintHandler;
import ch.elexis.core.ui.commands.AufPrintListHandler;
import ch.elexis.core.ui.dialogs.EditAUFDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.views.provider.Auf2LabelProvider;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Arbeitsunfähigkeitszeugnisse erstellen und verwalten.
 *
 * @author gerry
 *
 */
public class AUF2 extends ViewPart implements IRefreshable {
	public static final String ID = "ch.elexis.auf"; //$NON-NLS-1$
	TableViewer tv;
	private Action newAUF, filterAUF, printList, delAUF, modAUF, printAUF;
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);
	private AufByFallFilter aufFilter = new AufByFallFilter();
	private boolean isFilterActive = false;
	private String currentFallID = null;
	private List<ISickCertificate> selectedCertificates = new ArrayList<>();

	@Inject
	void activeCertificate(@Optional ISickCertificate certificate) {
		CoreUiUtil.runAsyncIfActive(() -> {
			boolean bSelect = certificate != null;
			modAUF.setEnabled(bSelect);
			delAUF.setEnabled(bSelect);

			if (bSelect && tv != null) {
				// refresh & select only if not already selected
				if (!Objects.equals(tv.getStructuredSelection(), new StructuredSelection(certificate))) {
					tv.refresh(false);
					tv.setSelection(new StructuredSelection(certificate));
				}
			}
		}, tv);
	}

	@Inject
	void activePatient(@Optional IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {
			if (patient != null) {
				tv.refresh();
				ContextServiceHolder.get().getRootContext().removeTyped(ISickCertificate.class);
				newAUF.setEnabled(true);
			} else {
				newAUF.setEnabled(false);
				modAUF.setEnabled(false);
				delAUF.setEnabled(false);
			}
		}, tv);
	}

	@Optional
	@Inject
	void activeCoverage(ICoverage iCoverage) {
		String fallId = iCoverage.getId().toString();
		if (isFilterActive) {
			aufFilter.setFallID(fallId);
			tv.refresh();
		}
		currentFallID = fallId;
	}

	public AUF2() {
		setTitleImage(Images.IMG_VIEW_WORK_INCAPABLE.getImage());
	}

	@Override
	public void createPartControl(Composite parent) {
		// setTitleImage(Desk.getImage(ICON));
		setPartName(Messages.AUF2_certificate); // $NON-NLS-1$
		tv = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tv.setLabelProvider(new DefaultLabelProvider());
		tv.setContentProvider(new AUFContentProvider());
		tv.setLabelProvider(new Auf2LabelProvider());
		makeActions();
		ViewMenus menus = new ViewMenus(getViewSite());
		menus.createMenu(newAUF, filterAUF, printList, delAUF, modAUF, printAUF);
		menus.createToolbar(newAUF, filterAUF, printList, delAUF, printAUF);
		tv.setUseHashlookup(true);
		tv.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getStructuredSelection().getFirstElement() instanceof ISickCertificate) {
					ContextServiceHolder.get().getRootContext()
							.setTyped(
									event.getStructuredSelection().getFirstElement());
				}
			}
		});
		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				modAUF.run();
			}
		});
		tv.setInput(getViewSite());

		final Transfer[] dragTransferTypes = new Transfer[] { TextTransfer.getInstance() };

		tv.addDragSupport(DND.DROP_COPY, dragTransferTypes, new DragSourceAdapter() {

			@Override
			public void dragSetData(DragSourceEvent event) {
				IStructuredSelection selection = (IStructuredSelection) tv.getSelection();
				StringBuilder sb = new StringBuilder();
				if (selection != null && !selection.isEmpty()) {
					ISickCertificate auf = (ISickCertificate) selection.getFirstElement();
					sb.append(StoreToStringServiceHolder.getStoreToString(auf)).append(","); //$NON-NLS-1$
				}
				event.data = sb.toString().replace(",$", StringUtils.EMPTY); //$NON-NLS-1$
			}
		});
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selectedCertificates.clear();
				IStructuredSelection selection = (IStructuredSelection) tv.getSelection();
				for (Object obj : selection.toArray()) {
					if (obj instanceof ISickCertificate) {
						selectedCertificates.add((ISickCertificate) obj);
					}
				}
			}
		});
		tv.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (tv.getTable().getItem(new Point(e.x, e.y)) == null) {
					tv.setSelection(StructuredSelection.EMPTY);
					selectedCertificates.clear();
				}
			}
		});
		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void makeActions() {
		newAUF = new Action(Messages.Core_New_ellipsis) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.AUF2_createNewCert); // $NON-NLS-1$
			}

			@Override
			public void run() {
				IHandlerService handlerService = PlatformUI.getWorkbench()
						.getService(IHandlerService.class);
				try {
					Object createdAuf = handlerService.executeCommand(AufNewHandler.CMD_ID, null);
					if (createdAuf instanceof ISickCertificate) {
						ContextServiceHolder.get().getRootContext().setTyped(createdAuf);
					}
					refresh();
				} catch (Exception e) {
					LoggerFactory.getLogger(BriefAuswahl.class).error("cannot execute cmd", e); //$NON-NLS-1$
				}
			}
		};
		filterAUF = new Action(Messages.Feature_filter, Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
				setToolTipText(Messages.Feature_filter);
			}

		    @Override
		    public void run() {
		        aufFilter.setFilterActive(!isFilterActive);
		        if (isFilterActive) {
		            aufFilter.resetFallID();
		            tv.removeFilter(aufFilter);
		        } else {
		            if (currentFallID != null) {
		                aufFilter.setFallID(currentFallID);
		            }
		            tv.addFilter(aufFilter);
		        }
		        isFilterActive = !isFilterActive;
		        tv.refresh();
		    }
		};
		printList = new Action(Messages.Core_Print_List) {
		    {
		        setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText(Messages.AUF2_LIST_TOOLBAR);
		    }

		    @Override
		    public void run() {
				try {
					IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
					handlerService.executeCommand(AufPrintListHandler.CMD_ID, null);
				} catch (Exception e) {
					LoggerFactory.getLogger(BriefAuswahl.class).error("cannot execute cmd", e);
		        }
		    }
		};
		delAUF = new Action(Messages.Core_Delete_ellipsis) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.AUF2_deleteCertificate); // $NON-NLS-1$
			}

			@Override
			public void run() {
				ISickCertificate sel = getSelectedCertificate();
				if (sel != null) {
					if (MessageDialog.openConfirm(getViewSite().getShell(), Messages.AUF2_deleteReally,
							Messages.AUF2_doyoywantdeletereally)) { // $NON-NLS-1$ //$NON-NLS-2$
						CoreModelServiceHolder.get().delete(sel);
						tv.refresh(false);
					}
				}
			}
		};
		modAUF = new Action(Messages.Core_DoChange_ellipsis) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.AUF2_editCertificate); // $NON-NLS-1$
			}

			@Override
			public void run() {
				ISickCertificate sel = getSelectedCertificate();
				if (sel != null) {
					new EditAUFDialog(getViewSite().getShell(), sel, sel.getCoverage()).open();
					tv.refresh(true);
				}
			}
		};
		printAUF = new Action(Messages.Core_Print_ellipsis) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText(Messages.AUF2_createPrint); // $NON-NLS-1$
			}

			@Override
			public void run() {
				IHandlerService handlerService = PlatformUI.getWorkbench()
						.getService(IHandlerService.class);
				try {
					handlerService.executeCommand(AufPrintHandler.CMD_ID, null);
				} catch (Exception e) {
					LoggerFactory.getLogger(BriefAuswahl.class).error("cannot execute cmd", e); //$NON-NLS-1$
				}
			}
		};
	}

	private ISickCertificate getSelectedCertificate() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if ((sel == null) || (sel.isEmpty())) {
			return null;
		}
		return (ISickCertificate) sel.getFirstElement();
	}

	public class AUFContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			// Patient pat = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
			java.util.Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();

			if (patient.isPresent()) {
				INamedQuery<ISickCertificate> query = CoreModelServiceHolder.get().getNamedQuery(ISickCertificate.class,
						"patient"); //$NON-NLS-1$
				List<ISickCertificate> list = query
						.executeWithParameters(query.getParameterMap("patient", patient.get())); //$NON-NLS-1$
				return list.toArray();
			}
			return new Object[0];
		}

		@Override
		public void dispose() { /* leer */
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			/* leer */
		}

	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	@Override
	public void refresh() {
		Display.getDefault().asyncExec(() -> {
			tv.refresh();
		});
	}

	public TableViewer getViewer() {
		return tv;
	}

	public boolean isFilterActive() {
		return isFilterActive;
	}

	public AufByFallFilter getFilter() {
		return aufFilter;
	}

	public List<ISickCertificate> getSelectedCertificates() {
		return selectedCertificates;
	}
}
