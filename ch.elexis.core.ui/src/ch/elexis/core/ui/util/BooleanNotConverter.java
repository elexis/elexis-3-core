package ch.elexis.core.ui.util;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Converts boolean values to !boolean
 */
public class BooleanNotConverter extends Converter {
	
	public BooleanNotConverter(){
		super(Boolean.class, Boolean.class);
	}
	
	@Override
	public Object convert(Object fromObject){
		return !(Boolean) fromObject;
	}
}
