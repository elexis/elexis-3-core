package ch.elexis.core.ui.text;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Leistungsblock;
import ch.rgw.tools.Result;

public class BlockMakro implements IKonsMakro {
	
	@Override
	public String executeMakro(String makro){
		Konsultation actKons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		
		List<Leistungsblock> macros = Leistungsblock.findMacrosValidForCurrentMandator(makro);
		if ((macros != null) && (macros.size() > 0) && (actKons != null)) {
			Leistungsblock lb = macros.get(0);
			addBlock(actKons, CoreModelServiceHolder.get().load(lb.getId(), ICodeElementBlock.class)
				.orElse(null));
			
			return StringConstants.EMPTY;
		}
		return null;
	}
	
	public void addBlock(Konsultation actKons, ICodeElementBlock elementBlock){
		IEncounter encounter = CoreModelServiceHolder.get().load(actKons.getId(), IEncounter.class).orElse(null);
		if (elementBlock != null && encounter != null) {
			List<ch.elexis.core.model.ICodeElement> elements = elementBlock.getElements();
			for (ICodeElement ice : elements) {
				if (ice instanceof IBillable) {
					Result<IBilled> res =
						BillingServiceHolder.get().bill((IBillable) ice, encounter, 1.0);
					if (!res.isOK()) {
						MessageEvent.fireError("Error", res.toString());
					}
				} else if (ice instanceof IDiagnosis) {
					encounter.addDiagnosis((IDiagnosis) ice);
					CoreModelServiceHolder.get().save(encounter);
				}
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
		}
	}
}
