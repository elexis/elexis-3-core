package ch.elexis.core.ui.views.codesystems;

import java.util.List;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.VerrechenbarFavorites;
import ch.elexis.data.VerrechenbarFavorites.Favorite;

public class BlockTreeViewerItem {
	
	public static BlockTreeViewerItem of(Leistungsblock block){
		BlockTreeViewerItem ret = new BlockTreeViewerItem(block);
		return ret;
	}
	
	private Leistungsblock block;
	private List<BlockElementViewerItem> currentChildren;
	
	public BlockTreeViewerItem(Leistungsblock block){
		this.block = block;
	}
	
	public String getText(){
		return block.getLabel();
	}
	
	public Leistungsblock getBlock(){
		return block;
	}
	
	public List<BlockElementViewerItem> getChildren(){
		currentChildren = BlockElementViewerItem.of(block, false);
		return currentChildren;
	}
	
	public boolean hasChildren(){
		// load children lazy in getChildren, loading Leistungsblock elements for current context is complex
		if (currentChildren != null) {
			return !currentChildren.isEmpty();
		}
		return true;
	}
	
	public static class ColorizedLabelProvider extends LabelProvider implements IColorProvider {
		
		private BlockElementViewerItem.ColorizedLabelProvider elementLabelProvider =
			new BlockElementViewerItem.ColorizedLabelProvider();
		
		@Override
		public String getText(final Object element){
			if (element instanceof BlockElementViewerItem) {
				BlockElementViewerItem item = (BlockElementViewerItem) element;
				return elementLabelProvider.getText(item);
			} else if (element instanceof BlockTreeViewerItem) {
				BlockTreeViewerItem item = (BlockTreeViewerItem) element;
				return item.getText();
			}
			return super.getText(element);
		}
		
		@Override
		public Image getImage(Object element){
			if (element instanceof BlockTreeViewerItem) {
				BlockTreeViewerItem item = (BlockTreeViewerItem) element;
				Favorite fav = VerrechenbarFavorites.isFavorite(item.getBlock());
				if (fav != null)
					return Images.IMG_STAR.getImage();
			}
			return null;
		}
		
		@Override
		public Color getForeground(Object element){
			return null;
		}
		
		@Override
		public Color getBackground(Object element){
			if (element instanceof BlockElementViewerItem) {
				BlockElementViewerItem item = (BlockElementViewerItem) element;
				return elementLabelProvider.getBackground(item);
			}
			return null;
		}
	}
}
