package ch.elexis.core.ui.e4.jface.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
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

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.CoreUtil.OS;

/**
 * Several {@link URIFieldEditorComposite} sharing one operating system
 * selector. Use this instead of single {@link URIFieldEditorComposite} if a
 * preference page holds more than one path, so all paths of the page always
 * belong to the same operating system:
 *
 * <pre>
 * Betriebssystem   [ WINDOWS v ]
 * Download         [ ****************** ]  [ Durchsuchen... ]
 * Upload           [ ****************** ]  [ Durchsuchen... ]
 * </pre>
 *
 * @since 3.14
 */
public class OsPathEditorGroup extends Composite {

	private ComboViewer osCombo;

	private final List<URIFieldEditorComposite> editors = new ArrayList<>();

	private IPreferenceStore preferenceStore;

	public OsPathEditorGroup(Composite parent, int style) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 8;
		setLayout(layout);

		createOsSelector();
	}

	private void createOsSelector() {
		Label label = new Label(this, SWT.NONE);
		label.setText(Messages.Core_OperatingSystem);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		Combo combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		osCombo = new ComboViewer(combo);
		osCombo.setContentProvider(ArrayContentProvider.getInstance());
		osCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((CoreUtil.OS) element).name();
			}
		});
		osCombo.setInput(CoreUtil.OS.values());

		osCombo.addSelectionChangedListener(event -> {
			OS selection = (OS) event.getStructuredSelection().getFirstElement();
			for (URIFieldEditorComposite editor : getPathEditors()) {
				editor.setOperatingSystem(selection);
			}
		});

		osCombo.setSelection(new StructuredSelection(CoreUtil.getOperatingSystemType()));
	}

	public URIFieldEditorComposite addPathEditor(String preferenceName, String labelText) {
		URIFieldEditorComposite editor = new URIFieldEditorComposite(preferenceName, labelText, this, SWT.NONE);
		editor.setOsSelectorVisible(false);
		editor.setEmptyStringAllowed(true);
		editor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		editor.setOperatingSystem(getOperatingSystem());
		if (preferenceStore != null) {
			editor.setPreferenceStore(preferenceStore);
		}
		editors.add(editor);
		alignLabels();
		return editor;
	}

	public void setPreferenceStore(IPreferenceStore preferenceStore) {
		this.preferenceStore = preferenceStore;
		for (URIFieldEditorComposite editor : getPathEditors()) {
			editor.setPreferenceStore(preferenceStore);
		}
	}

	public OS getOperatingSystem() {
		return (OS) osCombo.getStructuredSelection().getFirstElement();
	}

	public List<URIFieldEditorComposite> getPathEditors() {
		List<URIFieldEditorComposite> ret = new ArrayList<>(editors.size());
		for (URIFieldEditorComposite editor : editors) {
			if (!editor.isDisposed()) {
				ret.add(editor);
			}
		}
		return ret;
	}

	public void adjustHorizontalSpan() {
		if (getParent().getLayout() instanceof GridLayout && getLayoutData() instanceof GridData) {
			((GridData) getLayoutData()).horizontalSpan = ((GridLayout) getParent().getLayout()).numColumns;
		}
	}

	private void alignLabels() {
		int width = 0;
		for (URIFieldEditorComposite editor : getPathEditors()) {
			Label label = editor.getLabelControl();
			if (StringUtils.isNotBlank(label.getText())) {
				width = Math.max(width, label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
			}
		}
		for (URIFieldEditorComposite editor : getPathEditors()) {
			Label label = editor.getLabelControl();
			if (StringUtils.isNotBlank(label.getText())) {
				GridData labelData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
				labelData.widthHint = width;
				label.setLayoutData(labelData);
			}
		}
	}
}
