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
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.dialogs.FreeTextDiagnoseDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.util.PersistentObjectDragSource;
import ch.elexis.core.ui.util.PersistentObjectDragSource.ISelectionRenderer;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.DiagnosenView;
import ch.elexis.data.FreeTextDiagnose;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;

public class DiagnosenDisplay extends Composite implements ISelectionRenderer, IUnlockable {
	private Table table;
	private TableViewer viewer;
	
	private final PersistentObjectDropTarget dropTarget;

	private final ElexisEventListener eeli_update = new ElexisUiEventListenerImpl(
		Konsultation.class, ElexisEvent.EVENT_UPDATE) {
		@Override
		public void runInUi(ElexisEvent ev){
			PersistentObject obj = ev.getObject();
			if (obj != null && obj.equals(actEncounter)) {
				viewer.setInput(actEncounter.getDiagnosen());
			}
		}
	};
	
	private Konsultation actEncounter;
	private ToolBar toolBar;
	private TableViewerFocusCellManager focusCellManager;
	
	public void setEnabled(boolean enabled) {
		toolBar.setEnabled(enabled);
		super.setEnabled(enabled);
	};

	public DiagnosenDisplay(final IWorkbenchPage page, final Composite parent, final int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		ToolBarManager toolBarManager = new ToolBarManager(SWT.RIGHT);
		toolBarManager.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_NEW.getImageDescriptor();
			}
			
			@Override
			public void run(){
				try {
					page.showView(DiagnosenView.ID);
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
				} catch (Exception ex) {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							Messages.DiagnosenDisplay_ErrorStartingCodeSystem + ex.getMessage(),
							ex, ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		toolBarManager.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_DOCUMENT_TEXT.getImageDescriptor();
			}
			
			@Override
			public void run(){
				FreeTextDiagnoseDialog ftDialog =
					new FreeTextDiagnoseDialog(Display.getDefault().getActiveShell());
				if (ftDialog.open() == Window.OK) {
					FreeTextDiagnose diag = new FreeTextDiagnose(ftDialog.getText(), true);
					actEncounter.addDiagnose(diag);
					viewer.setInput(actEncounter.getDiagnosen());
				}
			}
		});
		toolBar = toolBarManager.createControl(this);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		
		viewer = new TableViewer(this,
			SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table = viewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		// dummy table viewer needed for SelectionsProvider for Menu
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setMenu(createDgMenu());
		createColumns();
		
		// connect double click on column to actions
		focusCellManager =
			new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				ViewerCell focusCell = focusCellManager.getFocusCell();
				int columnIndex = focusCell.getColumnIndex();
				if (columnIndex == 2) {
					IStructuredSelection selection = viewer.getStructuredSelection();
					if (!selection.isEmpty()) {
						if (selection.getFirstElement() instanceof IDiagnose) {
							actEncounter.removeDiagnose((IDiagnose) selection.getFirstElement());
						}
						setEncounter(actEncounter);
					}
				}
			}
		});
		
		// new PersistentObjectDragSource()
		dropTarget =
			new PersistentObjectDropTarget(Messages.DiagnosenDisplay_DiagnoseTarget, table,
				new DropReceiver()); //$NON-NLS-1$
		new PersistentObjectDragSource(table, this);

		ElexisEventDispatcher.getInstance().addListeners(eeli_update);
	}

	public void clear(){
		actEncounter = null;
		viewer.setInput(Collections.emptyList());
	}

	private final class DropReceiver implements PersistentObjectDropTarget.IReceiver {
		@Override
		public void dropped(final PersistentObject o, final DropTargetEvent ev){
			if (actEncounter == null) {
				SWTHelper.alert("Keine Konsultation ausgewählt",
					"Bitte wählen Sie zuerst eine Konsultation aus");
			} else {
				if (o instanceof IDiagnose) {
					actEncounter.addDiagnose((IDiagnose) o);
					setEncounter(actEncounter);
				}
			}
		}

		@Override
		public boolean accept(final PersistentObject o){
			if (o instanceof IVerrechenbar) {
				return false;
			}
			if (o instanceof IDiagnose) {
				return true;
			}
			return false;
		}
	}

	public void setEncounter(Konsultation encounter){
		actEncounter = encounter;
		viewer.setInput(encounter.getDiagnosen());
	}
	
	private void createColumns(){
		String[] titles = {
			"Code", "Bezeichnung", ""
		};
		int[] bounds = {
			45, 250, 45
		};
		
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IDiagnose) {
					IDiagnose diagnosis = (IDiagnose) element;
					if (!(diagnosis instanceof FreeTextDiagnose)) {
						return diagnosis.getCode();
					}
				}
				return "";
			}
		});
		
		col = createTableViewerColumn(titles[1], bounds[1], 1, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IDiagnose) {
					IDiagnose diagnosis = (IDiagnose) element;
					return diagnosis.getText();
				}
				return "";
			}
		});
		
		col = createTableViewerColumn(titles[2], bounds[2], 2, SWT.NONE);
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
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, int colNumber,
		int style){
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, style);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		return viewerColumn;
	}
	
	private Menu createDgMenu(){
		MenuManager contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (!selection.isEmpty()) {
					manager.add(new Action() {
						
						public ImageDescriptor getImageDescriptor(){
							return Images.IMG_DELETE.getImageDescriptor();
						};
						
						public String getText(){
							return Messages.DiagnosenDisplay_RemoveDiagnoses;
						};
						
						public void run(){
							for (Object object : selection.toList()) {
								if (object instanceof IDiagnose) {
									actEncounter.removeDiagnose((IDiagnose) object);
								}
							}
							setEncounter(actEncounter);
						};
					});
				}
			}
		});
		return contextMenuManager.createContextMenu(table);
	}
	
	@Override
	public List<PersistentObject> getSelection(){
		List<PersistentObject> ret = new ArrayList<>();
		IStructuredSelection selection = viewer.getStructuredSelection();
		if (!selection.isEmpty()) {
			for (Object object : selection.toList()) {
				if (object instanceof IDiagnose) {
					String clazz = object.getClass().getName();
					ret.add(CoreHub.poFactory
						.createFromString(clazz + "::" + ((IDiagnose) object).getCode())); //$NON-NLS-1$
				}
			}
		}
		return ret;
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		setEnabled(unlocked);
		redraw();
	}
}
