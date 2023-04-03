package ch.elexis.core.mail.ui.preference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.MailTextTemplate;
import ch.elexis.core.mail.PreferenceConstants;
import ch.elexis.core.mail.ui.dialogs.TextTemplateDialog;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.TextTemplateComposite;

public class TextTemplates extends PreferencePage implements IWorkbenchPreferencePage {

	private Composite parentComposite;
	private ComboViewer templatesViewer;

	private TextTemplateComposite templateComposite;
	private Button defaultBtn;

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(2, false));

		templatesViewer = new ComboViewer(parentComposite);
		templatesViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		templatesViewer.setContentProvider(new ArrayContentProvider());
		templatesViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ITextTemplate) {
					return ((ITextTemplate) element).getName() + (((ITextTemplate) element).getMandator() != null
							? " (" + ((ITextTemplate) element).getMandator().getLabel() + ")"
							: StringUtils.EMPTY);
				}
				return super.getText(element);
			}
		});
		updateTemplatesCombo();
		templatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getStructuredSelection() != null
						&& event.getStructuredSelection().getFirstElement() instanceof ITextTemplate) {
					templateComposite.save();
					ITextTemplate selectedTemplate = (ITextTemplate) event.getStructuredSelection().getFirstElement();
					templateComposite.setTemplate(selectedTemplate);
					String defaultTemplateId = ConfigServiceHolder.get().get(PreferenceConstants.PREF_DEFAULT_TEMPLATE,
							null);
					if (defaultTemplateId != null && selectedTemplate.getId().equals(defaultTemplateId)) {
						defaultBtn.setSelection(true);
					} else {
						defaultBtn.setSelection(false);
					}
				} else {
					templateComposite.setTemplate(null);
					defaultBtn.setSelection(false);
				}
			}
		});

		ToolBar accountsTool = new ToolBar(parentComposite, SWT.NONE);

		ToolBarManager accountsToolMgr = new ToolBarManager(accountsTool);
		accountsToolMgr.add(new AddTextTemplateAction());
		accountsToolMgr.add(new RemoveTextTemplateAction());
		accountsToolMgr.update(true);

		templateComposite = new TextTemplateComposite(parentComposite, SWT.NONE);
		templateComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		Composite buttonComposite = new Composite(parentComposite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		buttonComposite.setLayout(new RowLayout());

		Button logoBtn = new Button(buttonComposite, SWT.PUSH);
		if (loadImage("elexismailpraxislogo").isPresent()) {
			logoBtn.setText("Praxis Logo überschreiben");
		} else {
			logoBtn.setText("Praxis Logo setzen");
		}
		logoBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell());
				String filename = dialog.open();
				if (filename != null) {
					File file = new File(filename);
					if (file.exists()) {
						IImage image = loadImage("elexismailpraxislogo").orElse(createImage());
						try (FileInputStream in = new FileInputStream(file);
								ByteArrayOutputStream out = new ByteArrayOutputStream()) {
							IOUtils.copy(in, out);
							out.flush();
							image.setImage(out.toByteArray());
							image.setTitle("elexismailpraxislogo." + FilenameUtils.getExtension(file.getName()));
							CoreModelServiceHolder.get().save(image);
							logoBtn.setText("Praxis Logo überschreiben");
						} catch (IOException e1) {
							MessageDialog.openError(getShell(), "Fehler",
									"Die Datei konnte nicht als Praxis Logo importiert werden.");
							LoggerFactory.getLogger(getClass()).error("Error importing praxislogo", e);
						}
					}
				}
			}

		});

		defaultBtn = new Button(buttonComposite, SWT.CHECK);
		defaultBtn.setText("Als Standard Vorlage verwenden");
		defaultBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selectedTemplate = templatesViewer.getStructuredSelection();
				if (selectedTemplate != null && selectedTemplate.getFirstElement() instanceof ITextTemplate)
					ConfigServiceHolder.get().set(PreferenceConstants.PREF_DEFAULT_TEMPLATE,
							((ITextTemplate) selectedTemplate.getFirstElement()).getId());
			}
		});

		return parentComposite;
	}

	private IImage createImage() {
		IImage image = CoreModelServiceHolder.get().create(IImage.class);
		image.setPrefix("ch.elexis.core.mail");
		return image;
	}

	private Optional<IImage> loadImage(String imageName) {
		IQuery<IImage> query = CoreModelServiceHolder.get().getQuery(IImage.class);
		query.and("prefix", COMPARATOR.EQUALS, "ch.elexis.core.mail");
		query.and("title", COMPARATOR.LIKE, imageName + "%");
		return query.executeSingleResult();
	}

	@Override
	public boolean performOk() {
		templateComposite.save();
		return super.performOk();
	}

	private void updateTemplatesCombo() {
		templatesViewer.setInput(MailTextTemplate.load());
		templatesViewer.refresh();
	}

	private class AddTextTemplateAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_NEW.getImageDescriptor();
		}

		@Override
		public void run() {
			TextTemplateDialog dialog = new TextTemplateDialog(getShell());
			if (dialog.open() == Window.OK) {
				ITextTemplate template = new MailTextTemplate.Builder().mandator(dialog.getMandator())
						.name(dialog.getName()).buildAndSave();
				updateTemplatesCombo();
				templatesViewer.setSelection(new StructuredSelection(template));
			}
		}
	}

	private class RemoveTextTemplateAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_DELETE.getImageDescriptor();
		}

		@Override
		public void run() {
			IStructuredSelection selection = templatesViewer.getStructuredSelection();
			if (selection != null && selection.getFirstElement() instanceof ITextTemplate) {
				templatesViewer.setSelection(new StructuredSelection());
				CoreModelServiceHolder.get().remove((ITextTemplate) selection.getFirstElement());
				updateTemplatesCombo();
			}
		}
	}
}
