package ch.elexis.core.ui.e4.preference;

import org.eclipse.jface.resource.ImageDescriptor;

public class FieldEditorPreferencePage extends org.eclipse.jface.preference.FieldEditorPreferencePage {

	public FieldEditorPreferencePage() {
	}

	public FieldEditorPreferencePage(int style) {
		super(style);
	}

	public FieldEditorPreferencePage(String title, int style) {
		super(title, style);
	}

	public FieldEditorPreferencePage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors() {

	}

}
