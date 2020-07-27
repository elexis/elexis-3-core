package ch.elexis.core.ui.documents.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.documents.FilterCategory;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;

public class DocumentsTreeContentProvider implements ITreeContentProvider {
	
	private Map<ICategory, List<IDocument>> documentsMap = new HashMap<>();
	private FilterCategory selectedFilter = null;
	
	private boolean flat;
	
	private TreeViewer viewer;
	
	private IPatient currentPatient;
	
	public DocumentsTreeContentProvider(TreeViewer viewer){
		this.viewer = viewer;
		this.flat = false;
	}
	
	public void setFlat(boolean value){
		this.flat = value;
	}
	
	public void inputChanged(Viewer v, Object oldInput, Object newInput){
		if (newInput instanceof IPatient) {
			loadByFilterCategory((IPatient) newInput);
		} else {
			documentsMap.clear();
		}
	}
	
	public DocumentsTreeContentProvider selectFilterCategory(ISelection selection){
		StructuredSelection sel = (StructuredSelection) selection;
		if (selection != null) {
			Object element = sel.getFirstElement();
			if (element instanceof FilterCategory) {
				selectedFilter = (FilterCategory) element;
				if (viewer != null) {
					viewer.refresh(true);
					viewer.expandAll();
				}
			}
		}
		return this;
	}
	
	private void loadByFilterCategory(IPatient newInput){
		if (newInput != currentPatient) {
			if (newInput != null) {
				if (selectedFilter.isAll()) {
					documentsMap = DocumentStoreServiceHolder.getService()
						.getDocumentsByPatientId(newInput.getId());
					if (viewer != null) {
						viewer.refresh(true);
					}
				} else {
					loadElementsByCategory(newInput.getId(), selectedFilter);
				}
			}
		}
		currentPatient = newInput;
	}
	
	public void updateElement(IDocument iDocument){
		// also called after category creation - that call will be ignored
		if (iDocument != null && !"text/category".equals(iDocument.getMimeType())) {
			removeFromCategories(iDocument);
			FilterCategory filterCategory = new FilterCategory(iDocument.getCategory());
			List<IDocument> categoryDocuments = documentsMap.get(filterCategory);
			if (categoryDocuments != null) {
				if (!categoryDocuments.contains(iDocument) && !iDocument.isDeleted()) {
					categoryDocuments.add(iDocument);
				} else if (categoryDocuments.contains(iDocument) && iDocument.isDeleted()) {
					categoryDocuments.remove(iDocument);
				}
			} else if (!iDocument.isDeleted()) {
				categoryDocuments = new ArrayList<>();
				categoryDocuments.add(iDocument);
				documentsMap.put(filterCategory, categoryDocuments);
			}
		}
	}
	
	private void removeFromCategories(IDocument iDocument){
		Set<ICategory> categories = new HashSet<>(documentsMap.keySet());
		for (ICategory category : categories) {
			List<IDocument> categoryDocuments = documentsMap.get(category);
			categoryDocuments.remove(iDocument);
			if (categoryDocuments.isEmpty()) {
				documentsMap.remove(category);
			}
		}
	}
	
	private void loadElementsByCategory(String patientId, ICategory iCategory){
		if (!(iCategory instanceof FilterCategory) || documentsMap.get(iCategory) == null) {
			List<IDocument> iDocuments = DocumentStoreServiceHolder.getService()
				.getDocumentsByCategory(patientId, iCategory);
			if (!iDocuments.isEmpty()) {
				documentsMap.put(new FilterCategory(iDocuments.get(0).getCategory()), iDocuments);
			}
		}
		if (viewer != null) {
			viewer.refresh(true);
		}
	}
	
	public void dispose(){}
	
	public Object[] getElements(Object parent){
		if (documentsMap.isEmpty() && parent != null) {
			if (parent instanceof IPatient) {
				loadByFilterCategory((IPatient) parent);
			}
		}
		Object[] categories = getFilteredCategories();
		if (flat) {
			// single category - load its children
			if (categories.length == 1) {
				return getChildren(categories[0]);
			} else {
				return Arrays.stream(categories).flatMap(o -> Arrays.stream(getChildren(o)))
					.toArray();
			}
		}
		return categories;
	}
	
	private Object[] getFilteredCategories(){
		if (selectedFilter.isAll()) {
			List<ICategory> keys = new ArrayList<>(documentsMap.keySet());
			return keys.toArray();
		} else if (documentsMap.containsKey(selectedFilter)) {
			return new Object[] {
				selectedFilter
			};
		}
		return new Object[0];
	}
	
	public Object[] getChildren(Object parentElement){
		if (parentElement instanceof ICategory && documentsMap.containsKey(parentElement)) {
			return documentsMap.get(parentElement).toArray();
		} else {
			return new Object[0];
		}
	}
	
	public Object getParent(Object element){
		if (element instanceof IDocument) {
			IDocument dh = (IDocument) element;
			return new FilterCategory(dh.getCategory());
		}
		return null;
	}
	
	public boolean hasChildren(Object element){
		return documentsMap.containsKey(element);
	}

	public void setViewer(TreeViewer treeViewer){
		this.viewer = treeViewer;
	}
}
