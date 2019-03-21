package ch.elexis.core.ui.proposals;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IQuery;

public class IdentifiableProposalProvider<T extends Identifiable>
		implements IContentProposalProvider {
	
	private List<IdentifiableContentProposal<T>> proposals =
		new LinkedList<IdentifiableContentProposal<T>>();
	private IQuery<T> query;
	private List<T> queryResults = new LinkedList<T>();
	
	public IdentifiableProposalProvider(IQuery<T> query){
		this.query = query;
	}
	
	@Override
	public IContentProposal[] getProposals(String contents, int position){
		if (contents == null || contents.length() < 1)
			return null;
		
		if (position == 1) {
			// refresh all available from database
			queryResults.clear();
			queryResults = query.execute();
		}
		
		proposals.clear();
		
		for (T a : queryResults) {
			String label = getLabelForObject(a);
			if (label.toLowerCase().startsWith(contents.toLowerCase())) {
				proposals.add(new IdentifiableContentProposal<T>(label, a));
			}
		}
		
		return proposals.toArray(new ContentProposal[] {});
	}
	
	/**
	 * The label to add on the content proposal.
	 * 
	 * @param a
	 * @return
	 */
	public String getLabelForObject(T a){
		return a.getLabel();
	}
}
