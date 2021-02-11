package ch.elexis.core.mail.ui.preference;

import java.util.ArrayList;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;

public class MailAccountComposite extends Composite {

	private WritableValue<MailAccount> value;
	private DataBindingContext context;
	
	private Label fromAddressLabel;
	private Text fromAddress;
	private WritableList<Mandant> mandantInput;
	private Text txtPort;
	private Text txtHost;
	private Text txtPassword;
	private Text txtUsername;
	private Text txtId;
	
	public MailAccountComposite(Composite parent, int style){
		super(parent, style);
		
		createContent();
	}
	
	private void createContent(){
		value = new WritableValue();
		context = new DataBindingContext();
		
		setLayout(new GridLayout(2, false));
		
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText("ID");
		txtId = new Text(this, SWT.BORDER);
		txtId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		IObservableValue target = WidgetProperties.text(SWT.Modify).observe(txtId);
		IObservableValue model =
			PojoProperties.value("id", MailAccount.class).observeDetail(value);
		UpdateValueStrategy targetToModel = new UpdateValueStrategy();
		targetToModel.setAfterGetValidator((o1) -> {
			String s = (String) o1;
			if (s != null && !s.isEmpty()) {
				return ValidationStatus.ok();
			}
			return ValidationStatus.error("");
		});
		Binding binding = context.bindValue(target, model, targetToModel, null);
		ControlDecorationSupport.create(binding, SWT.TOP | SWT.LEFT);

		lbl = new Label(this, SWT.NONE);
		lbl.setText("Typ");
		ComboViewer typeViewer = new ComboViewer(this, SWT.DROP_DOWN);
		typeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		typeViewer.setContentProvider(ArrayContentProvider.getInstance());
		typeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				TYPE type = (TYPE) element;
				if (type == TYPE.IMAP) {
					return "Eingehend (" + type.name() + ")";
				} else if (type == TYPE.IMAPS) {
					return "Ausgehend (IMAP over SSL)";
				} else if (type == TYPE.SMTP) {
					return "Ausgehend (" + type.name() + ")";
				} else {
					return ((TYPE) element).name();
				}
			}
		});
		typeViewer.setInput(TYPE.values());
		IViewerObservableValue viewerTarget = ViewerProperties.singleSelection().observe(typeViewer);
		model = PojoProperties.value("type", MailAccount.class).observeDetail(value);
		context.bindValue(viewerTarget, model);
		typeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				TYPE type = (TYPE) typeViewer.getStructuredSelection().getFirstElement();
				if (type != null) {
					switch (type) {
					case IMAP:
						txtPort.setText("143");
						break;
					case IMAPS:
						txtPort.setText("993");
						break;
					case SMTP:
						txtPort.setText("25");
						break;
					default:
						break;
					}
				}
				updateUi();
			}
		});
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Username");
		txtUsername = new Text(this, SWT.BORDER);
		txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txtUsername);
		model = PojoProperties.value("username", MailAccount.class).observeDetail(value);
		context.bindValue(target, model);
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Password");
		txtPassword = new Text(this, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txtPassword);
		model = PojoProperties.value("password", MailAccount.class).observeDetail(value);
		context.bindValue(target, model);
		
		fromAddressLabel = new Label(this, SWT.NONE);
		fromAddressLabel.setText("Von Adresse");
		fromAddress = new Text(this, SWT.BORDER);
		fromAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(fromAddress);
		model = PojoProperties.value("from", MailAccount.class).observeDetail(value);
		context.bindValue(target, model);
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Host");
		txtHost = new Text(this, SWT.BORDER);
		txtHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txtHost);
		model = PojoProperties.value("host", MailAccount.class).observeDetail(value);
		targetToModel = new UpdateValueStrategy();
		targetToModel.setAfterGetValidator((o1) -> {
			String s = (String) o1;
			if (s != null && !s.isEmpty()) {
				return ValidationStatus.ok();
			}
			return ValidationStatus.error("");
		});
		binding = context.bindValue(target, model, targetToModel, null);
		ControlDecorationSupport.create(binding, SWT.TOP | SWT.LEFT);
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Port");
		txtPort = new Text(this, SWT.BORDER);
		txtPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txtPort);
		model = PojoProperties.value("port", String.class).observeDetail(value);
		targetToModel = new UpdateValueStrategy();
		targetToModel.setAfterGetValidator((o1) -> {
			String s = (String) o1;
			if (s != null && !s.isEmpty()) {
				return ValidationStatus.ok();
			}
			return ValidationStatus.error("");
		});
		binding = context.bindValue(target, model, targetToModel, null);
		ControlDecorationSupport.create(binding, SWT.TOP | SWT.LEFT);
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Start TLS");
		Button btnStartTls = new Button(this, SWT.CHECK);
		target = WidgetProperties.buttonSelection().observe(btnStartTls);
		model = PojoProperties.value("starttls", Boolean.class).observeDetail(value);
		context.bindValue(target, model);
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Mandanten");
		TableViewer mandantViewer = new TableViewer(this, SWT.BORDER);
		mandantViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mandantInput = new WritableList<>(new ArrayList<Mandant>(), Mandant.class);
		ViewerSupport.bind(mandantViewer, mandantInput, PojoProperties.values(new String[] {
			"label"
		}));
		MenuManager menuManager = new MenuManager();
		menuManager.add(new Action("hinzufügen") {
			@Override
			public void run(){
				KontaktSelektor selector =
					new KontaktSelektor(getShell(), Mandant.class, "Mandant auswahl",
						"Mandanten für das Konto auswählen", Kontakt.DEFAULT_SORT);
				if (selector.open() == Dialog.OK) {
					Mandant selected = (Mandant) selector.getSelection();
					getAccount().addMandant(
						CoreModelServiceHolder.get().load(selected.getId(), IMandator.class)
							.orElse(null));
					updateUi();
				}
			}
		});
		menuManager.add(new Action("entfernen") {
			@Override
			public void run(){
				IStructuredSelection selection =
					(IStructuredSelection) mandantViewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					Mandant selected = (Mandant) selection.getFirstElement();
					getAccount().removeMandant(CoreModelServiceHolder.get()
						.load(selected.getId(), IMandator.class).orElse(null));
					updateUi();
				}
			}
			
			@Override
			public boolean isEnabled(){
				IStructuredSelection selection =
					(IStructuredSelection) mandantViewer.getSelection();
				return selection != null && !selection.isEmpty();
			}
		});
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				IContributionItem[] items = manager.getItems();
				for (IContributionItem iContributionItem : items) {
					iContributionItem.update();
				}
			}
		});
		mandantViewer.getControl()
			.setMenu(menuManager.createContextMenu(mandantViewer.getControl()));
	}
	

	public void setAccount(MailAccount mailAccount){
		value.setValue(mailAccount);
		updateUi();
	}
	
	private void updateUi(){
		MailAccount mailAccount = getAccount();
		mandantInput.clear();
		if (mailAccount != null) {
			if (mailAccount.getType() == TYPE.IMAP) {
				((GridData) fromAddressLabel.getLayoutData()).exclude = true;
				fromAddressLabel.setVisible(false);
				((GridData) fromAddress.getLayoutData()).exclude = true;
				fromAddress.setVisible(false);
			} else if (mailAccount.getType() == TYPE.SMTP) {
				((GridData) fromAddressLabel.getLayoutData()).exclude = false;
				fromAddressLabel.setVisible(true);
				((GridData) fromAddress.getLayoutData()).exclude = false;
				fromAddress.setVisible(true);
			} else {
				((GridData) fromAddressLabel.getLayoutData()).exclude = true;
				fromAddressLabel.setVisible(false);
				((GridData) fromAddress.getLayoutData()).exclude = true;
				fromAddress.setVisible(false);
			}
			String mandants = mailAccount.getMandants();
			if (mandants != null) {
				String[] ids = mandants.split("\\|\\|");
				for (String string : ids) {
					Mandant mandant = Mandant.load(string);
					if (mandant != null && mandant.exists()) {
						mandantInput.add(mandant);
					}
				}
			}
		}
		layout();
		redraw();
	}
	
	public MailAccount getAccount(){
		return value.getValue();
	}
}
