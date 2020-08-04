package ch.elexis.core.mail.ui.preference;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.mail.MailTextTemplate;
import ch.elexis.core.mail.ui.dialogs.TextTemplateDialog;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.TextTemplateComposite;

public class TextTemplates extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Composite parentComposite;
	private ComboViewer templatesViewer;
	
	private TextTemplateComposite templateComposite;
	
	@Override
	public void init(IWorkbench workbench){
		
	}
	
	@Override
	protected Control createContents(Composite parent){
		parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(2, false));
		
		templatesViewer = new ComboViewer(parentComposite);
		templatesViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		templatesViewer.setContentProvider(new ArrayContentProvider());
		templatesViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof ITextTemplate) {
					return ((ITextTemplate) element).getName()
						+ (((ITextTemplate) element).getMandator() != null
								? " (" + ((ITextTemplate) element).getMandator().getLabel() + ")"
								: "");
				}
				return super.getText(element);
			}
		});
		updateTemplatesCombo();
		templatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getStructuredSelection() != null
					&& event.getStructuredSelection().getFirstElement() instanceof ITextTemplate) {
					templateComposite.save();
					templateComposite.setTemplate(
						(ITextTemplate) event.getStructuredSelection().getFirstElement());
				} else {
					templateComposite.setTemplate(null);
				}
			}
		});
		
		ToolBar accountsTool = new ToolBar(parentComposite, SWT.NONE);
		
		ToolBarManager accountsToolMgr = new ToolBarManager(accountsTool);
		accountsToolMgr.add(new AddTextTemplateAction());
		accountsToolMgr.add(new RemoveTextTemplateAction());
		accountsToolMgr.update(true);
		
		templateComposite = new TextTemplateComposite(parentComposite, SWT.NONE);
		templateComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		

		return parentComposite;
	}
	
	@Override
	public boolean performOk(){
		templateComposite.save();
		return super.performOk();
	}
	
	private void updateTemplatesCombo(){
		templatesViewer.setInput(MailTextTemplate.load());
		templatesViewer.refresh();
	}
	
	private class AddTextTemplateAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_NEW.getImageDescriptor();
		}
		
		@Override
		public void run(){
			TextTemplateDialog dialog = new TextTemplateDialog(getShell());
			if (dialog.open() == Window.OK) {
				ITextTemplate template = new MailTextTemplate.Builder()
					.mandator(dialog.getMandator()).name(dialog.getName())
					.buildAndSave();
				updateTemplatesCombo();
				templatesViewer.setSelection(new StructuredSelection(template));
			}
		}
	}
	
	private class RemoveTextTemplateAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_DELETE.getImageDescriptor();
		}
		
		@Override
		public void run(){
			IStructuredSelection selection = templatesViewer.getStructuredSelection();
			if (selection != null && selection.getFirstElement() instanceof ITextTemplate) {
				templatesViewer.setSelection(new StructuredSelection());
				CoreModelServiceHolder.get().remove((ITextTemplate) selection.getFirstElement());
				updateTemplatesCombo();
			}
		}
	}
}
