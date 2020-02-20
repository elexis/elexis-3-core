package ch.elexis.core.ui.documents.provider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.core.findings.util.ValueSetServiceHolder;

public class DocumentClassProposalProvider implements IContentProposalProvider {
	
	@Override
	public IContentProposal[] getProposals(String searchString, int position){
		List<IContentProposal> ret = new ArrayList<IContentProposal>();
		if (searchString != null && !searchString.isEmpty()) {
			return ValueSetServiceHolder.getIValueSetService()
				.getValueSetByName("EprDocumentClassCode").stream()
				.filter(o -> o != null && o.getDisplay().startsWith(searchString))
				.map(o -> new CodingContentProposal(o))
				.toArray(ContentProposal[]::new);
			
		}
		return ret.toArray(new IContentProposal[ret.size()]);
	}
}
