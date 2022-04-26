package ch.elexis.core.ui.e4.jface.preference;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;

/**
 * An editor for types compatible with the {@link IVirtualFilesystemService}.
 * Will hide a password on showing the entry.
 *
 */
public class URIFieldEditor extends StringButtonFieldEditor {

	/**
	 * Initial path for the Browse dialog.
	 */
	private File filterPath = null;

	/**
	 * The password-unmasked URI value
	 */
	private String unmaskedValue = null;

	/**
	 * Creates a new directory field editor
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
		setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
		createControl(parent);
	}

	@Override
	protected String changePressed() {
		File f = new File(getTextControl().getText());
		if (!f.exists()) {
			f = null;
		}
		File d = getDirectory(f);
		if (d == null) {
			return null;
		}

		unmaskedValue = d.getAbsolutePath();
		return unmaskedValue;
	}

	@Override
	protected void doStore() {
		getPreferenceStore().setValue(getPreferenceName(), unmaskedValue);
	}

	@Override
	protected boolean doCheckState() {
		setErrorMessage("");
		unmaskedValue = getTextControl().getText();
		String uri = unmaskedValue.trim();
		if (uri.length() == 0 && isEmptyStringAllowed()) {
			return true;
		}

		if (uri.contains("*")) {
			setErrorMessage("Passwort muss gesetzt sein");
			return false;
		}

		try {
			VirtualFilesystemServiceHolder.get().of(uri);
			return true;
		} catch (IOException | IllegalArgumentException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
	}

	@Override
	protected void doLoad() {
		if (getTextControl() != null) {
			unmaskedValue = getPreferenceStore().getString(getPreferenceName());
			try {
				URI uri = IVirtualFilesystemService.stringToURI(unmaskedValue);
				String maskedValue = IVirtualFilesystemService.hidePasswordInUrlString(uri.toString());
				if (maskedValue.contains("*")) {
					getTextControl().setText(maskedValue);
					oldValue = maskedValue;
				} else {
					getTextControl().setText(unmaskedValue);
					oldValue = unmaskedValue;
				}
			} catch (MalformedURLException | URISyntaxException e) {
				setErrorMessage(e.getMessage());
			}
		}
	}

	/**
	 * Helper that opens the directory chooser dialog.
	 *
	 * @param startingDirectory The directory the dialog will open in.
	 * @return File File or <code>null</code>.
	 *
	 */
	private File getDirectory(File startingDirectory) {

		DirectoryDialog fileDialog = new DirectoryDialog(getShell(), SWT.OPEN | SWT.SHEET);
		if (startingDirectory != null) {
			fileDialog.setFilterPath(startingDirectory.getPath());
		} else if (filterPath != null) {
			fileDialog.setFilterPath(filterPath.getPath());
		}
		String dir = fileDialog.open();
		if (dir != null) {
			dir = dir.trim();
			if (dir.length() > 0) {
				return new File(dir);
			}
		}

		return null;
	}

	/**
	 * Sets the initial path for the Browse dialog.
	 *
	 * @param path initial path for the Browse dialog
	 * @since 3.6
	 */
	public void setFilterPath(File path) {
		filterPath = path;
	}

}
