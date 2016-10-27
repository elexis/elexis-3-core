package ch.elexis.core.findings.ui.composites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ui.services.CodingServiceComponent;

public class CodingContentProposalProvider implements IContentProposalProvider {
	
	private Optional<String> selectedSystem;
	
	private HashMap<String, ICoding> labelToCoding = new HashMap<>();
	
	@Override
	public IContentProposal[] getProposals(String contents, int position){
		List<IContentProposal> ret = new ArrayList<IContentProposal>();
		if (contents != null && !contents.isEmpty()) {
			labelToCoding.keySet().parallelStream().forEach(label -> {
				String match = contents.toLowerCase();
				if (label.toLowerCase().contains(match)) {
					ret.add(new ContentProposal(label));
				}
			});
		}
		return ret.toArray(new IContentProposal[ret.size()]);
	}
	
	public String toLabel(ICoding coding){
		return "[" + coding.getCode() + "] " + coding.getDisplay();
	}
	
	public Optional<ICoding> fromLabel(String label){
		return Optional.ofNullable(labelToCoding.get(label));
	}
	
	public void setSelectedSystem(Optional<String> selectedSystem){
		this.selectedSystem = selectedSystem;
		clearCache();
		buildCache();
	}
	
	private void clearCache(){
		labelToCoding.clear();
	}
	
	private void buildCache(){
		if (selectedSystem.isPresent()) {
			List<ICoding> codes =
				CodingServiceComponent.getService().getAvailableCodes(selectedSystem.get());
			codes.parallelStream().forEach(iCoding -> {
				String label = toLabel(iCoding);
				labelToCoding.put(label, iCoding);
			});
		}
	}
	
	public Optional<ICoding> getCodingForProposal(IContentProposal proposal){
		String label = proposal.getContent();
		if (label != null && !label.isEmpty()) {
			return fromLabel(label);
		}
		return Optional.empty();
	}
}
