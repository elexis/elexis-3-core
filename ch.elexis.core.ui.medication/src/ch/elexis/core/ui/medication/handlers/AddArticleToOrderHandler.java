package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.BestellView;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;
import ch.rgw.tools.StringTool;

public class AddArticleToOrderHandler extends AbstractHandler {
	private static final Logger log = LoggerFactory.getLogger(AddArticleToOrderHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IWorkbenchPage activePage =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
		List<Artikel> articlesToOrder = getArticlesToOrder(
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection());
		if (articlesToOrder.isEmpty()) {
			log.debug("Skip handler execution as no articles are selected to add to an order!");
			return null;
		}
		
		// load BestellView and pass articles to order
		try {
			if (StringTool.isNothing(BestellView.ID)) {
				log.warn("BestellView.ID not found or empty");
				SWTHelper.alert("Fehler", "BestellView.ID");
			}
			
			activePage.showView(BestellView.ID);
			BestellView bestellView = (BestellView) activePage.findView(BestellView.ID);
			//hide BestellView as we only need to make sure it's properly initialized
			activePage.hideView(bestellView);
			
			bestellView.addItemsToOrder(articlesToOrder);
		} catch (PartInitException e) {
			log.error("Cant't load BestellView to add articles to order", e);
		}
		return null;
		
	}
	
	/**
	 * Add articles from all the selected prescriptions to an arraylist
	 * 
	 * @param selection
	 *            from the current view
	 * @return a list of selected articles or an {@code EMPTY} list
	 */
	@SuppressWarnings("unchecked")
	private List<Artikel> getArticlesToOrder(ISelection selection){
		List<Artikel> articlesToOrder = new ArrayList<Artikel>();
		
		if (selection == null || selection.isEmpty()) {
			return articlesToOrder;
		}
		
		// add all selected articles to order list
		IStructuredSelection structSelcection = (IStructuredSelection) selection;
		List<MedicationTableViewerItem> mtvItems = structSelcection.toList();
		for (MedicationTableViewerItem mtvItem : mtvItems) {
			Prescription p = mtvItem.getPrescription();
			if (p != null) {
				Artikel arti = p.getArtikel();
				if (arti != null) {
					articlesToOrder.add(arti);
				}
			}
		}
		return articlesToOrder;
	}
}
