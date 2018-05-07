package ch.elexis.core.ui.dialogs.provider;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.interfaces.ILocalizedEnum;

public class ILocalizedEnumLabelProvider extends LabelProvider {
	
	private static ILocalizedEnumLabelProvider instance;
	
	@Override
	public String getText(Object element){
		ILocalizedEnum ile = (ILocalizedEnum) element;
		return ile.getLocaleText();
	}
	
	public static IBaseLabelProvider getInstance(){
		synchronized (LabelProvider.class) {
			if (instance == null) {
				instance = new ILocalizedEnumLabelProvider();
			}
			return instance;
		}
	}
}
