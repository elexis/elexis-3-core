package ch.elexis.core.ui.mediorder.internal.handler;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.PlatformUI;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.core.ui.mediorder.MediorderPart;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class EditMediorderEntryHandler {

	@Inject
	IStockService stockService;

	@Inject
	@Service(filterExpression = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	private static final String TAB_ARTIKELSTAMM = "Artikelstamm";
	private static final String TARGET_NAME = "Mediorder";

	@Execute
	public void execute(MPart part, @Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object selection) {
		MediorderPart mediOrderPart = (MediorderPart) part.getObject();
		List<IStockEntry> selectedEntries = mediOrderPart.getSelectedStockEntries();
		if (selectedEntries == null || selectedEntries.isEmpty()) {
			return;
		}

		IStockEntry stockEntry = mediOrderPart.getSelectedStockEntries().get(0);
		IStock stock = stockEntry.getStock();

		try {
			LeistungenView leistungenView = (LeistungenView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(LeistungenView.ID);

			CodeSelectorHandler csHandler = CodeSelectorHandler.getInstance();
			csHandler.setCodeSelectorTarget(new ICodeSelectorTarget() {

				@Override
				public void codeSelected(Object code) {
					if (code instanceof IArticle) {
						stockService.unstoreArticleFromStock(stock,
								StoreToStringServiceHolder.getStoreToString(stockEntry.getArticle()));

						IArticle article = (IArticle) code;
						IStockEntry entry = stockService.storeArticleInStock(stock, article);
						entry.setMinimumStock(stockEntry.getMinimumStock());
						entry.setCurrentStock(stockEntry.getCurrentStock());
						entry.setMaximumStock(stockEntry.getMaximumStock());
						coreModelService.save(entry);
						mediOrderPart.refresh();
					}
				}

				@Override
				public void registered(boolean isRegistered) {
				}

				@Override
				public String getName() {
					return TARGET_NAME;
				}
			});
			csHandler.getCodeSelectorTarget().registered(false);

			for (CTabItem cti : leistungenView.ctab.getItems()) {
				if (cti.getText().equalsIgnoreCase(TAB_ARTIKELSTAMM)) {
					leistungenView.setSelected(cti);
					leistungenView.setFocus();
					leistungenView.ctab.setSelection(cti);
				}
			}

		} catch (Exception e) {
			LoggerFactory.getLogger(EditMediorderEntryHandler.class).error("Error showing LeistungenView", e); //$NON-NLS-1$
		}
	}
}
