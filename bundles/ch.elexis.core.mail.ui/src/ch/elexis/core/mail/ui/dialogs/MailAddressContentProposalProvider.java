package ch.elexis.core.mail.ui.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.e4.fieldassist.IdentifiableContentProposal;

public class MailAddressContentProposalProvider implements IContentProposalProvider {

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		List<IContentProposal> ret = new ArrayList<IContentProposal>();
		HashSet<IContact> contacts = new HashSet<IContact>();

		String addressString = null;
		if (contents != null && !contents.isEmpty()) {
			addressString = getContentAddress(contents);
		}

		IPatient activePatient = ContextServiceHolder.get().getActivePatient().orElse(null);
		if (activePatient != null) {
			IQuery<IDocumentLetter> lastQuery = CoreModelServiceHolder.get().getQuery(IDocumentLetter.class);
			lastQuery.and(ModelPackage.Literals.IDOCUMENT__PATIENT, COMPARATOR.EQUALS, activePatient);
			lastQuery.and(ModelPackage.Literals.IDOCUMENT_LETTER__RECIPIENT, COMPARATOR.NOT_EQUALS, null);
			lastQuery.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
			for (IDocumentLetter document : lastQuery.execute()) {
				if (document.getRecipient() != null && StringUtils.isNotBlank(document.getRecipient().getEmail())) {
					String email = document.getRecipient().getEmail();
					if (addressString != null && addressString.length() > 1) {
						if (!(email.contains(addressString)
								|| document.getRecipient().getDescription1().contains(addressString)
								|| document.getRecipient().getDescription2().contains(addressString))) {
							continue;
						}
					}
					if (!contacts.contains(document.getRecipient())) {
						contacts.add(document.getRecipient());
						ret.add(new IdentifiableContentProposal<IContact>(
								email + " - " + document.getRecipient().getLabel(),
								document.getRecipient()));
					}
				}
			}
		}
		if (addressString != null && addressString.length() > 1) {
			IQuery<IContact> query = CoreModelServiceHolder.get().getQuery(IContact.class);
			query.and(ModelPackage.Literals.ICONTACT__EMAIL, COMPARATOR.LIKE, "%" + addressString + "%");
			query.startGroup();
			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%" + addressString + "%");
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, "%" + addressString + "%");
			query.orJoinGroups();
			query.and(ModelPackage.Literals.ICONTACT__EMAIL, COMPARATOR.NOT_EQUALS, null);
			query.and(ModelPackage.Literals.ICONTACT__EMAIL, COMPARATOR.NOT_EQUALS, StringUtils.EMPTY);

			for (IContact contact : query.execute()) {
				if (!contacts.contains(contact)) {
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
