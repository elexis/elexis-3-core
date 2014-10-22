package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.data.LabOrder;
import ch.elexis.data.LabOrder.State;

public class LaborOrdersContentProvider implements ITreeContentProvider {
	private List<LabOrder> orders;
	
	private List<LabOrder> open = new ArrayList<LabOrder>();
	private List<LabOrder> done = new ArrayList<LabOrder>();
	
	private State[] roots = {
		State.ORDERED, State.DONE
	};

	@Override
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		if (newInput instanceof List<?>) {
			orders = (List<LabOrder>) newInput;
		}
	}
	
	private void updateLists(){
		open.clear();
		done.clear();
		for (LabOrder labOrder : orders) {
			if (labOrder.getState() == State.ORDERED) {
				open.add(labOrder);
			} else {
				done.add(labOrder);
			}
		}
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		updateLists();
		return roots;
	}
	
	@Override
	public Object[] getChildren(Object parentElement){
		if (parentElement instanceof State) {
			if (parentElement == State.DONE) {
				return done.toArray();
			} else if (parentElement == State.ORDERED) {
				return open.toArray();
			}
		}
		return null;
	}
	
	@Override
	public Object getParent(Object element){
		return null;
	}
	
	@Override
	public boolean hasChildren(Object element){
		if (element instanceof State) {
			if (element == State.DONE) {
				return !done.isEmpty();
			} else if (element == State.ORDERED) {
				return !open.isEmpty();
			}
		}
		return false;
	}
}
