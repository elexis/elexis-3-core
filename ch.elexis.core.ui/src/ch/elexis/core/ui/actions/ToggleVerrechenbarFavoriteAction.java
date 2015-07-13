package ch.elexis.core.ui.actions;

import org.eclipse.jface.action.Action;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.VerrechenbarAdapter;
import ch.elexis.data.VerrechenbarFavorites;
import ch.elexis.data.VerrechenbarFavorites.Favorite;

public class ToggleVerrechenbarFavoriteAction extends Action {
	
	private PersistentObject currentSelection;
	
	@Override
	public void run(){
		Favorite favorite = VerrechenbarFavorites.isFavorite(currentSelection);
		VerrechenbarFavorites.setFavorite(currentSelection, favorite == null);
	}
	
	public void updateSelection(Object object){
		if (object instanceof VerrechenbarAdapter || object instanceof Leistungsblock) {
			setEnabled(true);
			currentSelection = (PersistentObject) object;
			Favorite favorite = VerrechenbarFavorites.isFavorite(currentSelection);
			setText((favorite != null) ? Messages.ToggleVerrechenbarFavoriteAction_DeFavorize
					: Messages.ToggleVerrechenbarFavoriteAction_Favorize);
			setImageDescriptor((favorite != null) ? Images.IMG_STAR.getImageDescriptor()
					: Images.IMG_STAR_EMPTY.getImageDescriptor());
		} else {
			setEnabled(false);
			currentSelection = null;
		}
	}
}
