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

public class TitleProposalProvider implements IContentProposalProvider {
	
	public static final int TITLE_POSITION_PREFIX = 1;
	public static final int TITLE_POSITION_SUFFIX = 2;

	public static final String[] titlePrefix = {
		"Dr.", "Dr. med.", "Prof.", "Dipl.-Ing.", "Dipl.-Ing. (FH)", "Mag.", "Mag. (FH)", "Ing."
	};
	public static final String[] titleSuffix = {
		"MBA", "MSc", "B.Sc.", "B.A.", "LLC", "FESC"
	};
	public static List<IContentProposal> titleProposalPrefix;
	public static List<IContentProposal> titleProposalSuffix;
	
	int titlePosition;
	
	public TitleProposalProvider(int titlePosition){
		this.titlePosition = titlePosition;
		initContentProposals();
	}
	
	@Override
	public IContentProposal[] getProposals(String contents, int position){
		switch (titlePosition) {
		case TITLE_POSITION_PREFIX:
			return titleProposalPrefix.toArray(new ContentProposal[] {});
		case TITLE_POSITION_SUFFIX:
			return titleProposalSuffix.toArray(new ContentProposal[] {});
		default:
			return null;
		}
	}
	
	private void initContentProposals(){
		titleProposalPrefix = new LinkedList<IContentProposal>();
		for (int i = 0; i < titlePrefix.length; i++) {
			titleProposalPrefix.add(new ContentProposal(titlePrefix[i]));
		}
		titleProposalSuffix = new LinkedList<IContentProposal>();
		for (int i = 0; i < titleSuffix.length; i++) {
			titleProposalSuffix.add(new ContentProposal(titleSuffix[i]));
		}
	}
	
}
