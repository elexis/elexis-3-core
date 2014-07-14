package ch.elexis.core.ui.proposals;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

public class PersistentObjectProposalProvider<T extends PersistentObject> implements
		IContentProposalProvider {
	
	private List<PersistentObjectContentProposal<T>> proposals =
		new LinkedList<PersistentObjectContentProposal<T>>();
	private Query<T> query;
	private List<T> queryResults = new LinkedList<T>();
	
	public PersistentObjectProposalProvider(Class<? extends PersistentObject> clazz){
		this(clazz, null, null, null);
	}
	
	/**
	 * Add an optional query filter to the content proposal
	 * 
	 * @param field
	 * @param operator
	 * @param value
	 */
	public PersistentObjectProposalProvider(Class<? extends PersistentObject> clazz, String field,
		String operator, String value){
		query = new Query<T>(clazz);
		if (field != null)
			query.add(field, operator, value);
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
				proposals.add(new PersistentObjectContentProposal<T>(label, a));
			}
		}
		
		return proposals.toArray(new ContentProposal[] {});
	}
	
	/**
	 * The label to add on the content proposal. Normally not required to be overriden. One example,
	 * however, is {@link Mandant} as the real label is gathered using
	 * {@link Mandant#getMandantLabel()} here.
	 * 
	 * @param a
	 * @return
	 */
	public String getLabelForObject(T a){
		return a.getLabel();
	}
}
