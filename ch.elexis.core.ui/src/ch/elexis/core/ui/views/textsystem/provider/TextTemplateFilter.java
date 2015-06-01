package ch.elexis.core.ui.views.textsystem.provider;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.ui.views.textsystem.model.TextTemplate;

public class TextTemplateFilter extends ViewerFilter {
	private String searchTerm;
	
	public void setSearchTerm(String term){
		this.searchTerm = ".*" + term.toLowerCase() + ".*";
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element){
		if (searchTerm == null || searchTerm.length() == 0) {
			return true;
		}
		
		TextTemplate tt = (TextTemplate) element;
		String name = tt.getName().toLowerCase();
		if (name.matches(searchTerm)) {
			return true;
		}
		
		String desc = tt.getDescription().toLowerCase();
		if (desc.matches(searchTerm)) {
			return true;
		}
		
		String mime = tt.getMimeTypePrintname().toLowerCase();
		if (mime.matches(searchTerm)) {
			return true;
		}
		
		String mandant = tt.getMandantLabel().toLowerCase();
		if (mandant.matches(searchTerm)) {
			return true;
		}
		
		return false;
	}
	
}
