package ch.elexis.core.ui.text;

import java.util.List;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.Query;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class BlockMakro implements IKonsMakro {
	
	@Override
	public String executeMakro(String makro){
		Konsultation actKons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		
		Query<Leistungsblock> qbe = new Query<Leistungsblock>(Leistungsblock.class);
		qbe.add(Leistungsblock.NAME, Query.EQUALS, makro);
		qbe.startGroup();
		qbe.add(Leistungsblock.MANDANT_ID, Query.EQUALS, CoreHub.actMandant.getId());
		qbe.or();
		qbe.add(Leistungsblock.MANDANT_ID, Query.EQUALS, StringTool.leer);
		qbe.endGroup();
		List<Leistungsblock> list = qbe.execute();
		if ((list != null) && (list.size() > 0) && (actKons != null)) {
			Leistungsblock lb = list.get(0);
			for (ICodeElement ice : lb.getElements()) {
				if (ice instanceof IVerrechenbar) {
					Result<IVerrechenbar> res = actKons.addLeistung((IVerrechenbar) ice);
					if (!res.isOK()) {
						MessageEvent.fireError("Error", res.toString());
					}
				} else if (ice instanceof IDiagnose) {
					actKons.addDiagnose((IDiagnose) ice);
				}
			}
		}
		return "";
	}
}
