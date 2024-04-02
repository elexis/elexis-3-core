
package ch.elexis.core.ui.eigenartikel;

import java.util.HashMap;

import org.eclipse.jface.viewers.ITreeContentProvider;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;

public class EigenartikelTreeContentProvider implements ITreeContentProvider, ICommonViewerContentProvider {

	public static String FILTER_KEY = "Name"; //$NON-NLS-1$
	private CommonViewer commonViewer;
	private String filter = null;

	private boolean showProducts;

	public EigenartikelTreeContentProvider(CommonViewer cv) {
		this.commonViewer = cv;
	}

	@Override
	public void reorder(String field) {
	}

	@Override
	public void selected() {
	}

	@Override
	public void init() {
	}

	@Override
	public void startListening() {
		commonViewer.getConfigurer().getControlFieldProvider().addChangeListener(this);
	}

	@Override
	public void stopListening() {
		commonViewer.getConfigurer().getControlFieldProvider().removeChangeListener(this);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		IQuery<IArticle> query = CoreModelServiceHolder.get().getQuery(IArticle.class);
		query.and(ModelPackage.Literals.IARTICLE__TYP, COMPARATOR.EQUALS, ArticleTyp.EIGENARTIKEL);
		if (filter != null) {
			query.and(ModelPackage.Literals.IARTICLE__NAME, COMPARATOR.LIKE, "%" + filter + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!showProducts) {
			query.and(ModelPackage.Literals.IARTICLE__PRODUCT, COMPARATOR.NOT_EQUALS, null);
		} else {
			query.and(ModelPackage.Literals.IARTICLE__PRODUCT, COMPARATOR.EQUALS, null);
		}
		query.orderBy(ModelPackage.Literals.IARTICLE__NAME, ORDER.ASC);
		return query.execute().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (!showProducts) {
			return null;
		}
		if (parentElement != null && parentElement instanceof IArticle) {
			IArticle ea = (IArticle) parentElement;
			if (ea.isProduct()) {
				return ea.getPackages().toArray();
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (!showProducts) {
			return false;
		}
		IArticle ea = (IArticle) element;
		return ea.isProduct() && !ea.getPackages().isEmpty();
	}

	@Override
	public void changed(HashMap<String, String> values) {
		String filterValue = values.get(FILTER_KEY).toLowerCase();
		if (filterValue != null && filterValue.length() > 1) {
			filter = filterValue;
		} else {
			filter = null;
		}
		commonViewer.notify(CommonViewer.Message.update);
	}

	public void setShowProducts(boolean checked) {
		this.showProducts = checked;
		commonViewer.notify(CommonViewer.Message.update);
	}

}
