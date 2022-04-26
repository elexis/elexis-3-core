package ch.elexis.core.ui.text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.PlaceholderAttribute;
import ch.elexis.core.ui.util.CoreUiUtil;

public class TextTemplateComposite extends Composite {

	@Inject
	private ITextReplacementService replacementService;

	private Text replacementProposals;

	private StyledText templateText;

	private ITextTemplate template;

	public TextTemplateComposite(Composite parent, int style) {
		super(parent, style);
		CoreUiUtil.injectServices(this);
		createContent();
	}

	private void createContent() {
		setLayout(new GridLayout());
		replacementProposals = new Text(this, SWT.BORDER);
		replacementProposals.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ContentProposalAdapter toAddressProposalAdapter = new ContentProposalAdapter(replacementProposals,
				new TextContentAdapter(), new ReplacementProposalProvider(), null, null);
		toAddressProposalAdapter.addContentProposalListener(new IContentProposalListener() {
			@Override
			public void proposalAccepted(IContentProposal proposal) {
				boolean insertSpace = false;
				if (templateText.getCaretOffset() > 0) {
					String beforeChar = templateText.getText(templateText.getCaretOffset() - 1,
							templateText.getCaretOffset() - 1);
					insertSpace = !(beforeChar.equals(" ") || beforeChar.equals("\n"));
				}
				String insertText = (insertSpace ? " [" : "[") + proposal.getContent() + "]";
				templateText.insert(insertText);
				templateText.setCaretOffset(templateText.getCaretOffset() + insertText.length());
				replacementProposals.setText("");
			}
		});
		replacementProposals.setMessage("Platzhalter Suche und Auswahl");

		templateText = new StyledText(this, SWT.MULTI | SWT.BORDER);
		templateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * Set the {@link ITextTemplate} that will be updated {@link #updateModel()} or
	 * updated and saved {@link #save()}. saved
	 *
	 * @param template
	 */
	public void setTemplate(ITextTemplate template) {
		this.template = template;
		if (template != null && template.getTemplate() != null) {
			templateText.setText(template.getTemplate());
			return;
		}
		templateText.setText("");
	}

	/**
	 * Update the template text.
	 *
	 */
	public void updateModel() {
		if (template != null) {
			template.setTemplate(templateText.getText());
		}
	}

	/**
	 * Update the template text and save the change.
	 *
	 */
	public void save() {
		if (template != null) {
			updateModel();
			CoreModelServiceHolder.get().save(template);
		}
	}

	private class ReplacementProposalProvider implements IContentProposalProvider {

		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			contents = contents.toLowerCase();
			List<IContentProposal> proposals = new ArrayList<>();
			for (ITextPlaceholderResolver resolver : replacementService.getResolvers()) {
				List<PlaceholderAttribute> attributes = resolver.getSupportedAttributes();
				for (PlaceholderAttribute attribute : attributes) {
					String proposalText = attribute.getTypeName() + "." + attribute.getAttributeName();
					if (proposalText.toLowerCase().contains(contents)) {
						proposals.add(new ContentProposal(proposalText));
					}
				}
			}
			proposals.sort(new Comparator<IContentProposal>() {
				@Override
				public int compare(IContentProposal arg0, IContentProposal arg1) {
					return arg0.getLabel().compareTo(arg1.getLabel());
				}
			});
			return proposals.toArray(new IContentProposal[proposals.size()]);
		}
	}
}
