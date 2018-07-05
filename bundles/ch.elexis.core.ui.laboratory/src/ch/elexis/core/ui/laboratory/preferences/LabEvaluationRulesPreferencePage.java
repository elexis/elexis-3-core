package ch.elexis.core.ui.laboratory.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.l10n.Messages;

public class LabEvaluationRulesPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	public LabEvaluationRulesPreferencePage(){}
	
	private Button btnCheckNonStringEqualRefValResValIsPathologicAbsolute;
	private Button btnCheckNonStringEqualRefValResValIsPathologicText;
	
	/**
	 * Create contents of the preference page.
	 * 
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent){
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		Label lblLabImportRulesHeader = new Label(container, SWT.NONE);
		lblLabImportRulesHeader
			.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblLabImportRulesHeader
			.setText(Messages.LabEvaluationRulesPreferencePage_lblLabImportRulesHeader_text);
		
		Group grpResultEvaluation = new Group(container, SWT.NONE);
		grpResultEvaluation.setLayout(new GridLayout(1, false));
		grpResultEvaluation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnUseParameterRefValOnMissingResRefVal = new Button(grpResultEvaluation, SWT.CHECK);
		btnUseParameterRefValOnMissingResRefVal.setEnabled(false);
		btnUseParameterRefValOnMissingResRefVal.setSelection(true);
		btnUseParameterRefValOnMissingResRefVal.setText(
			Messages.LabEvaluationRulesPreferencePage_btnUseParameterRefValOnMissingResRefVal_text);
		
		Group grpAbsoluteResults = new Group(grpResultEvaluation, SWT.NONE);
		grpAbsoluteResults.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpAbsoluteResults
			.setText(Messages.LabEvaluationRulesPreferencePage_grpAbsoluteResults_text);
		grpAbsoluteResults.setLayout(new GridLayout(1, false));
		
		Button btnCheckStringEqualRefValResValIsNonPathologicAbsolute =
			new Button(grpAbsoluteResults, SWT.CHECK);
		btnCheckStringEqualRefValResValIsNonPathologicAbsolute.setEnabled(false);
		btnCheckStringEqualRefValResValIsNonPathologicAbsolute.setSelection(true);
		btnCheckStringEqualRefValResValIsNonPathologicAbsolute.setText(
			Messages.LabEvaluationRulesPreferencePage_btnCheckStringEqualRefValResValIsNonPathologicAbsolute_text);
		
		Button btnResultStartsWithPosIsPathologic = new Button(grpAbsoluteResults, SWT.CHECK);
		btnResultStartsWithPosIsPathologic.setEnabled(false);
		btnResultStartsWithPosIsPathologic.setSelection(true);
		btnResultStartsWithPosIsPathologic.setText(
			Messages.LabEvaluationRulesPreferencePage_btnResultStartsWithPosIsPathologic_text);
		
		btnCheckNonStringEqualRefValResValIsPathologicAbsolute =
			new Button(grpAbsoluteResults, SWT.CHECK);
		btnCheckNonStringEqualRefValResValIsPathologicAbsolute.setText(
			Messages.LabEvaluationRulesPreferencePage_btnCheckNonStringEqualRefValResValIsPathologicAbsolute_text);
		btnCheckNonStringEqualRefValResValIsPathologicAbsolute.addSelectionListener(
			new LERSelectionAdapter(Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT
				+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC));
		btnCheckNonStringEqualRefValResValIsPathologicAbsolute.setSelection(CoreHub.globalCfg.get(
			Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT
				+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC,
			false));
		
		Label lblWennKeineRegel = new Label(grpAbsoluteResults, SWT.NONE);
		lblWennKeineRegel.setText(Messages.LabEvaluationRulesPreferencePage_lblWennKeineRegel_text);
		
		Group grpTextResults = new Group(grpResultEvaluation, SWT.NONE);
		grpTextResults.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpTextResults.setText(Messages.LabEvaluationRulesPreferencePage_grpTextResults_text);
		grpTextResults.setLayout(new GridLayout(1, false));
		
		Button btnCheckStringEqualRefValResValIsNonPathologicText =
			new Button(grpTextResults, SWT.CHECK);
		btnCheckStringEqualRefValResValIsNonPathologicText.setEnabled(false);
		btnCheckStringEqualRefValResValIsNonPathologicText.setSelection(true);
		btnCheckStringEqualRefValResValIsNonPathologicText.setText(
			Messages.LabEvaluationRulesPreferencePage_btnCheckStringEqualRefValResValIsNonPathologicAbsolute_text);
		
		btnCheckNonStringEqualRefValResValIsPathologicText = new Button(grpTextResults, SWT.CHECK);
		btnCheckNonStringEqualRefValResValIsPathologicText.setText(
			Messages.LabEvaluationRulesPreferencePage_btnCheckNonStringEqualRefValResValIsPathologicText_text);
		btnCheckNonStringEqualRefValResValIsPathologicText.setText(
			Messages.LabEvaluationRulesPreferencePage_btnCheckNonStringEqualRefValResValIsPathologicText_text);
		btnCheckNonStringEqualRefValResValIsPathologicText.addSelectionListener(
			new LERSelectionAdapter(Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_TEXT
				+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC));
		btnCheckNonStringEqualRefValResValIsPathologicText.setSelection(CoreHub.globalCfg.get(
			Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_TEXT
				+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC,
			false));
		
		Label lblResultatEndbestimmt = new Label(grpResultEvaluation, SWT.NONE);
		lblResultatEndbestimmt
			.setText(Messages.LabEvaluationRulesPreferencePage_lblResultatEndbestimmt_text);
		
		return container;
	}
	
	private class LERSelectionAdapter extends SelectionAdapter {
		
		private String globalConfigurationKey;
		
		public LERSelectionAdapter(String globalConfigurationKey){
			this.globalConfigurationKey = globalConfigurationKey;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e){
			Button button = (Button) e.getSource();
			CoreHub.globalCfg.set(globalConfigurationKey, button.getSelection());
			super.widgetSelected(e);
		}
		
	}
	
	@Override
	protected void performApply(){
		CoreHub.globalCfg.flush();
		super.performApply();
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench){}
	
}
