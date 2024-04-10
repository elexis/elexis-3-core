package ch.elexis.core.ui.e4.jface.preference;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.ui.e4.dialog.VirtualFilesystemUriEditorDialog;

/**
 * An editor for types compatible with the {@link IVirtualFilesystemService}.
 * Will hide a password on showing the entry.
 *
 */
public class URIFieldEditor extends StringButtonFieldEditor {

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
		String value = getPreferenceStore().getString(getPreferenceName());
		if (getTextControl() != null) {
			getTextControl().setText(value);
			oldValue = value;
		}
	}

	@Override
	protected String changePressed() {
		IVirtualFilesystemService virtualFilesystemService = VirtualFilesystemServiceHolder.get();
		URI inputUri = null;
		try {
			String stringValue = getStringValue();
			if (StringUtils.isNotBlank(stringValue)) {
				IVirtualFilesystemHandle fileHandle = virtualFilesystemService.of(getStringValue());
				inputUri = fileHandle.toURL().toURI();
			}
		} catch (URISyntaxException | IOException e) {
		}
		VirtualFilesystemUriEditorDialog dialog = new VirtualFilesystemUriEditorDialog(getShell(),
				virtualFilesystemService, inputUri);
		int open = dialog.open();
		if (IDialogConstants.OK_ID == open) {
			return dialog.getValue().toString();
		}
		return null;
	}

}
