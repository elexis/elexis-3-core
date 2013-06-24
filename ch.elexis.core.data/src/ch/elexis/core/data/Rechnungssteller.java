/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.data;

import ch.elexis.core.constants.StringConstants;
import ch.rgw.tools.StringTool;

/**
 * This class is only needed to denote a person or organization that can make a bill. It is simply a
 * contact.
 * 
 * @author Gerry
 * 
 */
public class Rechnungssteller extends Kontakt {
	
	public static Rechnungssteller load(String id){
		return new Rechnungssteller(id);
	}
	
	protected Rechnungssteller(String id){
		super(id);
	}
	
	/**
	 * usually but not mandatory, the biller will be a user
	 */
	@Override
	public String getLabel(){
		if (get(FLD_IS_USER).equals(StringConstants.ONE)) {
			String l = get("Label");
			if (!StringTool.isNothing(l)) {
				return l;
			}
		}
		return super.getLabel();
	}
	
	protected Rechnungssteller(){}
}
