package ch.elexis.core.ui.e4.dialog;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;

public class VirtualFilesystemUriEditorDialog extends TitleAreaDialog {

	private DataBindingContext m_bindingContext;

	private IVirtualFilesystemService virtualFilesystemService;
	private MyURI uri;

	private Text txtHost;
	private Text txtUser;
	private Text txtPath;
	private Text txtPassword;
	private Text txtUri;
	private Combo comboScheme;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public VirtualFilesystemUriEditorDialog(Shell parentShell, IVirtualFilesystemService virtualFilesystemService,
			URI uri) {
		super(parentShell);
		this.virtualFilesystemService = virtualFilesystemService;
		this.uri = new MyURI();
		if (uri != null) {
			this.uri.setUri(uri);
		}
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("URL Editor");
		setMessage("Edit Virtual Filesystem Service supported URL");

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblScheme = new Label(container, SWT.NONE);
		lblScheme.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblScheme.setText("Scheme");

		comboScheme = new Combo(container, SWT.NONE);
		comboScheme.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboScheme.setItems("smb", "file");

		Label lblHost = new Label(container, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHost.setText("Host");

		txtHost = new Text(container, SWT.BORDER);
		txtHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblPath = new Label(container, SWT.NONE);
		lblPath.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPath.setText("Path");

		txtPath = new Text(container, SWT.BORDER);
		txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label txtPath = new Label(container, SWT.NONE);
		txtPath.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtPath.setText("User");

		txtUser = new Text(container, SWT.BORDER);
		txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassword.setText("Password");

		txtPassword = new Text(container, SWT.BORDER);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNewLabel = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		Label lblUri = new Label(container, SWT.NONE);
		lblUri.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUri.setText("URI");

		txtUri = new Text(container, SWT.BORDER);
		txtUri.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CLIENT_ID, "Test", false);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		m_bindingContext = initDataBindings();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		setErrorMessage(null);
		if (IDialogConstants.CLIENT_ID == buttonId) {
			try {
				IVirtualFilesystemHandle vfsHandle = virtualFilesystemService.of(uri.getUri().toString());
				boolean isOk = vfsHandle.exists() && vfsHandle.canRead();
				if (isOk) {
					MessageDialog.openInformation(getShell(), "URL Test",
							uri.getUri().toString() + " exists and is readable");
				} else {
					MessageDialog.openWarning(getShell(), "URL Test",
							uri.getUri().toString() + " does not exist or is not readable");
				}
			} catch (IOException e) {
				setErrorMessage(e.getLocalizedMessage());
			}
		} else {
			super.buttonPressed(buttonId);
		}
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(543, 312);
	}

	public URI getValue() {
		return uri.getUri();
	}

	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();

		//
		ISWTObservableValue<String> widgetScheme = WidgetProperties.comboSelection().observe(comboScheme);
		IObservableValue<String> modelScheme = BeanProperties.value(MyURI.class, "scheme", String.class).observe(uri);
		bindingContext.bindValue(widgetScheme, modelScheme, null, null);

		//
		IObservableValue<String> widget = WidgetProperties.text(SWT.Modify).observe(txtHost);
		IObservableValue<String> model = BeanProperties.value(MyURI.class, "host", String.class).observe(uri);
		bindingContext.bindValue(widget, model, null, null);

		//
		IObservableValue<String> widgetPath = WidgetProperties.text(SWT.Modify).observe(txtPath);
		IObservableValue<String> modelPath = BeanProperties.value(MyURI.class, "path", String.class).observe(uri);
		bindingContext.bindValue(widgetPath, modelPath, null, null);

		//
		IObservableValue<String> widgetPass = WidgetProperties.text(SWT.Modify).observe(txtPassword);
		IObservableValue<String> modelPass = BeanProperties.value(MyURI.class, "pass", String.class).observe(uri);
		bindingContext.bindValue(widgetPass, modelPass, null, null);

		//
		IObservableValue<String> widgetUser = WidgetProperties.text(SWT.Modify).observe(txtUser);
		IObservableValue<String> modelUser = BeanProperties.value(MyURI.class, "user", String.class).observe(uri);
		bindingContext.bindValue(widgetUser, modelUser, null, null);

		//
		IConverter<String, URI> stringToUriConverter = IConverter.create(String.class, URI.class,
				o -> URI.create(o.toString()));
		IConverter<URI, String> uriToStringConverter = IConverter.create(URI.class, String.class,
				(u) -> u != null ? uri.getUri().toString() : "");
		IObservableValue<String> widgetUri = WidgetProperties.text(SWT.Modify).observe(txtUri);
		IObservableValue<URI> modelUri = BeanProperties.value(MyURI.class, "uri", URI.class).observe(uri);
		bindingContext.bindValue(widgetUri, modelUri, UpdateValueStrategy.create(stringToUriConverter),
				UpdateValueStrategy.create(uriToStringConverter));

		return bindingContext;
	}

	private class MyURI {

		private String scheme;
		private String host;
		private String path;
		private String user;
		private String pass;
		private int port;

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			firePropertyChange("port", this.port, this.port = port);
			firePropertyChange("uri", null, getUri());
		}

		public String getScheme() {
			return scheme;
		}

		public void setScheme(String scheme) {
			firePropertyChange("scheme", this.scheme, this.scheme = scheme);
			firePropertyChange("uri", null, getUri());
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			firePropertyChange("host", this.host, this.host = host);
			firePropertyChange("uri", null, getUri());
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			firePropertyChange("path", this.path, this.path = path);
			firePropertyChange("uri", null, getUri());
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			firePropertyChange("user", this.user, this.user = user);
			firePropertyChange("uri", null, getUri());
		}

		public String getPass() {
			return pass;
		}

		public void setPass(String pass) {
			firePropertyChange("pass", this.pass, this.pass = pass);
			firePropertyChange("uri", null, getUri());
		}

		public URI getUri() {
			try {
				return new URI(scheme, user + ":" + pass, host, port, path, null, null);
			} catch (URISyntaxException e) {

			}
			return null;
		}

		public void setUri(URI uri) {
			setScheme(uri.getScheme());
			setHost(uri.getHost());
			setPort(uri.getPort());
			setPath(uri.getPath());
			String userInfo = uri.getUserInfo();
			if (StringUtils.isNotBlank(userInfo)) {
				int indexOf = userInfo.indexOf(':');
				if (indexOf > 0) {
					setUser(userInfo.substring(0, indexOf));
					setPass(userInfo.substring(indexOf + 1, userInfo.length()));
				} else {
					setUser(userInfo);
				}
			}
			firePropertyChange("uri", getUri(), uri);
		}

		private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

		@SuppressWarnings("unused")
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}

		@SuppressWarnings("unused")
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);
		}

		protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
			changeSupport.firePropertyChange(propertyName, oldValue, newValue);
		}

	}
}
