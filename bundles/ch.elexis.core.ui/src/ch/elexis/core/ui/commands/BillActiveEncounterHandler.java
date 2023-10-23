package ch.elexis.core.ui.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.util.BillingUtil;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.services.holder.ContextServiceHolder;
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
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Optional<IEncounter> selectedEncounter = ContextServiceHolder.get().getTyped(IEncounter.class);
		if (selectedEncounter.isPresent()) {
			Konsultation selectedKonsultation = (Konsultation) NoPoUtil.loadAsPersistentObject(selectedEncounter.get());
			Result<Konsultation> result = BillingUtil.getBillableResult(selectedKonsultation);
			if (result.isOK()) {
				List<Result<IInvoice>> results = BillingUtil.createBills(getBillMap(selectedKonsultation));
				if (!results.isEmpty() && results.get(0).isOK()) {
					Rechnung invoice = Rechnung.load(results.get(0).get().getId());
					new RnOutputDialog(UiDesk.getTopShell(), Collections.singletonList(invoice)).open();
				} else {
					for (Result<IInvoice> invoiceResult : results) {
						if (!invoiceResult.isOK()) {
							showResult(invoiceResult, HandlerUtil.getActiveShell(event));
						}
					}
				}
			} else {
				showResult(result, HandlerUtil.getActiveShell(event));
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private void showResult(Result<?> result, Shell shell) {
		StringBuilder sb = new StringBuilder();
		for (msg message : result.getMessages()) {
			if (message.getSeverity() != SEVERITY.OK) {
				if (sb.length() > 0) {
					sb.append(" / "); //$NON-NLS-1$
				}
				sb.append(message.getText());
			}
		}
		MessageDialog.openInformation(shell, "Nicht verrechenbar",
				"Die Konsultation kann nicht verrechnet werden.\n\n" + sb.toString());
	}

	private Map<Rechnungssteller, Map<Fall, List<Konsultation>>> getBillMap(Konsultation encounter) {
		Map<Rechnungssteller, Map<Fall, List<Konsultation>>> ret = new HashMap<>();
		ret.put(encounter.getMandant().getRechnungssteller(),
				Collections.singletonMap(encounter.getFall(), Collections.singletonList(encounter)));
		return ret;
	}

}
