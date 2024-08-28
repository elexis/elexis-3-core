package ch.elexis.core.ui.mediorder;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.ui.icons.Images;

public class StockEntryReadyContributionItem {
	
	@Inject
	private ECommandService commandService;
	
	@Inject
	private EHandlerService handlerService;
	
	@AboutToShow
	public void fill(List<MMenuElement> items, MPart part) {
		MediorderPart mediorderPart = (MediorderPart) part.getObject();
		List<IStockEntry> entries = mediorderPart.getSelectedStockEntries(); // wird für multiple selection gebraucht
		if (entries != null && !entries.isEmpty()) {
			 boolean hasInStockEntries = entries.stream().anyMatch(entry -> {
		            MediorderEntryState state = MediorderPartUtil.determineState(entry);
		            return state == MediorderEntryState.IN_STOCK || state == MediorderEntryState.PARTIALLY_IN_STOCK;
		        });

			if (hasInStockEntries) {
				MDirectMenuItem dynamicItem = MMenuFactory.INSTANCE.createDirectMenuItem();
				dynamicItem.setType(ItemType.CHECK);
				dynamicItem.setLabel(Messages.Mediorder_BillAndClose);
				dynamicItem.setIconURI(Images.IMG_MONEY.getIconURI());
				dynamicItem.setContributionURI("bundleclass://ch.elexis.core.ui.mediorder/" + getClass().getName()); //$NON-NLS-1$
				items.add(dynamicItem);
			}
		}
	}

	@Execute
	private void setStatus() {
		Map<String, Object> parameters = Map.of("ch.elexis.core.ui.mediorder.commandparameter.billAndCloseOrderEntry",
				"billAndCloseStockEntry");
		ParameterizedCommand parameterizedCommand = commandService
				.createCommand("ch.elexis.core.ui.mediorder.command.billAndCloseOrderEntry", parameters);
		if (parameterizedCommand != null) {
			handlerService.executeHandler(parameterizedCommand);
		}
	}
}
