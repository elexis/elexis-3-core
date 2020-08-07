package ch.elexis.core.mail.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.icons.Images;

public class RemoveAttachmentAction extends Action implements IAction {
	
	private AttachmentsComposite composite;
	
	public RemoveAttachmentAction(AttachmentsComposite attachmentsComposite){
		this.composite = attachmentsComposite;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(){
		return Images.IMG_DELETE.getImageDescriptor();
	}
	
	@Override
	public void run(){
		if (StringUtils.isNotEmpty(composite.getDocuments())) {
			List<IDocument> documents = getAsDocuments(composite.getDocuments());
			ListSelectionDialog dialog =
				new ListSelectionDialog(Display.getDefault().getActiveShell(), documents,
					ArrayContentProvider.getInstance(), new LabelProvider() {
						public String getText(Object element){
							if (element instanceof IDocument) {
								return ((IDocument) element).getLabel();
								
							}
							return super.getText(element);
						};
					}, "Selektion der zu entfernenden Anhänge.");
			if (dialog.open() == Window.OK) {
				List<String> documentsParts =
					new ArrayList<>(Arrays.asList(composite.getDocuments().split(":::")));
				
				Object[] selected = dialog.getResult();
				if (selected != null && selected.length > 0) {
					for (Object object : selected) {
						if (object instanceof IDocument) {
							documentsParts.remove(StoreToStringServiceHolder.get()
								.storeToString((Identifiable) object).orElse(""));
						}
					}
					composite.setDocuments(
						documentsParts.stream().collect(Collectors.joining(":::")));
				}
			}
		}
	}
	
	private List<IDocument> getAsDocuments(String documents){
		String[] parts = documents.split(":::");
		List<IDocument> ret = new ArrayList<IDocument>();
		for (String string : parts) {
			Optional<Identifiable> loaded = StoreToStringServiceHolder.get().loadFromString(string);
			if (loaded.isPresent() && loaded.get() instanceof IDocument) {
				ret.add((IDocument) loaded.get());
			}
		}
		return ret;
	}
}
