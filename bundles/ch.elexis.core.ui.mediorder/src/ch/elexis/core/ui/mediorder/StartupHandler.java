package ch.elexis.core.ui.mediorder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ch.elexis.barcode.scanner.BarcodeScannerMessage;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;

@Component(property = EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.EVENT_UPDATE)
public class StartupHandler extends AbstractBillAndCloseMediorderHandler implements EventHandler {

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(ElexisEventTopics.EVENT_UPDATE)) {
			if (event.getProperty("org.eclipse.e4.data") instanceof BarcodeScannerMessage) {
				BarcodeScannerMessage b = (BarcodeScannerMessage) event.getProperty("org.eclipse.e4.data");

				CompletableFuture.runAsync(() -> {
					String trimedChunk = b.getChunk().trim();
					if (trimedChunk != null && trimedChunk.contains("PatientStock")) {
						IQuery<IStock> query = CoreModelServiceHolder.get().getQuery(IStock.class);
						query.and("id", COMPARATOR.EQUALS, trimedChunk);
						if (query.execute().size() != 0) {
							IPatient patient = query.execute().get(0).getOwner().asIPatient();
							List<IStockEntry> stockEntries = StockServiceHolder.get().getPatientStock(patient).get()
									.getStockEntries();
							billAndClose(CoreModelServiceHolder.get(), ContextServiceHolder.get(),
									CoverageServiceHolder.get(), BillingServiceHolder.get(), stockEntries, true);
						}
					}
				});
			}
		}
	}

}
