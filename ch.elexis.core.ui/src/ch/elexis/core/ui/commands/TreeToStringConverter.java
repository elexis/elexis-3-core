/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.commands;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

import ch.rgw.tools.StringTool;
import ch.rgw.tools.Tree;

public class TreeToStringConverter extends AbstractParameterValueConverter {
	static final HashMap<String, Tree<?>> map = new HashMap<String, Tree<?>>();
	
	@Override
	public Object convertToObject(String parameterValue) throws ParameterValueConversionException{
		Tree<?> ret = map.get(parameterValue);
		return ret;
	}
	
	@Override
	public String convertToString(Object parameterValue) throws ParameterValueConversionException{
		if (parameterValue instanceof Tree) {
			String ret = StringTool.unique(getClass().getName());
			map.put(ret, (Tree<?>) parameterValue);
			return ret;
		}
		throw new ParameterValueConversionException("Parameter was not instance of Tree");
	}
	
}
