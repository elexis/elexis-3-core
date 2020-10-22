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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IFreeTextDiagnosis;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.dialogs.FreeTextDiagnoseDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.views.codesystems.DiagnosenView;
import ch.elexis.data.FreeTextDiagnose;
import ch.elexis.data.Konsultation;
import ch.rgw.tools.StringTool;

public class DiagnosenDisplay extends Composite implements IUnlockable {
	private Table table;
	private TableViewer viewer;
	
	private final GenericObjectDropTarget dropTarget;

	@Optional
	@Inject
	public void udpateEncounter(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IEncounter encounter){
		if (encounter != null && encounter.equals(actEncounter)) {
			viewer.setInput(actEncounter.getDiagnoses());
		}
	}
	
	private IEncounter actEncounter;
	private ToolBar toolBar;
	private TableColumnLayout tableLayout;
	
	@Override
	public void setEnabled(boolean enabled) {
		toolBar.setEnabled(enabled);
		super.setEnabled(enabled);
	};

	public DiagnosenDisplay(final IWorkbenchPage page, final Composite parent, final int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		Label label = new Label(this, SWT.NONE);
		FontDescriptor boldDescriptor =
			FontDescriptor.createFrom(label.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(label.getDisplay());
		label.setFont(boldFont);
		label.setText(Messages.DiagnosenDisplay_Diagnoses);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		ToolBarManager toolBarManager = new ToolBarManager(SWT.RIGHT);
		IAction newAction = new Action() {
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
		};
		newAction.setToolTipText(Messages.DiagnosenDisplay_AddDiagnosis);
		toolBarManager.add(newAction);

		IAction textAction = new Action() {
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_DOCUMENT_TEXT.getImageDescriptor();
			}
			
			@Override
			public void run(){
				FreeTextDiagnoseDialog ftDialog =
					new FreeTextDiagnoseDialog(Display.getDefault().getActiveShell());
				if (ftDialog.open() == Window.OK) {
					String freeText = ftDialog.getText();
					if (StringUtils.isNotBlank(freeText)) {
						IFreeTextDiagnosis diagnosis =
							CoreModelServiceHolder.get().create(IFreeTextDiagnosis.class);
						diagnosis.setText(freeText);
						CoreModelServiceHolder.get().save(diagnosis);
						actEncounter.addDiagnosis(diagnosis);
						CoreModelServiceHolder.get().save(actEncounter);
						viewer.setInput(actEncounter.getDiagnoses());
					}
				}
			}
		};
		toolBarManager.add(textAction);
		textAction.setToolTipText(Messages.DiagnosenDisplay_AddTextDiagnosis);

		toolBar = toolBarManager.createControl(this);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		tableLayout = new TableColumnLayout();
		Composite tableComposite = new Composite(this, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableComposite.setLayout(tableLayout);
		viewer = new TableViewer(tableComposite,
			SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table = viewer.getTable();
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setMenu(createDgMenu());
		createColumns();
		
		// connect double click on column to actions
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e){
				int clickedIndex = -1;
				// calculate column of click
				int width = 0;
				TableColumn[] columns = table.getColumns();
				for (int i = 0; i < columns.length; i++) {
					TableColumn tc = columns[i];
					if (width < e.x && e.x < width + tc.getWidth()) {
						clickedIndex = i;
						break;
					}
					width += tc.getWidth();
				}
				if (clickedIndex != -1) {
					if (clickedIndex == 3) {
						IStructuredSelection selection = viewer.getStructuredSelection();
						if (!selection.isEmpty()) {
							if (selection.getFirstElement() instanceof IDiagnosis) {
								actEncounter
									.removeDiagnosis((IDiagnosis) selection.getFirstElement());
								CoreModelServiceHolder.get().save(actEncounter);
							}
							setEncounter(actEncounter);
						}
					}
				}
			}
		});
		
		dropTarget =
			new GenericObjectDropTarget(Messages.DiagnosenDisplay_DiagnoseTarget, table,
				new DropReceiver()) {
				@Override
				protected Control getHighLightControl(){
					return DiagnosenDisplay.this;
				}
			};
		
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e){
				int width = DiagnosenDisplay.this.getBounds().width;
				int labelWidth = label.getBounds().width;
				int toolBarWidth = toolBar.getBounds().width;
				if (width < labelWidth + toolBarWidth) {
					if (label.getVisible()) {
						GridData labeldata = (GridData) label.getLayoutData();
						labeldata.exclude = true;
						label.setVisible(false);
						GridData toolData = (GridData) toolBar.getLayoutData();
						toolData.grabExcessHorizontalSpace = true;
					}
				} else {
					if (!label.getVisible()) {
						GridData labeldata = (GridData) label.getLayoutData();
						labeldata.exclude = false;
						label.setVisible(true);
						GridData toolData = (GridData) toolBar.getLayoutData();
						toolData.grabExcessHorizontalSpace = false;
					}
					}
				}
		});
	}

	public void clear(){
		actEncounter = null;
		viewer.setInput(Collections.emptyList());
	}

	private final class DropReceiver implements GenericObjectDropTarget.IReceiver {
		@Override
		public void dropped(List<Object> list, DropTargetEvent e){
			if (accept(list)) {
				for (Object object : list) {
					if (object instanceof IDiagnose) {
						Konsultation actKons = Konsultation.load(actEncounter.getId());
						actKons.addDiagnose((IDiagnose) object);
					} else if (object instanceof IDiagnosis) {
						IDiagnosis diagnosis = (IDiagnosis) object;
						actEncounter.addDiagnosis(diagnosis);
						CoreModelServiceHolder.get().save(actEncounter);
					}
				}
				viewer.setInput(actEncounter.getDiagnoses());
			}
		}

		@Override
		public boolean accept(List<Object> list){
			if (actEncounter == null) {
				return false;
			}
			for (Object object : list) {
				if (!(object instanceof IDiagnose || object instanceof IDiagnosis)) {
					return false;
				}
			}
			return true;
		}
	}

	public void setEncounter(IEncounter encounter){
		actEncounter = encounter;
		table.getColumn(0).setWidth(0);
		viewer.setInput(encounter.getDiagnoses());
	}
	
	private void createColumns(){
		String[] titles = {
			"", Messages.Display_Column_Code, Messages.Display_Column_Designation, StringTool.leer
		};
		int[] weights = {
			0, 15, 70, 15
		};
		
		TableViewerColumn col = createTableViewerColumn(titles[0], weights[0], 0, SWT.LEFT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				return StringTool.leer;
			}
		});
		
		col = createTableViewerColumn(titles[1], weights[1], 1, SWT.LEFT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IDiagnose) {
					IDiagnose diagnosis = (IDiagnose) element;
					if (!(diagnosis instanceof FreeTextDiagnose)) {
						return diagnosis.getCode();
					}
				} else if (element instanceof IDiagnosis) {
					IDiagnosis diagnosis = (IDiagnosis) element;
					if (diagnosis instanceof IDiagnosisReference) {
						IDiagnosisReference diagnosisRef = (IDiagnosisReference) element;
						if (diagnosisRef.getReferredClass().toLowerCase().contains("freetext")) {
							return "";
						}
					}
					return diagnosis.getCode();
				}
				return StringTool.leer;
			}
		});
		
		col = createTableViewerColumn(titles[2], weights[2], 2, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IDiagnose) {
					IDiagnose diagnosis = (IDiagnose) element;
					return diagnosis.getText();
				} else if (element instanceof IDiagnosis) {
					IDiagnosis diagnosis = (IDiagnosis) element;
					return diagnosis.getText();
				}
				return StringTool.leer;
			}
		});
		
		col = createTableViewerColumn(titles[3], weights[3], 3, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				return StringTool.leer;
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
	
	private Menu createDgMenu(){
		MenuManager contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (!selection.isEmpty()) {
					manager.add(new Action() {
						
						@Override
						public ImageDescriptor getImageDescriptor(){
							return Images.IMG_DELETE.getImageDescriptor();
						};
						
						@Override
						public String getText(){
							return Messages.DiagnosenDisplay_RemoveDiagnoses;
						};
						
						@Override
						public void run(){
							for (Object object : selection.toList()) {
								if (object instanceof IDiagnosis) {
									actEncounter.removeDiagnosis((IDiagnosis) object);
									CoreModelServiceHolder.get().save(actEncounter);
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
	public void setUnlocked(boolean unlocked) {
		setEnabled(unlocked);
		redraw();
	}
}
