package ch.elexis.core.mail.ui.preference;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import ch.elexis.core.ui.icons.Images;

public class TextTemplates extends PreferencePage implements IWorkbenchPreferencePage {
	
	private Composite parentComposite;
	private ComboViewer templatesViewer;
	
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
				

			}
		});
		
		ToolBar accountsTool = new ToolBar(parentComposite, SWT.NONE);
		
		ToolBarManager accountsToolMgr = new ToolBarManager(accountsTool);
		accountsToolMgr.add(new AddTextTemplateAction());
		accountsToolMgr.update(true);
		
		return parentComposite;
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
				new MailTextTemplate.Builder().mandator(dialog.getMandator()).name(dialog.getName())
					.buildAndSave();
				updateTemplatesCombo();
			}
		}
	}
}
