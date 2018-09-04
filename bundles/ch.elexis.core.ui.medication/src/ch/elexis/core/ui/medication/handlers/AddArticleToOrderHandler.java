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

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.views.BestellView;
import ch.elexis.data.Artikel;
import ch.elexis.data.Prescription;

public class AddArticleToOrderHandler extends AbstractHandler {
	private static final Logger log = LoggerFactory.getLogger(AddArticleToOrderHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IWorkbenchPage activePage =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
		List<IArticle> articlesToOrder = getArticlesToOrder(
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection());
		if (articlesToOrder.isEmpty()) {
			log.debug("Skip handler execution as no articles are selected to add to an order!");
			return null;
		}
		
		// load BestellView and pass articles to order
		try {
			BestellView bestellView = (BestellView) activePage.showView(BestellView.ID);
			if (bestellView != null) {
				bestellView.addItemsToOrder(articlesToOrder);
			} else {
				log.error("Cant't load BestellView to add articles to order");
			}
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
	private List<IArticle> getArticlesToOrder(ISelection selection){
		List<IArticle> articlesToOrder = new ArrayList<>();
		
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
					CoreModelServiceHolder.get().load(arti.getId(), IArticle.class)
						.ifPresent(a -> articlesToOrder.add(a));
				}
			}
		}
		return articlesToOrder;
	}
}
