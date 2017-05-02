package ch.elexis.core.ui.text;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.VerrechenbarFavorites;
import ch.elexis.data.VerrechenbarFavorites.Favorite;
import ch.rgw.tools.Result;

public class FavoritenKonsMakro implements IKonsMakro {
	
	@Override
	public String executeMakro(String macro){
		Konsultation actKons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		Favorite fav = VerrechenbarFavorites.findFavoritByMacroForCurrentUser(macro);
		if (fav != null) {
			if (fav.getPersistentObject() instanceof IVerrechenbar) {
				Result<IVerrechenbar> res =
					actKons.addLeistung((IVerrechenbar) fav.getPersistentObject());
				if (!res.isOK()) {
					MessageEvent.fireError("Error", res.toString());
				}
			} else if (fav.getPersistentObject() instanceof Leistungsblock) {
				BlockMakro blockMacro = new BlockMakro();
				blockMacro.addBlock(actKons, (Leistungsblock) fav.getPersistentObject());
			}
		}
		
		return StringConstants.EMPTY;
	}
	
}
