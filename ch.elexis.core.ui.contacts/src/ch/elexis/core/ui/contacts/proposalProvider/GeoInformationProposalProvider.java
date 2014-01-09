/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.proposalProvider;

import org.eclipse.jface.fieldassist.IContentProposalProvider;

import at.medevit.elexis.geonames.interfaces.IGeonameService;
import ch.elexis.core.types.CountryCode;
import ch.elexis.core.ui.contacts.geonamesconsumer.GeonamesConsumer;


public abstract class GeoInformationProposalProvider implements
		IContentProposalProvider {

	protected static CountryCode configuredCountry;
	protected static IGeonameService igs = GeonamesConsumer.getGeonameService();
	
	public CountryCode getCountry(){
		return configuredCountry;
	}	
	
	public void setCountry(CountryCode country){
		GeoInformationProposalProvider.configuredCountry = country;
		init(country);
	}
	
	public String getDialPrefix() {
		String dialPrefix = igs.getDialPrefixByCountry(configuredCountry);
		return (dialPrefix != null) ? dialPrefix : "";
	}
	
	public abstract void init(CountryCode country);

}
