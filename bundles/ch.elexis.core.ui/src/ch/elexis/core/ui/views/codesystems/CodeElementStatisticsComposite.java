package ch.elexis.core.ui.views.codesystems;

import java.util.Collections;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.data.service.CodeElementServiceHolder;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.core.ui.util.GenericObjectDragSource;
import ch.elexis.data.PersistentObject;

public class CodeElementStatisticsComposite extends Composite {
	
	private Label title;
	private TableViewer viewer;
	private String elexisClassName;
	private IContact contact;
	

	public CodeElementStatisticsComposite(String elexisClassName, Composite parent, int style){
		super(parent, style);
		this.elexisClassName = elexisClassName;
		createContent();
	}
	
	private void createContent(){
		setLayout(new GridLayout(1, false));
		
		title = new Label(this, SWT.NONE);
		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		viewer = new TableViewer(this,
			SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof PersistentObject) {
					return ((PersistentObject) element).getLabel();
				} else if (element instanceof Identifiable) {
					return ((Identifiable) element).getLabel();
				}
				return super.getText(element);
			}
		});
		
		MenuManager menu = new MenuManager();
		menu.setRemoveAllWhenShown(true);
		menu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				manager.add(new ClearStatisticAction());
			}
		});
		viewer.getControl().setMenu(menu.createContextMenu(viewer.getControl()));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (selection != null && !selection.isEmpty()) {
					ICodeSelectorTarget target =
						CodeSelectorHandler.getInstance().getCodeSelectorTarget();
					if (target != null) {
						for (Object selected : selection.toList()) {
							target.codeSelected(selected);
						}
					}
				}
			}
		});
		new GenericObjectDragSource(viewer);
	}
	
	public void setTitle(String title){
		this.title.setText(title);
		layout(true, true);
	}
	
	public void setContact(IContact contact){
		this.contact = contact;
		if (contact != null) {
			viewer.setInput(CodeElementServiceHolder.getStatistics(elexisClassName, contact));
		} else {
			viewer.setInput(Collections.emptyList());
		}
	}
	
	private class ClearStatisticAction extends Action {
		
		@Override
		public String getText(){
			return Messages.CodeSelectorFactory_resetStatistic;
		}
		
		@Override
		public void run(){
			CodeElementServiceHolder.clearStatistics(elexisClassName, contact);
			viewer.setInput(CodeElementServiceHolder.getStatistics(elexisClassName, contact));
		}
	}
}
