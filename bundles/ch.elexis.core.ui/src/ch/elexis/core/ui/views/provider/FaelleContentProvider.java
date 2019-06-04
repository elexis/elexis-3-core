package ch.elexis.core.ui.views.provider;

import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.util.FallComparator;
import ch.elexis.core.ui.views.FaelleView;

/**
 * @since 3.0.0 extracted from {@link FaelleView}
 */
public class FaelleContentProvider implements IStructuredContentProvider {
	
	private FallComparator comparator;
	
	public FaelleContentProvider(){
		comparator = new FallComparator();
	}
	
	public Object[] getElements(final Object inputElement){
		IPatient act = ContextServiceHolder.get().getRootContext().getActivePatient().orElse(null);
		if (act == null) {
			return new Object[0];
		} else {
			ICoverage[] cases = act.getCoverages().toArray(new ICoverage[0]);
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
	