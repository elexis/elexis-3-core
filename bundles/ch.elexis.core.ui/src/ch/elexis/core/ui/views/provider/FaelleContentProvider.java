package ch.elexis.core.ui.views.provider;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.util.FallComparator;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;

public class FaelleContentProvider implements IStructuredContentProvider {
	
	private FallComparator comparator;
	
	public FaelleContentProvider(){
		comparator = new FallComparator();
	}
	
	public Object[] getElements(final Object inputElement){
		Patient act = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		if (act == null) {
			return new Object[0];
		} else {
			Fall[] cases = act.getFaelle();
			Arrays.sort(cases, comparator);
			return cases;
		}
		
	}
	
	public void dispose(){
		// TODO Auto-generated method stub
	}
	
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput){
		
	}
}
	