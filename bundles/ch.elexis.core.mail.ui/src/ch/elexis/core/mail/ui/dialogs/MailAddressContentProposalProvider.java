package ch.elexis.core.mail.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.data.Kontakt;
import ch.elexis.data.Query;

public class MailAddressContentProposalProvider implements IContentProposalProvider {
	
	@Override
	public IContentProposal[] getProposals(String contents, int position){
		List<IContentProposal> ret = new ArrayList<IContentProposal>();
		if (contents != null && !contents.isEmpty()) {
			String addressString = getContentAddress(contents);
			if (!addressString.isEmpty()) {
				Query<Kontakt> query = new Query<Kontakt>(Kontakt.class);
				query.add(Kontakt.FLD_E_MAIL, Query.LIKE, addressString + "%");
				List<Kontakt> contacts = query.execute();
				
				for (Kontakt contact : contacts) {
					String mailAddress = contact.getMailAddress();
					ret.add(new ContentProposal(mailAddress));
				}
			}
		}
		return ret.toArray(new IContentProposal[ret.size()]);
	}
	
	private String getContentAddress(String contents){
		int index = getLastAddressIndex(contents);
		if (index == 0) {
			return contents;
		} else {
			return contents.substring(index + 1).trim();
		}
	}
	
	public static int getLastAddressIndex(String contents){
		int index = contents.lastIndexOf(",");
		if (index == -1) {
			return 0;
		}
		return index;
	}
}
