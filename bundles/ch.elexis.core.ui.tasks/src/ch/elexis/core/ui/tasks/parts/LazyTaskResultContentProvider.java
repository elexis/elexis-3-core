package ch.elexis.core.ui.tasks.parts;

import java.util.List;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.tasks.model.ITask;

public class LazyTaskResultContentProvider implements ILazyContentProvider {
	
	private List<ITask> currentList;
	private TableViewer tableViewerResults;
	
	public LazyTaskResultContentProvider(TableViewer tableViewerResults){
		this.tableViewerResults = tableViewerResults;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		this.currentList = (List<ITask>) newInput;
	}
	
	@Override
	public void updateElement(int index){
		tableViewerResults.replace(currentList.get(index), index);
	}
	
	
	
}
