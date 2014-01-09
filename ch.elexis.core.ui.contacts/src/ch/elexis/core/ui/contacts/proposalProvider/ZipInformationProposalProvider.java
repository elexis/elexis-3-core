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

public class ZipInformationProposalProvider extends GeoInformationProposalProvider {
	
	private static List<String> zips;
	
	@Override
	public void init(CountryCode country) {
		zips = igs.getZipByCountry(country);
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position){
		List<ContentProposal> cp = new LinkedList<ContentProposal>();
		for (int i = 0; i < zips.size(); i++) {
			String currZip = zips.get(i);
			if (contents == null)
				cp.add(new ContentProposal(currZip));
			if (currZip.startsWith(contents))
				cp.add(new ContentProposal(currZip));
		}
		
		return cp.toArray(new ContentProposal[] {});
	}
	
	public String findCityNameForZip(String content){
		List<String> result = igs.getCityByZipAndCountry(content, configuredCountry);
		if (result.size() >= 1)
			return result.get(0);
		return "";
	}
	
}
