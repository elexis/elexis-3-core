package ch.elexis.core.ui.laboratory.preferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Labor;

public class LabEvaluationRulesPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	public LabEvaluationRulesPreferencePage(){}
	
	private Button btnCheckNonStringEqualRefValResValIsPathologicAbsolute;
	private Button btnCheckNonStringEqualRefValResValIsPathologicText;
	
	private ListViewer labMPathMNonPathListViewer;
	
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
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		Label lblLabNoPathFlagMeansNonPath = new Label(composite, SWT.NONE);
		lblLabNoPathFlagMeansNonPath
			.setText(Messages.LabEvaluationRulesPreferencePage_lblLabNoPathFlagMeansNonPath_text);
		
		ToolBarManager toolbarmgr = new ToolBarManager();
		toolbarmgr.add(new AddMissingPathFlagMeansNonPathLaboratoryAction());
		toolbarmgr.add(new RemoveMissingPathFlagMeansNonPathLaboratoryAction());
		ToolBar toolbar = toolbarmgr.createControl(composite);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		
		labMPathMNonPathListViewer = new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		labMPathMNonPathListViewer.setContentProvider(ArrayContentProvider.getInstance());
		labMPathMNonPathListViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof Kontakt) {
					return ((Kontakt) element).getLabel();
				}
				return super.getText(element);
			}
		});
		labMPathMNonPathListViewer.getList()
			.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		labMPathMNonPathListViewer.setInput(findAllLabsWithPathFlagMissingMeansNonPathologic());
		
		return container;
	}
	
	private Set<Labor> findAllLabsWithPathFlagMissingMeansNonPathologic(){
		List<String> laboratoryIdList = CoreHub.globalCfg.getAsList(
			Preferences.LABSETTINGS_MISSING_PATH_FLAG_MEANS_NON_PATHOLOGIC_FOR_LABORATORIES);
		return new HashSet<Labor>(
			laboratoryIdList.stream().map(id -> Labor.load(id)).collect(Collectors.toList()));
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
	
	private class AddMissingPathFlagMeansNonPathLaboratoryAction extends Action {
		
		{
			setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
		}
		
		@Override
		public void run(){
			KontaktSelektor dialog =
				new KontaktSelektor(getShell(), Labor.class, Messages.LabImporterUtil_SelectLab,
					Messages.LabImporterUtil_SelectLab, Kontakt.DEFAULT_SORT);
			if (dialog.open() == Dialog.OK) {
				Labor contact = (Labor) dialog.getSelection();
				Set<Labor> findAllLabsWithPathFlagMissingMeansNonPathologic =
					findAllLabsWithPathFlagMissingMeansNonPathologic();
				if (!findAllLabsWithPathFlagMissingMeansNonPathologic.contains(contact)) {
					findAllLabsWithPathFlagMissingMeansNonPathologic.add(contact);
					CoreHub.globalCfg.setAsList(
						Preferences.LABSETTINGS_MISSING_PATH_FLAG_MEANS_NON_PATHOLOGIC_FOR_LABORATORIES,
						findAllLabsWithPathFlagMissingMeansNonPathologic.stream()
							.map(l -> l.getId()).collect(Collectors.toList()));
					CoreHub.globalCfg.flush();
					labMPathMNonPathListViewer
						.setInput(findAllLabsWithPathFlagMissingMeansNonPathologic());
				}
			}
		}
	};
	
	private class RemoveMissingPathFlagMeansNonPathLaboratoryAction extends Action {
		{
			setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
		}
		
		public void run(){
			IStructuredSelection selection = labMPathMNonPathListViewer.getStructuredSelection();
			if (selection != null && !selection.isEmpty()) {
				Labor contact = (Labor) selection.getFirstElement();
				Set<Labor> findAllLabsWithPathFlagMissingMeansNonPathologic =
					findAllLabsWithPathFlagMissingMeansNonPathologic();
				findAllLabsWithPathFlagMissingMeansNonPathologic.remove(contact);
				CoreHub.globalCfg.setAsList(
					Preferences.LABSETTINGS_MISSING_PATH_FLAG_MEANS_NON_PATHOLOGIC_FOR_LABORATORIES,
					findAllLabsWithPathFlagMissingMeansNonPathologic.stream().map(l -> l.getId())
						.collect(Collectors.toList()));
				CoreHub.globalCfg.flush();
				labMPathMNonPathListViewer.remove(contact);
			}
		};
	}
	
}
