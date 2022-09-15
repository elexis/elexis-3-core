package at.medevit.elexis.text.docx.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import at.medevit.elexis.text.docx.DocxTextPlugin;
import at.medevit.elexis.text.docx.print.PrintProcess;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.utils.CoreUtil;

public class DocxPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private IPreferenceStore store;

	private StringFieldEditor printNoPrinterFieldEditor;
	private StringFieldEditor printPrinterFieldEditor;

	private IntegerFieldEditor printTimeoutFieldEditor;

	public DocxPreferencePage() {
		super(GRID);
		store = new SettingsPreferenceStore(CoreHub.localCfg);
		setPreferenceStore(store);
	}

	@Override
	public void init(IWorkbench workbench) {
		if (CoreHub.localCfg.get(DocxTextPlugin.PRINT_PROCESS_TIMEOUT, -1) == -1) {
			CoreHub.localCfg.set(DocxTextPlugin.PRINT_PROCESS_TIMEOUT, 30);
			CoreHub.localCfg.flush();
		}
	}

	@Override
	protected void createFieldEditors() {
		Label variablesDescription = new Label(getFieldEditorParent(), SWT.WRAP);
		variablesDescription.setText("Folgende Vairablen können in den Befehlen verwendet werden.\n\nVariablen: "
				+ PrintProcess.getVariablesAsString() + "\n\nZ.B.: befehl.exe -p [filename]\n");
		variablesDescription.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));

		printTimeoutFieldEditor = new IntegerFieldEditor(DocxTextPlugin.PRINT_PROCESS_TIMEOUT,
				"Druck Befehl timeout in Sekunden", getFieldEditorParent());
		addField(printTimeoutFieldEditor);

		printNoPrinterFieldEditor = new StringFieldEditor(DocxTextPlugin.PRINT_COMMAND_PREF,
				"Druck Befehl ohne konf. Drucker", getFieldEditorParent());
		addField(printNoPrinterFieldEditor);
		printPrinterFieldEditor = new StringFieldEditor(DocxTextPlugin.PRINTTOPRINTER_COMMAND_PREF,
				"Druck Befehl mit konf. Drucker", getFieldEditorParent());
		addField(printPrinterFieldEditor);

		if (CoreUtil.isWindows()) {
			Button checkBox = new Button(getFieldEditorParent(), SWT.CHECK);
			checkBox.setText("Vordefinierte Scripts (powershell) und Befehle verwenden.");
			if (ConfigServiceHolder.getGlobal(DocxTextPlugin.USE_PRINT_SCRIPT, false)) {
				checkBox.setSelection(true);
			}
			checkBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ConfigServiceHolder.setGlobal(DocxTextPlugin.USE_PRINT_SCRIPT, checkBox.getSelection());
					updateFieldEditors();
				}
			});
			updateFieldEditors();
		}
	}

	private void updateFieldEditors() {
		if (ConfigServiceHolder.getGlobal(DocxTextPlugin.USE_PRINT_SCRIPT, false)) {
			printNoPrinterFieldEditor.setEnabled(false, getFieldEditorParent());
			printPrinterFieldEditor.setEnabled(false, getFieldEditorParent());
		} else {
			printNoPrinterFieldEditor.setEnabled(true, getFieldEditorParent());
			printPrinterFieldEditor.setEnabled(true, getFieldEditorParent());
		}
	}
}
