/*******************************************************************************
 * Copyright (c) 2011-2017 Medevit OG, Medelexis AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package ch.elexis.core.data.propertyTester;

import org.eclipse.core.expressions.PropertyTester;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.ILocalLockService.Status;

public class ElexisPropertyTester extends PropertyTester {
	
	private static final String STANDALONE = "STANDALONE";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if (property != null)
		{
			switch (property)
			{
				case STANDALONE:
				{
					return !Status.STANDALONE.equals(CoreHub.getLocalLockService().getStatus());
				}
				default:
				{
					// empty
				}
			}
		}
		return false;
	}
	
}
