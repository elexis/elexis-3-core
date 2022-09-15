package ch.elexis.core.ui.documents.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.core.findings.util.ValueSetServiceHolder;

public class ValueSetProposalProvider implements IContentProposalProvider {

	public static final String EPRDOCUMENT_CLASSCODE = "EprDocumentClassCode"; //$NON-NLS-1$
	public static final String EPRDOCUMENT_PRACTICESETTINGCODE = "EprDocumentPracticeSettingCode"; //$NON-NLS-1$

	private final String valueSetName;

	public ValueSetProposalProvider(String valueSetName) {
		this.valueSetName = valueSetName;
	}

	@Override
	public IContentProposal[] getProposals(String searchString, int position) {
		List<IContentProposal> ret = new ArrayList<IContentProposal>();
		if (searchString != null && !searchString.isEmpty()) {
			ret = ValueSetServiceHolder.getIValueSetService().getValueSetByName(valueSetName).stream()
					.filter(o -> o.getDisplay().toLowerCase().contains(searchString.trim().toLowerCase()))
					.map(o -> new CodingContentProposal(o.getDisplay(), o)).collect(Collectors.toList());
		}
		return ret.toArray(new IContentProposal[ret.size()]);
	}
}
