package ch.elexis.core.ui.actions;

import org.eclipse.jface.action.Action;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.VerrechenbarFavorites;
import ch.elexis.data.VerrechenbarFavorites.Favorite;

public class ToggleVerrechenbarFavoriteAction extends Action {
	
	private IBillable currentBillable;
	
	private ICodeElementBlock currentBlock;
	
	@Override
	public void run(){
		if (currentBillable != null) {
			Favorite favorite = VerrechenbarFavorites.isFavorite(currentBillable);
			VerrechenbarFavorites.setFavorite(currentBillable, favorite == null);
		} else if (currentBlock != null) {
			Favorite favorite = VerrechenbarFavorites.isFavorite(currentBlock);
			VerrechenbarFavorites.setFavorite(currentBlock, favorite == null);
		}
	}
	
	public void updateSelection(Object object){
		if (object instanceof IBillable) {
			setEnabled(true);
			currentBillable = (IBillable) object;
			Favorite favorite = VerrechenbarFavorites.isFavorite(currentBillable);
			setText((favorite != null) ? Messages.ToggleVerrechenbarFavoriteAction_DeFavorize
					: Messages.ToggleVerrechenbarFavoriteAction_Favorize);
			setImageDescriptor((favorite != null) ? Images.IMG_STAR.getImageDescriptor()
					: Images.IMG_STAR_EMPTY.getImageDescriptor());
		} else if (object instanceof ICodeElementBlock) {
			setEnabled(true);
			currentBlock = (ICodeElementBlock) object;
			Favorite favorite = VerrechenbarFavorites.isFavorite(currentBlock);
			setText((favorite != null) ? Messages.ToggleVerrechenbarFavoriteAction_DeFavorize
					: Messages.ToggleVerrechenbarFavoriteAction_Favorize);
			setImageDescriptor((favorite != null) ? Images.IMG_STAR.getImageDescriptor()
					: Images.IMG_STAR_EMPTY.getImageDescriptor());
		} else {
			setEnabled(false);
			currentBillable = null;
			currentBlock = null;
		}
	}
}
