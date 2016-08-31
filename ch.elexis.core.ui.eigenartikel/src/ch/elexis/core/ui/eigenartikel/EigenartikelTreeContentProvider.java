
package ch.elexis.core.ui.eigenartikel;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;
import ch.elexis.data.Query;

public class EigenartikelTreeContentProvider
		implements ITreeContentProvider, ICommonViewerContentProvider {
	
	public static String FILTER_KEY = "Name";
	private CommonViewer commonViewer;
	private String filter = null;
	
	public EigenartikelTreeContentProvider(CommonViewer cv){
		this.commonViewer = cv;
	}
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
	
	@Override
	public void reorder(String field){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void selected(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void init(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void startListening(){
		commonViewer.getConfigurer().getControlFieldProvider().addChangeListener(this);
	}
	
	@Override
	public void stopListening(){
		commonViewer.getConfigurer().getControlFieldProvider().removeChangeListener(this);
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		Query<Eigenartikel> qre = new Query<Eigenartikel>(Eigenartikel.class);
		qre.add(Eigenartikel.FLD_TYP, Query.EQUALS, Eigenartikel.TYPNAME);
		if (filter != null) {
			qre.add(Eigenartikel.FLD_NAME, Query.LIKE, "%" + filter + "%");
		}
		qre.orderBy(false, Eigenartikel.FLD_NAME, Eigenartikel.FLD_EXTID);
		List<Eigenartikel> execute = qre.execute();
		List<Eigenartikel> collect =
			execute.stream().filter(p -> !p.isValidPackage()).collect(Collectors.toList());
		return collect.toArray();
	}
	
	@Override
	public Object[] getChildren(Object parentElement){
		if (parentElement != null && parentElement instanceof Eigenartikel) {
			Eigenartikel ea = (Eigenartikel) parentElement;
			if (ea.isProduct()) {
				return ea.getPackages().toArray();
			}
		}
		return null;
	}
	
	@Override
	public Object getParent(Object element){
		//		Eigenartikel ea = (Eigenartikel) element;
		//		if(ea.isProduct()) {
		//			return null;
		//		} else {
		//			return Eigenartikel.load(ea.get(Eigenartikel.FLD_EXTID));
		//		}
		return null;
	}
	
	@Override
	public boolean hasChildren(Object element){
		Eigenartikel ea = (Eigenartikel) element;
		return ea.isProduct() && ea.getPackages().size() > 0;
	}
	
	@Override
	public void changed(HashMap<String, String> values){
		String filterValue = values.get(FILTER_KEY).toLowerCase();
		if (filterValue != null && filterValue.length() > 1) {
			filter = filterValue;
		} else {
			filter = null;
		}
		commonViewer.notify(CommonViewer.Message.update);
	}
	
}
