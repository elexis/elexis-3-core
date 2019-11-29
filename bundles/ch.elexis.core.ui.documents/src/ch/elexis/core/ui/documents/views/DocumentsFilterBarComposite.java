package ch.elexis.core.ui.documents.views;

import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.documents.FilterCategory;
import ch.elexis.core.ui.UiDesk;

public class DocumentsFilterBarComposite extends Composite implements ISelectionProvider {
	private DocumentTypeAction currentSelection;
	
	private ListenerList<ISelectionChangedListener> selectionChangedListeners;
	private List<FilterCategory> filters;
	private ToolBarManager manager;
	
	public DocumentsFilterBarComposite(Composite parent, int style, List<FilterCategory> filters){
		super(parent, style);
		currentSelection = null;
		selectionChangedListeners = new ListenerList<>();
		this.filters = filters;
		createContent();
	}
	
	private void createContent(){
		setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
		setLayout(new FillLayout());
		
		manager = new ToolBarManager(SWT.WRAP);
		manager.createControl(this);
		refresh();
	}
	
	public void refresh(){
		updateVisible();
		updateSelected();
	}
	
	private void updateVisible(){
		manager.removeAll();
		
		if (filters != null) {
			for (FilterCategory cf : filters) {
				manager.add(new DocumentTypeAction(cf));
			}
		}
		manager.update(true);
		this.getParent().layout(true);
	}
	
	private void updateSelected(){
		fireSelectionChanged();
		manager.update(true);
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		selectionChangedListeners.add(listener);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		selectionChangedListeners.remove(listener);
	}
	
	@Override
	public ISelection getSelection(){
		if (currentSelection != null && currentSelection.getFilter() != null) {
			return new StructuredSelection(currentSelection.getFilter());
		}
		// use all if none is selected
		return new StructuredSelection(filters.get(0));
	}
	
	@Override
	public void setSelection(ISelection selection){
		// ignore until needed
	}
	
	private void fireSelectionChanged(){
		ISelection selection = getSelection();
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
			listener.selectionChanged(event);
		}
	}
	
	private class DocumentTypeAction extends Action {
		private FilterCategory filter;
		
		public DocumentTypeAction(FilterCategory filter){
			super(filter.getLabel(), Action.AS_RADIO_BUTTON);
			this.filter = filter;
		}
		
		public FilterCategory getFilter(){
			return filter;
		}
		
		@Override
		public void run(){
			currentSelection = this;
			if (isChecked()) {
				fireSelectionChanged();
			}
			manager.update(true);
		}
	}
}
