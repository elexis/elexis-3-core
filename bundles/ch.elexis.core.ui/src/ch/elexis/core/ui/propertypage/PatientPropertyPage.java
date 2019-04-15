/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/

package ch.elexis.core.ui.propertypage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;

public class PatientPropertyPage extends PropertyPage implements IWorkbenchPropertyPage, IUnlockable {
	
	public static final String ID = "at.medevit.elexis.properties.propertyPage.PatientPropertyPage";
	
	private Patient pat;
	private Text textVorname;
	private Text textNachname;
	private Text textTelefon1;
	private Text textTelefon2;
	private Text textHandy;
	private Text textFax;
	private Text textEmail;
	private Text textBemerkungen;
	private CDateTime geburtsdatum;
	
	private Combo comboGeschlecht;
	
	public PatientPropertyPage(){
		super();
	}
	
	@Override
	protected Control createContents(Composite parent){
		init();
		Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(2, false));
		
		Label lblNachname = new Label(comp, SWT.NONE);
		lblNachname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		lblNachname.setText(Messages.KontaktBlatt_LastName);
		
		textVorname = new Text(comp, SWT.BORDER);
		textVorname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblVorname = new Label(comp, SWT.NONE);
		lblVorname.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
		lblVorname.setText(Messages.KontaktBlatt_FirstName);
		
		textNachname = new Text(comp, SWT.BORDER);
		textNachname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblGeschlecht = new Label(comp, SWT.NONE);
		lblGeschlecht.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblGeschlecht.setText(Messages.KontaktBlatt_Sex);
		
		String toolTip = String.format(Messages.Patient_male_female_tooltip,
				Messages.Patient_male_short, Messages.Patient_female_short,
				Messages.Patient_male_long, Messages.Patient_female_long);
		comboGeschlecht = new Combo(comp, SWT.NONE);
		comboGeschlecht.setItems(new String[] {
				Messages.Patient_male_short, Messages.Patient_female_short
		});
		comboGeschlecht.setToolTipText(toolTip);
		comboGeschlecht.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblGeburtsdatum = new Label(comp, SWT.NONE);
		lblGeburtsdatum.setText(Messages.KontaktDetailDialog_labelBirthdate);
		geburtsdatum =
			new CDateTime(comp, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TEXT_TRAIL);
		geburtsdatum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		
		Label lblTelefon1 = new Label(comp, SWT.NONE);
		lblTelefon1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTelefon1.setText(Messages.KontaktBlatt_Phone1);
		
		textTelefon1 = new Text(comp, SWT.BORDER);
		textTelefon1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblTelefon2 = new Label(comp, SWT.NONE);
		lblTelefon2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTelefon2.setText(Messages.KontaktBlatt_Phone2);
		
		textTelefon2 = new Text(comp, SWT.BORDER);
		textTelefon2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblHandy = new Label(comp, SWT.NONE);
		lblHandy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHandy.setText(Messages.KontaktBlatt_Mobile);
		
		textHandy = new Text(comp, SWT.BORDER);
		textHandy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblFax = new Label(comp, SWT.NONE);
		lblFax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFax.setText(Messages.KontaktBlatt_Fax);
		
		textFax = new Text(comp, SWT.BORDER);
		textFax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		
		Label lblEmail = new Label(comp, SWT.NONE);
		lblEmail.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEmail.setText(Messages.KontaktBlatt_Mail);
		
		textEmail = new Text(comp, SWT.BORDER);
		textEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		
		Label lblBemerkungen = new Label(comp, SWT.NONE);
		lblBemerkungen.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lblBemerkungen.setText(Messages.KontaktBlatt_remark);
		
		textBemerkungen = new Text(comp, SWT.BORDER | SWT.MULTI);
		textBemerkungen.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		super.setTitle(pat.getLabel());
		textVorname.setText(pat.getName());
		textNachname.setText(pat.getVorname());
		geburtsdatum.setSelection(getGeburtsdatum());
		if (pat.getGeschlecht().trim().equalsIgnoreCase(Patient.MALE)) {
			comboGeschlecht.setText(Messages.Patient_male_short);
		} else {
			comboGeschlecht.setText(Messages.Patient_female_short);
		}
		textTelefon1.setText(pat.get(Patient.FLD_PHONE1));
		textTelefon2.setText(pat.get(Patient.FLD_PHONE2));
		textFax.setText(pat.get(Patient.FLD_FAX));
		textHandy.setText(pat.get(Patient.FLD_MOBILEPHONE));
		textEmail.setText(pat.get(Patient.FLD_E_MAIL));
		textBemerkungen.setText(pat.getBemerkung());
		
		setUnlocked(CoreHub.getLocalLockService().isLocked(pat));
		
		return comp;
	}
	
	private void init(){
		IAdaptable adapt = getElement();
		pat = (Patient) adapt.getAdapter(Patient.class);
	}
	
	private Date getGeburtsdatum(){
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		try {
			return df.parse(pat.getGeburtsdatum());
		} catch (ParseException e) {
			Status status =
				new Status(IStatus.WARNING, "at.medevit.elexis.properties", e.getLocalizedMessage());
			StatusManager.getManager().handle(status, StatusManager.LOG);
		}
		return null;
	}
	
	@Override
	protected void performApply(){
		Date bd = geburtsdatum.getSelection();
		Calendar cal = Calendar.getInstance();
		cal.setTime(bd);
		
		int geschlechtSelection = comboGeschlecht.getSelectionIndex();
		String geschlecht = Patient.MALE;

		if (geschlechtSelection == 1 || comboGeschlecht.getText().contentEquals(Messages.Patient_female_short)) {
			geschlecht = Patient.FEMALE; // German w for weiblich = female
		} else if (geschlechtSelection == 0 || comboGeschlecht.getText().contentEquals(Messages.Patient_male_short)) {
			geschlecht = Patient.MALE;
		} else if (geschlechtSelection == -1) {
			SWTHelper.showError(Messages.PatientErfassenDialog_Error_Sex,
					Messages.PatientErfassenDialog_Sex_must_be_specified);
			return;
		}
		pat.set(Patient.FLD_SEX, geschlecht);
		
		String bdS =
			cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH) + 1) + "."
				+ cal.get(Calendar.YEAR);
		String[] fields =
			{
				Patient.FLD_FIRSTNAME, Person.NAME, Person.BIRTHDATE, Patient.FLD_E_MAIL,
				Patient.FLD_PHONE1, Patient.FLD_PHONE2, Patient.FLD_MOBILEPHONE,
				Patient.FLD_REMARK, Patient.FLD_FAX
			};
		String[] values =
			{
				textNachname.getText(), textVorname.getText(), bdS, textEmail.getText(),
				textTelefon1.getText(), textTelefon2.getText(), textHandy.getText(),
				textBemerkungen.getText(), textFax.getText()
			};
		pat.set(fields, values);
	}
	
	@Override
	public boolean performOk(){
		performApply();
		return true;
	}

	@Override
	public void setUnlocked(boolean unlocked){
		textVorname.setEditable(unlocked);
		textNachname.setEditable(unlocked);
		textTelefon1.setEditable(unlocked);
		textTelefon2.setEditable(unlocked);
		textHandy.setEditable(unlocked);
		textFax.setEditable(unlocked);
		textEmail.setEditable(unlocked);
		textBemerkungen.setEditable(unlocked);
		geburtsdatum.setEditable(unlocked);
		comboGeschlecht.setEnabled(unlocked);
		
	}
}
