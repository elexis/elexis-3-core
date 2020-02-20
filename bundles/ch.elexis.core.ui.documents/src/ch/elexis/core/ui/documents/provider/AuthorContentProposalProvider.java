package ch.elexis.core.ui.documents.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.proposals.IdentifiableContentProposal;

public class AuthorContentProposalProvider implements IContentProposalProvider {
	
	@Override
	public IContentProposal[] getProposals(String searchString, int position){
		List<IContentProposal> ret = new ArrayList<IContentProposal>();
		if (searchString != null && !searchString.isEmpty()) {
			if (searchString.length() > 1) {
				int firstSpace = searchString.indexOf(" ");
				
				IQuery<IContact> query = CoreModelServiceHolder.get().getQuery(IContact.class);
				query.and(
					ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE,
					(firstSpace != -1 ? searchString.substring(0, firstSpace) : searchString)
						+ "%");
				query.limit(500);
				List<IContact> res = query.execute();
				
				// label filter
				if (firstSpace != -1) {
					res = res.stream()
						.filter(
							i -> i.getLabel().toLowerCase().startsWith(searchString.toLowerCase()))
						.collect(Collectors.toList());
				}
				
				for (IContact contact : res) {
					ret.add(new IdentifiableContentProposal<IContact>(contact.getLabel(), contact));
				}
			}
		}
		return ret.toArray(new IContentProposal[ret.size()]);
	}
}
