
package ch.elexis.core.ui.eigenartikel;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ITreeContentProvider;

import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;
import ch.elexis.data.Query;

public class EigenartikelTreeContentProvider
		implements ITreeContentProvider, ICommonViewerContentProvider {
	
	public static String FILTER_KEY = "Name";
	private CommonViewer commonViewer;
	private String filter = null;
	
	private boolean showProducts;
	
	public EigenartikelTreeContentProvider(CommonViewer cv){
		this.commonViewer = cv;
	}
	
	@Override
	public void reorder(String field){}
	
	@Override
	public void selected(){}
	
	@Override
	public void init(){}
	
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
		Query<Eigenartikel> qre = new Query<>(Eigenartikel.class, Eigenartikel.FLD_TYP,
			Eigenartikel.TYPNAME, Eigenartikel.TABLENAME, new String[] {
				Eigenartikel.FLD_NAME, Eigenartikel.FLD_EXTID, Eigenartikel.FLD_EXTINFO
			});
		if (filter != null) {
			qre.add(Eigenartikel.FLD_NAME, Query.LIKE, "%" + filter + "%", true);
		}
		if (!showProducts) {
			qre.add(Eigenartikel.FLD_EXTID, Query.NOT_EQUAL, null);
		}
		qre.orderBy(false, Eigenartikel.FLD_NAME, Eigenartikel.FLD_EXTID);
		List<Eigenartikel> execute = qre.execute();
		List<Eigenartikel> collect;
		if (showProducts) {
			collect =
				execute.stream().filter(p -> !p.isValidPackage()).collect(Collectors.toList());
		} else {
			collect = execute;
		}
		
		return collect.toArray();
	}
	
	@Override
	public Object[] getChildren(Object parentElement){
		if (!showProducts) {
			return null;
		}
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
		return null;
	}
	
	@Override
	public boolean hasChildren(Object element){
		if (!showProducts) {
			return false;
		}
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
	
	public void setShowProducts(boolean checked){
		this.showProducts = checked;
		commonViewer.notify(CommonViewer.Message.update);
	}
	
}
