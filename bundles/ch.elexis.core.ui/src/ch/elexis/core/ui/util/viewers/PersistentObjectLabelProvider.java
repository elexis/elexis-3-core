package ch.elexis.core.ui.util.viewers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.data.PersistentObject;

/**
 * 
 * @since 3.7
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PersistentObjectLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	private static PersistentObjectLabelProvider instance;
	
	/**
	 * Returns an instance of PersistentObjectLabelProvider. Since instances of this class do not
	 * maintain any state, they can be shared between multiple clients.
	 *
	 * @return an instance of PersistentObjectLabelProvider
	 *
	 * @since 3.7
	 */
	public static PersistentObjectLabelProvider getInstance(){
		synchronized (PersistentObjectLabelProvider.class) {
			if (instance == null) {
				instance = new PersistentObjectLabelProvider();
			}
			return instance;
		}
	}
	
	@Override
	public String getText(Object element){
		if (element != null) {
			return ((PersistentObject) element).getLabel();
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
