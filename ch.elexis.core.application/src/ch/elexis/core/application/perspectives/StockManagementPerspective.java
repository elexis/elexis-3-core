package ch.elexis.core.application.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.core.ui.views.BestellView;
import ch.elexis.core.ui.views.StockView;
import ch.elexis.core.ui.views.artikel.ArtikelSelektor;

public class StockManagementPerspective implements IPerspectiveFactory {
	
	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout){
		layout.setEditorAreaVisible(false);
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		
		layout.addView(BestellView.ID, IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(ArtikelSelektor.ID, IPageLayout.TOP, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(StockView.ID, IPageLayout.BOTTOM, 0.5f, IPageLayout.ID_EDITOR_AREA);
	}
	
	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(IPageLayout layout){}
	
	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(IPageLayout layout){}
	
	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(IPageLayout layout){}
	
}
