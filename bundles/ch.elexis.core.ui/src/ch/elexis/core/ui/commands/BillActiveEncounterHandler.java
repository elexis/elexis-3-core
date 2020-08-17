package ch.elexis.core.ui.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.views.rechnung.RnOutputDialog;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.Result.msg;

public class BillActiveEncounterHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Konsultation selectedEncounter =
			(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		if (selectedEncounter != null) {
			Result<Konsultation> result = BillingUtil.getBillableResult(selectedEncounter);
			if (result.isOK()) {
				List<Result<Rechnung>> results =
					BillingUtil.createBills(getBillMap(selectedEncounter));
				if (!results.isEmpty() && results.get(0).isOK()) {
					Rechnung invoice = results.get(0).get();
					new RnOutputDialog(UiDesk.getTopShell(), Collections.singletonList(invoice))
						.open();
				}
			} else {
				StringBuilder sb = new StringBuilder();
				for (@SuppressWarnings("rawtypes")
				msg message : result.getMessages()) {
					if (message.getSeverity() != SEVERITY.OK) {
						if (sb.length() > 0) {
							sb.append(" / ");
						}
						sb.append(message.getText());
					}
				}
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Nicht verrechenbar",
					"Die Konsultatione kann nicht verrechnet werden.\n\n" + sb.toString());
			}
		}
		return null;
	}
	
	private Map<Rechnungssteller, Map<Fall, List<Konsultation>>> getBillMap(
		Konsultation encounter){
		Map<Rechnungssteller, Map<Fall, List<Konsultation>>> ret = new HashMap<>();
		ret.put(encounter.getMandant().getRechnungssteller(),
			Collections.singletonMap(encounter.getFall(), Collections.singletonList(encounter)));
		return ret;
	}
	
}
