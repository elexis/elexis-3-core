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

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.PersistentObject;

public class PersistentObjectConverter extends AbstractParameterValueConverter {
	
	@Override
	public Object convertToObject(String parameterValue) throws ParameterValueConversionException{
		return CoreHub.poFactory.createFromString(parameterValue);
	}
	
	@Override
	public String convertToString(Object parameterValue) throws ParameterValueConversionException{
		return ((PersistentObject) parameterValue).storeToString();
	}
	
}
