package ch.elexis.core.ui.mediorder.internal.handler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Service;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStockService;

public class CreatePatientOrderHandler {

	@Inject
	@Service(filterExpression = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	IModelService coreModelService;

	@Inject
	EPartService partService;

	@Inject
	IContextService contextService;

	@Inject
	IEventBroker eventBroker;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, IConfigService configService,
			IOrderService orderService, IStockService stockService) {

		boolean excludeAlreadyOrderedItems = configService.get(
				Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER,
				Preferences.INVENTORY_ORDER_EXCLUDE_ALREADY_ORDERED_ITEMS_ON_NEXT_ORDER_DEFAULT);

		IQuery<IStock> queryStock = coreModelService.getQuery(IStock.class);
		queryStock.and("id", COMPARATOR.LIKE, "PatientStock-%");
		List<IStock> patientStocks = queryStock.execute();

		List<IStockEntry> stockEntries = patientStocks.parallelStream().flatMap(s -> s.getStockEntries().stream())
				.toList();

		if (excludeAlreadyOrderedItems) {
			stockEntries = new ArrayList<>(stockEntries);
			for (Iterator<IStockEntry> iterator = stockEntries.iterator(); iterator.hasNext();) {
				IStockEntry stockEntry = iterator.next();
				if (orderService.findOpenOrderEntryForStockEntry(stockEntry) != null) {
					iterator.remove();
				}
			}
		}

		if (stockEntries.isEmpty()) {
			MessageDialog.openInformation(shell, "Keine anwendbaren Einträge", "Keine anwendbaren Einträge gefunden");
			return;
		}

		IOrder order = coreModelService.create(IOrder.class);
		order.setTimestamp(LocalDateTime.now());
		order.setName("Medikamentenbestellung");
		coreModelService.save(order);

		for (IStockEntry stockEntry : stockEntries) {
			if (stockEntry.getArticle() != null) {
				if (stockEntry.getMaximumStock() != 0) {
				orderService.addRefillForStockEntryToOrder(stockEntry, order);
				}
			} else {
				LoggerFactory.getLogger(getClass()).warn("Could not resolve article [{}] of stock entry [{}]", //$NON-NLS-1$
						stockEntry.getLabel(), stockEntry.getId());
			}
		}
		eventBroker.post(ElexisEventTopics.EVENT_RELOAD, IStock.class);

		MPart orderPart = partService.findPart("ch.elexis.BestellenView");
		if (orderPart == null) {
			orderPart = partService.createPart("ch.elexis.BestellenView");
		}
		partService.showPart(orderPart, PartState.VISIBLE);
		contextService.setTyped(order);
	}
}