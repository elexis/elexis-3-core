package ch.elexis.core.ui.text;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dialogs.ResultDialog;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.data.Leistungsblock;
import ch.rgw.tools.Result;

public class BlockMakro implements IKonsMakro {

	@Override
	public String executeMakro(String makro) {
		Optional<IEncounter> actEncounter = ContextServiceHolder.get().getTyped(IEncounter.class);

		if (actEncounter.isPresent()) {
			List<Leistungsblock> macros = Leistungsblock.findMacrosValidForCurrentMandator(makro);
			if ((macros != null) && (macros.size() > 0)) {
				Leistungsblock lb = macros.get(0);
				addBlock(actEncounter.get(),
						CoreModelServiceHolder.get().load(lb.getId(), ICodeElementBlock.class).orElse(null));

				return StringConstants.EMPTY;
			}
		}
		return null;
	}

	public void addBlock(IEncounter encounter, ICodeElementBlock elementBlock) {
		if (elementBlock != null && encounter != null) {
			List<ch.elexis.core.model.ICodeElement> elements = elementBlock.getElements();
			StringJoiner notOkResults = new StringJoiner("\n");
			for (ICodeElement ice : elements) {
				if (ice instanceof IBillable) {
					Result<IBilled> res = BillingServiceHolder.get().bill((IBillable) ice, encounter, 1.0);
					if (!res.isOK()) {
						String message = ice.getCode() + " - " + ResultDialog.getResultMessage(res);
						if (!notOkResults.toString().contains(message)) {
							notOkResults.add(message);
						}
					}
				} else if (ice instanceof IDiagnosis) {
					encounter.addDiagnosis((IDiagnosis) ice);
					CoreModelServiceHolder.get().save(encounter);
				}
			}
			if (!notOkResults.toString().isEmpty()) {
				MessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.ResultDialog_Warning,
						notOkResults.toString());
			}
			java.util.List<ICodeElement> diff = elementBlock.getDiffToReferences(elements);
			if (!diff.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				diff.forEach(r -> {
					if (sb.length() > 0) {
						sb.append("\n");
					}
					sb.append(r);
				});
				MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Warnung",
						"Warnung folgende Leistungen konnten im aktuellen Kontext (Fall, Konsultation, Gesetz) nicht verrechnet werden.\n"
								+ sb.toString());
			}
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, encounter);
		}
	}
}
