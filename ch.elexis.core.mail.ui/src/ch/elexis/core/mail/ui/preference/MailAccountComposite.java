package ch.elexis.core.mail.ui.preference;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;

public class MailAccountComposite extends Composite {

	private WritableValue value;
	private DataBindingContext context;
	
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
		
		lbl = new Label(this, SWT.NONE);
		lbl.setText("Von");
		txt = new Text(this, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		target = WidgetProperties.text(SWT.Modify).observe(txt);
		model = PojoProperties.value("from", MailAccount.class).observeDetail(value);
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
	}
	
	public void setAccount(MailAccount mailAccount){
		value.setValue(mailAccount);
	}
	
	public MailAccount getAccount(){
		return (MailAccount) value.getValue();
	}
}
