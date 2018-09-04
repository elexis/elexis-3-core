package ch.elexis.core.ui.editors;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class NumericCellEditorValidator implements ICellEditorValidator {
	
	@Override
	public String isValid(Object value){
		if(value instanceof String) {
			((String) value).replaceAll(",", ".");
			try {
				Float.parseFloat((String) value);
			} catch (NumberFormatException e) {
				return "Wert [" + value + "] ist keine Zahl";
			}
		}
		return null;
	}
}
