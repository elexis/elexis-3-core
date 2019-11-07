package ch.elexis.core.ui.util.viewers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.Identifiable;

/**
 * @since 3.8
 * @noextend This class is not intended to be subclassed by clients.
 */
public class IdentifiableLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	private static IdentifiableLabelProvider instance;
	
	/**
	 * Returns an instance of {@link IdentifiableLabelProvider}. Since instances of this class do
	 * not maintain any state, they can be shared between multiple clients.
	 *
	 * @return an instance of IdentifiableLabelProvider
	 *
	 * @since 3.8
	 */
	public static IdentifiableLabelProvider getInstance(){
		synchronized (IdentifiableLabelProvider.class) {
			if (instance == null) {
				instance = new IdentifiableLabelProvider();
			}
			return instance;
		}
	}
	
	@Override
	public String getText(Object element){
		if (element != null) {
			return ((Identifiable) element).getLabel();
		}
		return "null";
	}
	
	@Override
	public Image getColumnImage(Object arg0, int arg1){
		return null;
	}
	
	@Override
	public String getColumnText(Object arg0, int arg1){
		return getText(arg0);
	}
	
}
