package ch.elexis.core.ui.e4.jface.preference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.preferences.PreferencesUtil;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.CoreUtil.OS;

/**
 * A {@link Composite} containing an operating system selector and a
 * {@link URIFieldEditor} for file system URIs. The method
 * {@link URIFieldEditorComposite#getPreferenceName(OS)} should be overwritten
 * to select the correct preference.
 * 
 */
public class URIFieldEditorComposite extends Composite {

	private URIFieldEditor storePath;

	private String defaultPreference;

	private ComboViewer osCombo;

	private String scheme;

	public URIFieldEditorComposite(String defaultPreference, Composite parent, int style) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		this.defaultPreference = defaultPreference;

		createContent();
	}

	/**
	 * Use this method to set the {@link IPreferenceStore} used by the
	 * {@link URIFieldEditor}. If a {@link FieldEditorPreferencePage} is available
	 * do not use this method, but set directly with
	 * {@link URIFieldEditorComposite#getFieldEditor()}. <br />
	 * The field editor will store after each value change if this method is used.
	 * 
	 * @param preferenceStore
	 */
	public void setPreferenceStore(IPreferenceStore preferenceStore) {
		storePath.setPreferenceStore(preferenceStore);
		storePath.load();

		// add
		storePath.setPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				storePath.store();
			}
		});
	}

	private void createContent() {
		Combo comboOs = new Combo(this, SWT.None);
		osCombo = new ComboViewer(comboOs);
		comboOs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		osCombo.setContentProvider(ArrayContentProvider.getInstance());
		osCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((CoreUtil.OS) element).name();
			}
		});
		osCombo.setInput(CoreUtil.OS.values());

		storePath = new URIFieldEditor(
				PreferencesUtil.getOsSpecificPreferenceName(CoreUtil.getOperatingSystemType(), defaultPreference),
				StringUtils.EMPTY, this);
		storePath.setEmptyStringAllowed(true);

		osCombo.addSelectionChangedListener(event -> {
			CoreUtil.OS selection = (OS) event.getStructuredSelection().getFirstElement();
			String preferenceName = PreferencesUtil.getOsSpecificPreferenceName(selection, defaultPreference);
			storePath.store();
			storePath.setPreferenceName(preferenceName);
			storePath.load();
		});

		osCombo.setSelection(new StructuredSelection(CoreUtil.getOperatingSystemType()));
	}

	public FieldEditor getFieldEditor() {
		return storePath;
	}

	/**
	 * Fix the possible URI scheme to the provided scheme.
	 * 
	 * @param scheme
	 */
	public void setFixedScheme(String scheme) {
		this.scheme = scheme;
		if (storePath != null) {
			storePath.setFixedScheme("file");
		}
	}
}
