package ch.elexis.core.mail.ui.preference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.mail.MailConstants;
import ch.elexis.core.mail.MailTextTemplate;
import ch.elexis.core.mail.PreferenceConstants;
import ch.elexis.core.mail.ui.dialogs.TextTemplateDialog;
import ch.elexis.core.model.IBlobSecondary;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.TextTemplateComposite;

public class TextTemplates extends PreferencePage implements IWorkbenchPreferencePage {

	private Composite parentComposite;
	private ComboViewer templatesViewer;

	private TextTemplateComposite templateComposite;
	private Button defaultBtn, confidentialBtn;
	private List<ITextTemplate> list;
	private TableViewer tableViewer;

	public static final String NAMED_BLOB_PREFIX = "TEXTTEMPLATE_";

	private static Logger logger = LoggerFactory.getLogger(TextTemplates.class);

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

					confidentialBtn.setSelection(
							selectedTemplate.getExtInfo(MailConstants.CONFIDENTIAL_MAIL) instanceof Boolean b && b);

					if (defaultTemplateId != null && selectedTemplate.getId().equals(defaultTemplateId)) {
						defaultBtn.setSelection(true);
					} else {
						defaultBtn.setSelection(false);
					}
					refresh();
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

		Label attachmentTitle = new Label(parentComposite, SWT.NONE);
		attachmentTitle.setText("Anhänge");

		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		composite.setLayout(new GridLayout(2, false));

		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		Table table = tableViewer.getTable();
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		table.setLinesVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(list);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				int index = tableViewer.getTable().getSelectionIndex();

				IStructuredSelection selectedTextTemplate = templatesViewer.getStructuredSelection();
				ITextTemplate template = (ITextTemplate) selectedTextTemplate.getFirstElement();

				IBlobSecondary textTemplate = CoreModelServiceHolder.get()
						.load(NAMED_BLOB_PREFIX + template.getId(), IBlobSecondary.class).orElse(null);

				if (textTemplate != null) {
					byte[] attachmentData = textTemplate.getContent();
					try {
						List<SerializableFile> fileList = SerializableFileUtil.deserializeData(attachmentData);

						Path temp = Files.createTempFile(fileList.get(index).getName(),
								fileList.get(index).getMimeType());

						Files.write(temp, fileList.get(index).getData());
						Program.launch(temp.toString());
					} catch (IOException | ClassNotFoundException e) {
						MessageDialog.openError(getShell(), "Fehler", "Das Dokument kann nicht geladen werden.");
						logger.info("Error loading document", e);
					}
				}
			}
		});

		TableViewerColumn tvc_attachments = new TableViewerColumn(tableViewer, SWT.NONE);
		tvc_attachments.getColumn().setWidth(table.getClientArea().width);
		tvc_attachments.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				SerializableFile attachments = (SerializableFile) element;
				return attachments.getName();
			}

			@Override
			public String getToolTipText(Object element) {
				return "Mit Doppelklick öffnen (keine Änderungen)";
			}
		});

		table.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int tableWidth = table.getClientArea().width;
				tvc_attachments.getColumn().setWidth(tableWidth);
			}
		});

		ToolBar toolbar = new ToolBar(composite, SWT.NONE);
		ToolBarManager tbmAttachments = new ToolBarManager(toolbar);
		tbmAttachments.add(new AddAttachmentsAction());
		tbmAttachments.add(new DeleteAttachmentsAction());
		tbmAttachments.update(true);

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
							logger.info("Error importing praxislogo", e);
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

		confidentialBtn = new Button(buttonComposite, SWT.CHECK);
		confidentialBtn.setText("Vertraulich");
		confidentialBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (templatesViewer.getStructuredSelection().getFirstElement() instanceof ITextTemplate template) {
					template.setExtInfo(MailConstants.CONFIDENTIAL_MAIL, confidentialBtn.getSelection());
				}
			}
		});

		return parentComposite;
	}

	private void refresh() {
		IStructuredSelection selection = templatesViewer.getStructuredSelection();
		ITextTemplate template = (ITextTemplate) selection.getFirstElement();
		if (selection != null && template instanceof ITextTemplate) {

			IBlobSecondary textTemplate = CoreModelServiceHolder.get()
					.load(NAMED_BLOB_PREFIX + template.getId(), IBlobSecondary.class).orElse(null);

			if (textTemplate != null) {
				byte[] attachmentData = textTemplate.getContent();
				try {
					List<SerializableFile> fileList = SerializableFileUtil.deserializeData(attachmentData);
					tableViewer.setInput(fileList);
				} catch (IOException | ClassNotFoundException e) {
					MessageDialog.openError(getShell(), "Fehler", "Die Daten können nicht geladen werden.");
					logger.info("Error loading texttemplate content", e);
				}
			} else {
				tableViewer.setInput(null);
			}
		}
		tableViewer.refresh(true);
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
		public boolean isEnabled() {
			return AccessControlServiceHolder.get().evaluate(EvACE.of(ITextTemplate.class, Right.REMOVE));
		}

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

	private class AddAttachmentsAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_NEW.getImageDescriptor();
		}

		@Override
		public String getToolTipText() {
			return "Angang hinzufügen";
		}

		@Override
		public void run() {
			IStructuredSelection selection = templatesViewer.getStructuredSelection();
			ITextTemplate template = (ITextTemplate) selection.getFirstElement();
			if (selection != null && template instanceof ITextTemplate) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
				fileDialog.setFilterExtensions(new String[] { "*.pdf" });
				String document = fileDialog.open();

				if (document != null) {
					try {
						String id = NAMED_BLOB_PREFIX + template.getId();
						IBlobSecondary textTemplate = CoreModelServiceHolder.get().load(id, IBlobSecondary.class)
								.orElse(null);
						if (textTemplate == null) {
							textTemplate = CoreModelServiceHolder.get().create(IBlobSecondary.class);
							textTemplate.setId(id);
						}

						byte[] bytes = Files.readAllBytes(Paths.get(document));
						ArrayList<SerializableFile> fileList = new ArrayList<>();
						String mimeType = fileDialog.getFileName().substring(fileDialog.getFileName().lastIndexOf("."));
						fileList.add(new SerializableFile(fileDialog.getFileName(), mimeType, bytes));

						if (textTemplate.getContent() != null) {
							byte[] DBArrayList = textTemplate.getContent();
							List<SerializableFile> deserializedContent = SerializableFileUtil
									.deserializeData(DBArrayList);

							for (SerializableFile serializableFile : deserializedContent) {
								fileList.add(new SerializableFile(serializableFile.getName(),
										serializableFile.getMimeType(), serializableFile.getData()));
							}
						}

						byte[] data = SerializableFileUtil.serializeData(fileList);
						textTemplate.setContent(data);

						CoreModelServiceHolder.get().save(textTemplate);
					} catch (IOException | ClassNotFoundException e) {
						MessageDialog.openError(getShell(), "Fehler", "Das Dokument kann nicht hinzugefügt werden.");
						logger.info("Error saving document", e);
					}
				}
			}
			refresh();
			this.setEnabled(true);
		}

	}

	private class DeleteAttachmentsAction extends Action {
		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_DELETE.getImageDescriptor();
		}

		@Override
		public String getToolTipText() {
			return "Anhang entfernen";
		}

		@Override
		public void run() {
			IStructuredSelection selection = templatesViewer.getStructuredSelection();
			ITextTemplate template = (ITextTemplate) selection.getFirstElement();
			int index = tableViewer.getTable().getSelectionIndex();

			String id = NAMED_BLOB_PREFIX + template.getId();
			IBlobSecondary textTemplate = CoreModelServiceHolder.get().load(id, IBlobSecondary.class).orElse(null);

			if (textTemplate != null) {
				byte[] attachmentData = textTemplate.getContent();
				try {
					ArrayList<SerializableFile> fileList = (ArrayList<SerializableFile>) SerializableFileUtil
							.deserializeData(attachmentData);

					if (index >= 0 && index < fileList.size()) {
						fileList.remove(index);
						byte[] serializedData = SerializableFileUtil.serializeData(fileList);
						textTemplate.setContent(serializedData);
						CoreModelServiceHolder.get().save(textTemplate);
					}
				} catch (IOException | ClassNotFoundException e) {
					MessageDialog.openError(getShell(), "Fehler", "Das Dokument kann nicht entfernt werden.");
					logger.info("Error deleting document", e);
				}
			}

			refresh();
			this.setEnabled(true);
		}

	}
}
