package ch.elexis.core.ui.text;

import java.util.List;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.model.ICodeElement;
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
		for (ICodeElement ice : block.getElements()) {
			if (ice instanceof IVerrechenbar) {
				Result<IVerrechenbar> res = kons.addLeistung((IVerrechenbar) ice);
				if (!res.isOK()) {
					MessageEvent.fireError("Error", res.toString());
				}
			} else if (ice instanceof IDiagnose) {
				kons.addDiagnose((IDiagnose) ice);
			}
		}
	}
}
