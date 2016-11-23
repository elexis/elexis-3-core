/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.findings.ui.composites;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.ui.dialogs.ConditionEditDialog;
import ch.elexis.core.findings.ui.services.CodingServiceComponent;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.NatTableFactory;
import ch.elexis.core.ui.util.NatTableWrapper;
import ch.elexis.core.ui.util.NatTableWrapper.IDoubleClickListener;
import ch.elexis.data.Patient;

/**
 * {@link Composite} implementation for managing the {@link ICondition} entries, of a
 * {@link Patient}.
 * 
 * @author thomas
 *
 */
public class DiagnoseListComposite extends Composite {
	private NatTableWrapper natTableWrapper;
	private ToolBarManager toolbarManager;
	
	private EventList<ICondition> dataList = new BasicEventList<>();
	
	public DiagnoseListComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		toolbarManager = new ToolBarManager();
		toolbarManager.add(new AddConditionAction());
		toolbarManager.add(new RemoveConditionAction());
		ToolBar toolbar = toolbarManager.createControl(this);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		toolbar.setBackground(parent.getBackground());
		
		natTableWrapper = NatTableFactory.createSingleColumnTable(this,
			new GlazedListsDataProvider<ICondition>(dataList, new IColumnAccessor<ICondition>() {
				@Override
				public int getColumnCount(){
					return 1;
				}
				
				@Override
				public Object getDataValue(ICondition condition, int columnIndex){
					switch (columnIndex) {
					case 0:
						return getFormattedDescriptionText(condition);
					}
					return "";
				}
				
				private Object getFormattedDescriptionText(ICondition condition){
					StringBuilder text = new StringBuilder();
					
					StringBuilder contentText = new StringBuilder();
					// first display text
					Optional<String> conditionText = condition.getText();
					conditionText.ifPresent(t -> {
						if (contentText.length() > 0) {
							contentText.append("\n");
						}
						contentText.append(t);
					});
					// then display the coding
					List<ICoding> codings = condition.getCoding();
					if (codings != null && !codings.isEmpty()) {
						for (ICoding iCoding : codings) {
							if (contentText.length() > 0) {
								contentText.append(", ");
							}
							contentText.append("[")
								.append(CodingServiceComponent.getService().getShortLabel(iCoding))
								.append(iCoding.getCode()).append("] ")
								.append(iCoding.getDisplay() != null ? iCoding.getDisplay() : "");
						}
					}
					// add additional information before content
					text.append("<strong>");
					ConditionStatus status = condition.getStatus();
					text.append(status.getLocalized());
					Optional<String> start = condition.getStart();
					start.ifPresent(string -> text.append(" (").append(string).append(" - "));
					Optional<String> end = condition.getEnd();
					end.ifPresent(string -> text.append(string));
					start.ifPresent(string -> text.append(")"));
					
					List<String> notes = condition.getNotes();
					if(!notes.isEmpty()) {
						text.append(" (" + notes.size() + ")");
					}
					if (contentText.toString().contains("\n")) {
						text.append("</strong>\n").append(contentText.toString());
					} else {
						text.append("</strong> ").append(contentText.toString());
					}
					

					return text.toString();
				}
				
				@Override
				public void setDataValue(ICondition condition, int arg1, Object arg2){
					// setting data values is not enabled here.
				}
			
			}), null);
		natTableWrapper.getNatTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		natTableWrapper.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(NatTableWrapper source, ISelection selection){
				if(selection instanceof StructuredSelection && !selection.isEmpty()) {
					ConditionEditDialog dialog =
						new ConditionEditDialog(
							(ICondition) ((StructuredSelection) selection).getFirstElement(),
							getShell());
					if (dialog.open() == Dialog.OK) {
						dialog.getCondition().ifPresent(c -> {
							source.getNatTable().refresh();
						});
					}
				}
			}
		});
		
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new ConditionsMenuListener());
		natTableWrapper.getNatTable().setMenu(mgr.createContextMenu(natTableWrapper.getNatTable()));
	}
	
	public void setInput(List<ICondition> conditions){
		dataList.clear();
		conditions.sort(new Comparator<ICondition>() {
			@Override
			public int compare(ICondition left, ICondition right){
				Optional<LocalDate> lrecorded = left.getDateRecorded();
				Optional<LocalDate> rrecorded = right.getDateRecorded();
				if(lrecorded.isPresent() && rrecorded.isPresent()) {
					return rrecorded.get().compareTo(lrecorded.get());
				} else {
					Optional<String> lstart = left.getStart();
					Optional<String> rstart = right.getStart();
					if (lstart.isPresent() && rstart.isPresent()) {
						return rstart.get().compareTo(lstart.get());
					}
				}
				return 0;
			}
		});
		dataList.addAll(conditions);
		natTableWrapper.getNatTable().refresh();
	}
	
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed){
		Point ret = toolbarManager.getControl().computeSize(wHint, hHint);
		Point natRet = natTableWrapper.computeSize(wHint, hHint);
		ret.y += natRet.y;
		ret.x = natRet.x;
		return ret;
	}
	
	@Override
	public void setBackground(Color color){
		super.setBackground(color);
		if (natTableWrapper != null && !natTableWrapper.isDisposed()) {
			natTableWrapper.getNatTable().setBackground(color);
		}
	}
	
	private class ConditionsMenuListener implements IMenuListener {
		
		@Override
		public void menuAboutToShow(IMenuManager manager){
			ISelection currentSelection = natTableWrapper.getSelection();
			if (currentSelection instanceof StructuredSelection) {
				StructuredSelection sSelection = (StructuredSelection) currentSelection;
				if (sSelection.size() == 1) {
					ICondition selectedCondition = (ICondition) sSelection.getFirstElement();
					ConditionStatus selectionStatus = selectedCondition.getStatus();
					if (selectionStatus != ConditionStatus.ACTIVE) {
						manager
							.add(new ToggleStatusAction(selectedCondition, ConditionStatus.ACTIVE));
					}
					if (selectionStatus != ConditionStatus.RESOLVED) {
						manager.add(
							new ToggleStatusAction(selectedCondition, ConditionStatus.RESOLVED));
					}
					if (selectionStatus != ConditionStatus.RELAPSE) {
						manager.add(
							new ToggleStatusAction(selectedCondition, ConditionStatus.RELAPSE));
					}
					if (selectionStatus != ConditionStatus.REMISSION) {
						manager.add(
							new ToggleStatusAction(selectedCondition, ConditionStatus.REMISSION));
					}
				}
				if (!sSelection.isEmpty()) {
					manager.add(new RemoveConditionAction());
				}
			}
		}
	}
	
	private class ToggleStatusAction extends Action {
		
		private ConditionStatus status;
		private ICondition condition;
		
		public ToggleStatusAction(ICondition condition, ConditionStatus status){
			this.status = status;
			this.condition = condition;
		}
		
		@Override
		public String getText(){
			return "Status " + status.getLocalized();
		}
		
		@Override
		public void run(){
			condition.setStatus(status);
			natTableWrapper.getNatTable().refresh();
		}
	}
	
	private class AddConditionAction extends Action {
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_NEW.getImageDescriptor();
		}
		
		@Override
		public String getText(){
			return "erstellen";
		}
		
		@Override
		public void run(){
			Patient selectedPatient = ElexisEventDispatcher.getSelectedPatient();
			if (selectedPatient != null) {
				ConditionEditDialog dialog =
					new ConditionEditDialog(ConditionCategory.DIAGNOSIS, getShell());
				if (dialog.open() == Dialog.OK) {
					dialog.getCondition().ifPresent(c -> {
						c.setPatientId(selectedPatient.getId());
						dataList.add(c);
						natTableWrapper.getNatTable().refresh();
					});
				}
			}
		}
	}
	
	private class RemoveConditionAction extends Action {
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_DELETE.getImageDescriptor();
		}
		
		@Override
		public String getText(){
			return "entfernen";
		}
		
		@Override
		public void run(){
			ISelection selection = natTableWrapper.getSelection();
			if (selection instanceof StructuredSelection && !selection.isEmpty()) {
				@SuppressWarnings("unchecked")
				List<ICondition> list = ((StructuredSelection) selection).toList();
				list.stream().forEach(c -> {
					FindingsServiceComponent.getService().deleteFinding(c);
					dataList.remove(c);
					natTableWrapper.getNatTable().refresh();
				});
			}
		}
	}
}
