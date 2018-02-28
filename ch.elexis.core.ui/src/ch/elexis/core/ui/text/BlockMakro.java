package ch.elexis.core.ui.text;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IDiagnose;
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
			addBlock(actKons, lb);
			
			return StringConstants.EMPTY;
		}
		return null;
	}
	
	public void addBlock(Konsultation kons, Leistungsblock block){
		List<ICodeElement> elements = block.getElements();
		for (ICodeElement ice : elements) {
			if (ice instanceof IVerrechenbar) {
				Result<IVerrechenbar> res = kons.addLeistung((IVerrechenbar) ice);
				if (!res.isOK()) {
					MessageEvent.fireError("Error", res.toString());
				}
			} else if (ice instanceof IDiagnose) {
				kons.addDiagnose((IDiagnose) ice);
			}
		}
		java.util.List<ICodeElement> diff = block.getDiffToReferences(elements);
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
