package ch.elexis.core.ui.views.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.views.FaelleView;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

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
			List<Fall> caseList = Arrays.asList(cases);
			Collections.sort(caseList, new Comparator<Fall>() {
				@Override
				public int compare(Fall f1, Fall f2){
					// compare gesetz
					int comp =
						ObjectUtils.compare(f1.getAbrechnungsSystem(), f2.getAbrechnungsSystem());
					if (comp == 0) {
						// compare beginn date
						TimeTool t1 = new TimeTool(f1.getBeginnDatum());
						TimeTool t2 = new TimeTool(f2.getBeginnDatum());
						comp = t1.isEqual(t2) ? 0 : (t1.isBefore(t2) ? 1 : -1);
						if (comp == 0) {
							// compare open state
							comp = ObjectUtils.compare(f2.isOpen(), f1.isOpen());
							// compare id 
							if (comp == 0) {
								comp =
									ObjectUtils.compare(f1.getBezeichnung(), f2.getBezeichnung());
								if (comp == 0) {
									comp = ObjectUtils.compare(f1.getId(), f2.getId());
								}
								
							}
						}
					}
					return comp;
				}
			});
			
			return caseList.toArray();
		}
		
	}
	
	public void dispose(){
		// TODO Auto-generated method stub
	}
	
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput){
		
	}
	
}
