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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

public class HL7LabImportRulesPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	public HL7LabImportRulesPreferencePage(){}
	
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
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		Label lblLabNoPathFlagMeansNonPath = new Label(composite, SWT.WRAP);
		lblLabNoPathFlagMeansNonPath
			.setText(Messages.HL7LabImportRulesPreferencePage_lblLabImportRulesHeader_text);
		
		ToolBarManager toolbarmgr = new ToolBarManager();
		toolbarmgr.add(new AddMissingPathFlagMeansNonPathLaboratoryAction());
		toolbarmgr.add(new RemoveMissingPathFlagMeansNonPathLaboratoryAction());
		ToolBar toolbar = toolbarmgr.createControl(composite);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		new Label(composite, SWT.NONE);
		
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
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gridData.heightHint = 80;
		labMPathMNonPathListViewer.getList()
			.setLayoutData(gridData);

		labMPathMNonPathListViewer.setInput(findAllLabsWithPathFlagMissingMeansNonPathologic());
		
		return container;
	}
	
	private Set<Labor> findAllLabsWithPathFlagMissingMeansNonPathologic(){
		List<String> laboratoryIdList = CoreHub.globalCfg.getAsList(
			Preferences.LABSETTINGS_MISSING_PATH_FLAG_MEANS_NON_PATHOLOGIC_FOR_LABORATORIES);
		return new HashSet<Labor>(
			laboratoryIdList.stream().map(id -> Labor.load(id)).collect(Collectors.toList()));
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
