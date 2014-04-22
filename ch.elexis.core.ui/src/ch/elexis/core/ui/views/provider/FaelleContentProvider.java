package ch.elexis.core.ui.views.provider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.views.FaelleView;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;

/**
 * @since 3.0.0 extracted from {@link FaelleView}
 */
public class FaelleContentProvider implements IStructuredContentProvider {
	
	public Object[] getElements(final Object inputElement){
		Patient act = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		if (act == null) {
			return new Object[0];
		} else {
			Fall[] cases = act.getFaelle();
			List<Fall> caseList = new ArrayList<Fall>();
			
			for (Fall fall : cases) {
				if (fall.isOpen()) {
					caseList.add(0, fall);
				} else {
					caseList.add(fall);
				}
			}
			return caseList.toArray();
		}
		
	}
	
	public void dispose(){
		// TODO Auto-generated method stub
	}
	
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput){
		
	}
	
}
