package ch.elexis.core.ui.documents.util;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.documents.provider.CodingContentProposal;
import ch.elexis.core.ui.proposals.IdentifiableContentProposal;

public class AutoCompleteTextUtil {
	private static final String PROPOSAL_RET_OBJ = "PROPOSAL_RET_OBJ";
	
	public static <T> void addAutoCompleteSupport(Text text, IContentProposalProvider cpProvider,
		T defaultObject){
		setValue(text, defaultObject);
		
		ContentProposalAdapter cpAdapter =
			new ContentProposalAdapter(text, new TextContentAdapter(), cpProvider, null, null);
		cpAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		cpAdapter.addContentProposalListener(new IContentProposalListener() {
			
			@Override
			public void proposalAccepted(IContentProposal proposal){
				text.setText(proposal.getLabel());
				text.setData(PROPOSAL_RET_OBJ, getProposalObject(proposal));
				text.setSelection(text.getText().length());
			}
		});
		text.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e){
				// resets the contents after manual change
				text.setData(PROPOSAL_RET_OBJ, null);
			}
		});
	}
	
	public static Object getData(Text text){
		return text.getData(PROPOSAL_RET_OBJ);
	}
	
	public static <T> void setValue(Text text, T defaultObject){
		if (defaultObject instanceof ICoding) {
			text.setText(((ICoding) defaultObject).getDisplay());
		} else if (defaultObject instanceof Identifiable) {
			text.setText(((Identifiable) defaultObject).getLabel());
		} else if (defaultObject != null) {
			text.setText(String.valueOf(defaultObject));
		}
		text.setData(PROPOSAL_RET_OBJ, defaultObject);
	}
	
	private static Object getProposalObject(IContentProposal proposal){
		if (proposal instanceof CodingContentProposal) {
			return ((CodingContentProposal) proposal).getCoding();
		} else if (proposal instanceof IdentifiableContentProposal<?>) {
			return ((IdentifiableContentProposal<?>) proposal).getIdentifiable();
		}
		return proposal.getContent();
	}
}
