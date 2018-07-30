package ch.elexis.core.ui.dialogs.controls;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.Messages;

public class ReminderVisibilityAndPopupComposite extends Composite {
	
	private StackLayout stackLayout = new StackLayout();
	private Composite noPatientSelectedComposite;
	private Composite patientSelectedComposite;
	private Button popupOnLogin;
	private Button showOnlyOnSelectedPatient;
	private Combo comboPopup;
	
	private ComboViewer cvPopup;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ReminderVisibilityAndPopupComposite(Composite parent, int style){
		super(parent, style);
		setLayout(stackLayout);
		
		noPatientSelectedComposite = new Composite(this, SWT.NONE);
		GridLayout gl_noPatientSelectedComposite = new GridLayout(2, false);
		gl_noPatientSelectedComposite.marginWidth = 0;
		gl_noPatientSelectedComposite.marginHeight = 0;
		noPatientSelectedComposite.setLayout(gl_noPatientSelectedComposite);
		
		Label popupIconLabel = new Label(noPatientSelectedComposite, SWT.NONE);
		popupIconLabel.setImage(Images.IMG_BELL_EXCLAMATION.getImage());
		
		popupOnLogin = new Button(noPatientSelectedComposite, SWT.CHECK);
		popupOnLogin.setText(Visibility.POPUP_ON_LOGIN.getLocaleText());
		
		patientSelectedComposite = new Composite(this, SWT.NONE);
		GridLayout gl_patientSelectedComposite = new GridLayout(4, false);
		gl_patientSelectedComposite.marginWidth = 0;
		gl_patientSelectedComposite.marginHeight = 0;
		patientSelectedComposite.setLayout(gl_patientSelectedComposite);
		
		Label visibilityLabel = new Label(patientSelectedComposite, SWT.NONE);
		visibilityLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		visibilityLabel.setImage(Images.IMG_EYE_WO_SHADOW.getImage());
		
		showOnlyOnSelectedPatient = new Button(patientSelectedComposite, SWT.CHECK);
		showOnlyOnSelectedPatient.setText(Visibility.ON_PATIENT_SELECTION.getLocaleText());
		showOnlyOnSelectedPatient.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				boolean selection = showOnlyOnSelectedPatient.getSelection();
				if (!selection) {
					cvPopup.setSelection(StructuredSelection.EMPTY);
				}
				comboPopup.setEnabled(selection);
			}
		});
		showOnlyOnSelectedPatient
			.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label visibilityIconLabel = new Label(patientSelectedComposite, SWT.NONE);
		visibilityIconLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		visibilityIconLabel.setImage(Images.IMG_BELL_EXCLAMATION.getImage());
		
		cvPopup = new ComboViewer(patientSelectedComposite, SWT.NONE);
		comboPopup = cvPopup.getCombo();
		comboPopup.setEnabled(false);
		comboPopup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cvPopup.setContentProvider(ArrayContentProvider.getInstance());
		cvPopup.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof Visibility) {
					return ((Visibility) element).getLocaleText();
				}
				return element.toString();
			}
		});
		cvPopup.addSelectionChangedListener(s -> {
			StructuredSelection ss = (StructuredSelection) s.getSelection();
			Object firstElement = ss.getFirstElement();
			if (firstElement != null && firstElement != StringConstants.EMPTY) {
				showOnlyOnSelectedPatient.setSelection(true);
			}
		});
		cvPopup.setInput(new Object[] {
			StringConstants.EMPTY, Visibility.POPUP_ON_PATIENT_SELECTION, Visibility.POPUP_ON_LOGIN
		});
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void selectVisibleControl(boolean withPatientRelation){
		if (withPatientRelation) {
			stackLayout.topControl = patientSelectedComposite;
		} else {
			stackLayout.topControl = noPatientSelectedComposite;
		}
		layout();
	}
	
	public Visibility getConfiguredVisibility(){
		if (patientSelectedComposite.equals(stackLayout.topControl)) {
			if (showOnlyOnSelectedPatient.getSelection()) {
				Object firstElement = cvPopup.getStructuredSelection().getFirstElement();
				if (firstElement == null || StringConstants.EMPTY.equals(firstElement)) {
					return Visibility.ON_PATIENT_SELECTION;
				} else {
					return (Visibility) firstElement;
				}
			} else {
				return Visibility.ALWAYS;
			}
		} else {
			if (popupOnLogin.getSelection()) {
				return Visibility.POPUP_ON_LOGIN;
			} else {
				return Visibility.ALWAYS;
			}
		}
	}
	
	public void setConfiguredVisibility(Visibility visibility, boolean withPatientRelation){
		selectVisibleControl(withPatientRelation);
		
		if (noPatientSelectedComposite.equals(stackLayout.topControl)) {
			popupOnLogin.setSelection(Visibility.POPUP_ON_LOGIN == visibility);
		} else {
			if (visibility == null) {
				visibility = Visibility.ALWAYS;
				boolean defaultPatientRelated = CoreHub.userCfg.get(Preferences.USR_REMINDER_DEFAULT_PATIENT_RELATED,
						false);
				if (withPatientRelation && defaultPatientRelated) {
					visibility = Visibility.ON_PATIENT_SELECTION;
				}
			}
			showOnlyOnSelectedPatient.setSelection(Visibility.ON_PATIENT_SELECTION == visibility);
			showOnlyOnSelectedPatient.setToolTipText(Messages.ReminderView_defaultPatientRelatedTooltip);
			showOnlyOnSelectedPatient.setToolTipText("Der Vorgabewert kann unter Einstellungen..Anwender..Pendenzen geändert werden");
			comboPopup.setEnabled(true);
			if (Visibility.POPUP_ON_LOGIN == visibility
				|| Visibility.POPUP_ON_PATIENT_SELECTION == visibility) {
				cvPopup.setSelection(new StructuredSelection(visibility));
			} else {
				cvPopup.setSelection(new StructuredSelection(StringConstants.EMPTY));
				
			}
			// showOnlyOnSelectedPatient.setSelection(!(Visibility.ALWAYS == visibility));
		}
		
	}
	
}
