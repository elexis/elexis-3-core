package ch.elexis.core.ui.text;

import java.util.Optional;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.events.MessageEvent;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.data.VerrechenbarFavorites;
import ch.elexis.data.VerrechenbarFavorites.Favorite;
import ch.rgw.tools.Result;

public class FavoritenKonsMakro implements IKonsMakro {

	@Override
	public String executeMakro(String macro) {
		Optional<IEncounter> actEncounter = ContextServiceHolder.get().getTyped(IEncounter.class);
		if (actEncounter.isPresent()) {
			Favorite fav = VerrechenbarFavorites.findFavoritByMacroForCurrentUser(macro);
			if (fav != null) {
				Optional<Identifiable> favObj = fav.getObject();
				if (favObj.isPresent()) {
					if (favObj.get() instanceof ICodeElementBlock) {
						BlockMakro blockMacro = new BlockMakro();
						blockMacro.addBlock(actEncounter.get(), (ICodeElementBlock) favObj.get());
						return StringConstants.EMPTY;
					} else if (favObj.get() instanceof IBillable) {
						Result<IBilled> res = BillingServiceHolder.get().bill((IBillable) favObj.get(),
								actEncounter.get(), 1.0);
						if (!res.isOK()) {
							MessageEvent.fireError("Error", res.toString()); //$NON-NLS-1$
						} else {
							return StringConstants.EMPTY;
						}
					}
				}
			}
		}
		return null;
	}

}
