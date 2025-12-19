package ch.elexis.core.ui.e4.jface.preference;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.ui.e4.dialog.VirtualFilesystemUriEditorDialog;

/**
 * An editor for types compatible with the {@link IVirtualFilesystemService}.
 * Will hide a password on showing the entry.
 *
 */
public class URIFieldEditor extends StringButtonFieldEditor {

	private String scheme;
	private boolean migrateLegacyPaths = false;
	private boolean useFileMode = false;

	/**
	 *
	 * /** Creates a new directory field editor
	 */
	protected URIFieldEditor() {
	}

	/**
	 * Creates a directory field editor.
	 *
	 * @param name      the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent    the parent of the field editor's control
	 */
	public URIFieldEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		setErrorMessage(JFaceResources.getString("DirectoryFieldEditor.errorMessage"));//$NON-NLS-1$
		setChangeButtonText(JFaceResources.getString("openBrowse"));//$NON-NLS-1$
		createControl(parent);
		getTextControl().setEchoChar('*');
		getTextControl().setEnabled(false);
	}

	@Override
	protected void doLoad() {
		IPreferenceStore store = getPreferenceStore();
		if (store == null)
			return;

		String value = store.getString(getPreferenceName());

		if (StringUtils.isNotBlank(value) && migrateLegacyPaths) {
			boolean isAlreadyUri = value.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:/.*");

			if (isAlreadyUri) {
				boolean isWindowsPath = value.contains("\\") || (value.length() > 2 && value.charAt(1) == ':');
				boolean isLinuxPath = value.startsWith("/");

				if (isWindowsPath || isLinuxPath) {
					try {
						File file = new File(value);
						value = file.toURI().toString();

						store.setValue(getPreferenceName(), value);
						System.out.println("Erfolgreich migriert zu URI: " + value);
					} catch (Exception e) {
						System.err.println("Fehler bei Migration von: " + value + " -> " + e.getMessage());
					}
				}
			}
		}

		if (getTextControl() != null) {
			getTextControl().setText(value != null ? value : "");
		}
		this.oldValue = value;
	}

	@Override
	protected void doStore() {
		// TODO Auto-generated method stub
		super.doStore();
	}

	@Override
	protected String changePressed() {
		IVirtualFilesystemService virtualFilesystemService = VirtualFilesystemServiceHolder.get();
		URI inputUri = null;
		try {
			String stringValue = getStringValue();
			if (StringUtils.isNotBlank(stringValue)) {
				if (!stringValue.startsWith("file:") && !stringValue.contains(":/")) {
					inputUri = new File(stringValue).toURI();
				} else {
					inputUri = new URI(stringValue);
				}
			}
		} catch (URISyntaxException e) {
		}
		VirtualFilesystemUriEditorDialog dialog = new VirtualFilesystemUriEditorDialog(getShell(),
				virtualFilesystemService, inputUri);
		dialog.setFixedScheme(scheme);
		dialog.setFileMode(useFileMode);
		if (IDialogConstants.OK_ID == dialog.open()) {
			return dialog.getValue().toString();
		}
		return null;
	}

	/**
	 * Fix the possible URI scheme to the provided scheme.
	 * 
	 * @param scheme
	 */
	public void setFixedScheme(String scheme) {
		this.scheme = scheme;
	}

	public void setMigrateLegacyPaths(boolean migrate) {
		this.migrateLegacyPaths = migrate;
	}

	public void setUseFileMode(boolean useFileMode) {
		this.useFileMode = useFileMode;
	}
}
