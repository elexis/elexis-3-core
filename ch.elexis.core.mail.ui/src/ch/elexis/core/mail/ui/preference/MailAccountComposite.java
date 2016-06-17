package ch.elexis.core.mail.ui.preference;

import java.util.ArrayList;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
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
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
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
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;

public class MailAccountComposite extends Composite {

	private WritableValue value;
	private DataBindingContext context;
	
	private Label fromAddressLabel;
	private Text fromAddress;
	private WritableList mandantInput;
	
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
		Text txt = new Text(this, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		IObservableValue target = WidgetProperties.text(SWT.Modify).observe(txt);
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
		ComboViewer viewer = new ComboViewer(this, SWT.DROP_DOWN);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return ((TYPE)element).name();
			}
		});
		viewer.setInput(TYPE.values());
		IViewerObservableValue viewerTarget = ViewerProperties.singleSelection().observe(viewer);
		model = PojoProperties.value("type", MailAccount.class).observeDetail(value);
		context.bindValue(viewerTarget, model);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				updateUi();
			}
		});
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Username");
		txt = new Text(this, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txt);
		model = PojoProperties.value("username", MailAccount.class).observeDetail(value);
		context.bindValue(target, model);
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Password");
		txt = new Text(this, SWT.BORDER | SWT.PASSWORD);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txt);
		model = PojoProperties.value("password", MailAccount.class).observeDetail(value);
		context.bindValue(target, model);
		
		fromAddressLabel = new Label(this, SWT.NONE);
		fromAddressLabel.setText("Von Adresse");
		fromAddressLabel.setLayoutData(new GridData());
		fromAddress = new Text(this, SWT.BORDER);
		fromAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(fromAddress);
		model = PojoProperties.value("from", MailAccount.class).observeDetail(value);
		context.bindValue(target, model);
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Host");
		txt = new Text(this, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txt);
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
		txt = new Text(this, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txt);
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
		Button btn = new Button(this, SWT.CHECK);
		btn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		target = WidgetProperties.selection().observe(btn);
		model = PojoProperties.value("starttls", Boolean.class).observeDetail(value);
		context.bindValue(target, model);
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Mandanten");
		TableViewer mandantViewer = new TableViewer(this, SWT.BORDER);
		mandantViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mandantInput = new WritableList(new ArrayList<Mandant>(), Mandant.class);
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
					getAccount().addMandant(selected);
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
					getAccount().removeMandant(selected);
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
	}
	
	public MailAccount getAccount(){
		return (MailAccount) value.getValue();
	}
}
