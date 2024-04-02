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
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.ui.PlatformUI;

public class StreetInformationProposalProvider implements IContentProposalProvider {

	private static List<String> streets;

	public IContentProposal[] getProposals(String contents, int position) {
		List<ContentProposal> cp = new LinkedList<>();
		for (int i = 0; i < streets.size(); i++) {
			String currStreet = streets.get(i);
			if (contents == null) {
				cp.add(new ContentProposal(currStreet));
			} else if (currStreet.toLowerCase().startsWith(contents.toLowerCase())) {
				cp.add(new ContentProposal(currStreet));
			}
		}

		return cp.toArray(new ContentProposal[] {});
	}

	public void setZip(final String zip) {
		// for speed resp. usability reasons, async load
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				streets = ContactGeonames.getStreetByZip(zip);
			}
		});
	}

}
