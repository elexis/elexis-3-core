package ch.elexis.core.ui.proposals;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IQuery;

public class IdentifiableProposalProvider<T extends Identifiable> implements IContentProposalProvider {

	private List<IdentifiableContentProposal<T>> proposals = new LinkedList<>();
	private IQuery<T> query;
	private List<T> queryResults = new LinkedList<>();

	private boolean allowNoContent = false;
	private boolean matchContained = false;

	public IdentifiableProposalProvider(IQuery<T> query) {
		this.query = query;
	}

	/**
	 * Allow proposals to be loaded if there is not contents string.
	 * 
	 */
	public void allowNoContent() {
		this.allowNoContent = true;
	}

	/**
	 * Match label of loaded proposals containing the contents string.
	 * 
	 */
	public void matchContained() {
		this.matchContained = true;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		if (!allowNoContent && StringUtils.isBlank(contents))
			return null;

		if (position == 1 || allowNoContent) {
			// refresh all available from database
			queryResults.clear();
			queryResults = query.execute();
		}

		proposals.clear();

		for (T a : queryResults) {
			String label = getLabelForObject(a);
			if (allowNoContent && StringUtils.isEmpty(contents)) {
				proposals.add(new IdentifiableContentProposal<T>(label, a));
			} else {
				if (matchContained) {
					if (label.toLowerCase().contains(contents.toLowerCase())) {
						proposals.add(new IdentifiableContentProposal<T>(label, a));
					}
				} else {
					if (label.toLowerCase().startsWith(contents.toLowerCase())) {
						proposals.add(new IdentifiableContentProposal<T>(label, a));
					}
				}
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
	public String getLabelForObject(T a) {
		return a.getLabel();
	}
}
