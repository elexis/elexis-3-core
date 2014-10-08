package ch.elexis.core.ui.preferences;

import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.data.Role;

public class RoleTreeContentProvider implements ITreeContentProvider, ICheckStateProvider {
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isChecked(Object element){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isGrayed(Object element){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		Role r = (Role) inputElement;
		return r.getChildren();
	}
	
	@Override
	public Object[] getChildren(Object parentElement){
		Role r = (Role) parentElement;
		return r.getChildren();
	}
	
	@Override
	public Object getParent(Object element){
		Role r = (Role) element;
		return r.getParent();
	}
	
	@Override
	public boolean hasChildren(Object element){
		Role r = (Role) element;
		return r.getChildren().length>0;
	}
	
}
