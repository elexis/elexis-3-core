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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.preferences.PreferencesUtil;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.CoreUtil.OS;

/**
 * A {@link Composite} containing an operating system selector and a
 * {@link URIFieldEditor} for file system URIs. The value is stored per
 * operating system, see
 * {@link PreferencesUtil#getOsSpecificPreferenceName(OS, String)}.
 * All controls sit on one row, the operating system selector between the label
 * and the path field:
 * 
 *   Verzeichnis   [ WINDOWS v ]   [ **************** ]   [ Durchsuchen... ]
 *
 * Without a label text the first column is omitted, for callers that provide
 * their own label next to this composite.
 */
public class URIFieldEditorComposite extends Composite {

	private URIFieldEditor storePath;

	private String defaultPreference;

	private String labelText;

	private ComboViewer osCombo;

	private String scheme;

	private boolean hasLabel;

	private boolean osSelectorVisible = true;

	private CoreUtil.OS operatingSystem;

	public URIFieldEditorComposite(String defaultPreference, Composite parent, int style) {
		this(defaultPreference, StringUtils.EMPTY, parent, style);
	}

	/**
	 * @param defaultPreference the base key, without the operating system suffix
	 * @param labelText         the label shown in front of the path field, may be
	 *                          empty if the caller provides its own label
	 * @param parent
	 * @param style
	 * @since 3.14
	 */
	public URIFieldEditorComposite(String defaultPreference, String labelText, Composite parent, int style) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		this.defaultPreference = defaultPreference;
		this.labelText = labelText;

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
		loadWithLegacyFallback();
		storePath.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				storePath.store();
			}
		});
	}

	public void setDefaultPreference(String defaultPreference) {
		this.defaultPreference = defaultPreference;
		storePath.setPreferenceName(PreferencesUtil.getOsSpecificPreferenceName(operatingSystem, defaultPreference));
		loadWithLegacyFallback();
	}

	private void loadWithLegacyFallback() {
		storePath.load();
		if (storePath.getPreferenceStore() == null || StringUtils.isNotBlank(storePath.getStringValue())) {
			return;
		}
		String legacyValue = storePath.getPreferenceStore().getString(defaultPreference);
		if (StringUtils.isNotBlank(legacyValue)) {
			storePath.setStringValue(legacyValue);
		}
	}

	private void createContent() {
		operatingSystem = CoreUtil.getOperatingSystemType();
		storePath = new URIFieldEditor(
				PreferencesUtil.getOsSpecificPreferenceName(operatingSystem, defaultPreference),
				labelText, this);
		storePath.setEmptyStringAllowed(true);

		Combo comboOs = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboOs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		osCombo = new ComboViewer(comboOs);
		osCombo.setContentProvider(ArrayContentProvider.getInstance());
		osCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((CoreUtil.OS) element).name();
			}
		});
		osCombo.setInput(CoreUtil.OS.values());

		Label label = storePath.getLabelControl(this);
		comboOs.moveBelow(label);

		hasLabel = StringUtils.isNotBlank(labelText);
		if (!hasLabel) {
			GridData hidden = new GridData();
			hidden.exclude = true;
			label.setLayoutData(hidden);
			label.setVisible(false);
		}

		GridLayout layout = new GridLayout(getNumberOfColumns(), false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 8;
		setLayout(layout);

		osCombo.addSelectionChangedListener(
				event -> setOperatingSystem((OS) event.getStructuredSelection().getFirstElement()));

		osCombo.setSelection(new StructuredSelection(operatingSystem));
	}

	private int getNumberOfColumns() {
		return 2 + (hasLabel ? 1 : 0) + (osSelectorVisible ? 1 : 0);
	}

	public void setOperatingSystem(CoreUtil.OS system) {
		if (system == null || system == operatingSystem) {
			return;
		}
		operatingSystem = system;
		storePath.store();
		storePath.setPreferenceName(PreferencesUtil.getOsSpecificPreferenceName(system, defaultPreference));
		loadWithLegacyFallback();
		if (!osCombo.getCombo().isDisposed() && !system.equals(osCombo.getStructuredSelection().getFirstElement())) {
			osCombo.setSelection(new StructuredSelection(system));
		}
	}

	public CoreUtil.OS getOperatingSystem() {
		return operatingSystem;
	}

	public void setOsSelectorVisible(boolean visible) {
		if (osSelectorVisible == visible) {
			return;
		}
		osSelectorVisible = visible;
		Combo combo = osCombo.getCombo();
		GridData comboData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		comboData.exclude = !visible;
		combo.setLayoutData(comboData);
		combo.setVisible(visible);
		((GridLayout) getLayout()).numColumns = getNumberOfColumns();
		layout(true);
	}

	public Label getLabelControl() {
		return storePath.getLabelControl(this);
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

	public String getFixedScheme() {
		return scheme;
	}

	public void setEmptyStringAllowed(boolean value) {
		storePath.setEmptyStringAllowed(value);
	}
}
