package ch.elexis.core.mail.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.e4.fieldassist.IdentifiableContentProposal;

public class MailAddressContentProposalProvider implements IContentProposalProvider {

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		List<IContentProposal> ret = new ArrayList<IContentProposal>();
		if (contents != null && !contents.isEmpty()) {
			String addressString = getContentAddress(contents);
			if (!addressString.isEmpty() && addressString.length() > 1) {
				IQuery<IContact> query = CoreModelServiceHolder.get().getQuery(IContact.class);
				query.and(ModelPackage.Literals.ICONTACT__EMAIL, COMPARATOR.LIKE, "%" + addressString + "%");
				query.startGroup();
				query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%" + addressString + "%");
				query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, "%" + addressString + "%");
				query.orJoinGroups();
				query.and(ModelPackage.Literals.ICONTACT__EMAIL, COMPARATOR.NOT_EQUALS, null);
				query.and(ModelPackage.Literals.ICONTACT__EMAIL, COMPARATOR.NOT_EQUALS, StringUtils.EMPTY);
				query.orderBy(ModelPackage.Literals.ICONTACT__EMAIL, ORDER.DESC);

				for (IContact contact : query.execute()) {
					ret.add(new IdentifiableContentProposal<IContact>(contact.getEmail() + " - " + contact.getLabel(),
							contact));
				}
			}
		}
		return ret.toArray(new IContentProposal[ret.size()]);
	}

	private String getContentAddress(String contents) {
		int index = getLastAddressIndex(contents);
		if (index == 0) {
			return contents;
		} else {
			return contents.substring(index + 1).trim();
		}
	}

	public static int getLastAddressIndex(String contents) {
		int index = contents.lastIndexOf(",");
		if (index == -1) {
			return 0;
		}
		return index;
	}
}
