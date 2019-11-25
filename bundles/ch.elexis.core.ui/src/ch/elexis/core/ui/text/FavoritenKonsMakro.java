package ch.elexis.core.ui.text;

import java.util.Optional;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.data.Konsultation;
import ch.elexis.data.VerrechenbarFavorites;
import ch.elexis.data.VerrechenbarFavorites.Favorite;
import ch.rgw.tools.Result;

public class FavoritenKonsMakro implements IKonsMakro {
	
	@Override
	public String executeMakro(String macro){
		Konsultation actKons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		Favorite fav = VerrechenbarFavorites.findFavoritByMacroForCurrentUser(macro);
		if (fav != null) {
			IEncounter encounter = CoreModelServiceHolder.get().load(actKons.getId(), IEncounter.class).orElse(null);
			Optional<Identifiable> favObj = fav.getObject();
			if (favObj.isPresent()) {
				if (favObj.get() instanceof ICodeElementBlock) {
					BlockMakro blockMacro = new BlockMakro();
					blockMacro.addBlock(actKons, (ICodeElementBlock) favObj.get());
					return StringConstants.EMPTY;
				} else if (favObj.get() instanceof IBillable) {
					Result<IBilled> res =
						BillingServiceHolder.get().bill((IBillable) favObj.get(), encounter, 1.0);
					if (!res.isOK()) {
						MessageEvent.fireError("Error", res.toString());
					} else {
						return StringConstants.EMPTY;
					}
				}
			}
		}
		return null;
	}
	
}
