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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;

import ch.elexis.core.types.CountryCode;


public class CityInformationProposalProvider extends GeoInformationProposalProvider {
	
	private static List<String[]> cities;
	
	@Override
	public void init(CountryCode country){
		cities = igs.getLabeledCitiesByCountry(country);
		//System.out.println("Initialized CityInformationProposalProvider");
	}
	
	@Override
	public IContentProposal[] getProposals(String contents, int position){
		//System.out.println("getProposals()");
		List<ContentProposal> cp = new LinkedList<ContentProposal>();
		for (int i = 0; i < cities.size(); i++) {
			String[] currCity = cities.get(i);
			if (contents == null)
				cp.add(new ContentProposal(currCity[0], currCity[0] + " (" + currCity[1] + ")",
					null));
			if (currCity[0].toLowerCase().startsWith(contents.toLowerCase()))
				cp.add(new ContentProposal(currCity[0], currCity[0] + " (" + currCity[1] + ")",
					null));
		}
		
		return cp.toArray(new ContentProposal[] {});
	}
	
	public String findZipForCityName(String content){
		List<String> result = igs.getZipByCityAndCountry(content, configuredCountry);
		if (result.size() >= 1)
			return result.get(0);
		return "";
	}
	
}
