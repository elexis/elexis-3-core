/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/

package ch.elexis.core.ui.propertypage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.util.SWTHelper;

public class PatientPropertyPage extends PropertyPage implements IWorkbenchPropertyPage, IUnlockable {

	public static final String ID = "at.medevit.elexis.properties.propertyPage.PatientPropertyPage"; //$NON-NLS-1$

	private IPatient pat;
	private Text textVorname;
	private Text textNachname;
	private Text textTelefon1;
	private Text textTelefon2;
	private Text textMobile;
	private Text textFax;
	private Text textEmail;
	private Text textBemerkungen;
	private CDateTime geburtsdatum;

	private ComboViewer comboGeschlecht;

	public PatientPropertyPage() {
		super();
	}

	@Override
	protected Control createContents(Composite parent) {
		init();
		Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(2, false));

		Label lblVorname = new Label(comp, SWT.NONE);
		lblVorname.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
		lblVorname.setText(Messages.Core_Firstname);

		textVorname = new Text(comp, SWT.BORDER);
		textVorname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNachname = new Label(comp, SWT.NONE);
		lblNachname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNachname.setText(Messages.Core_Name);

		textNachname = new Text(comp, SWT.BORDER);
		textNachname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblGeschlecht = new Label(comp, SWT.NONE);
		lblGeschlecht.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblGeschlecht.setText(Messages.Sex);

		String toolTip = String.format(Messages.Patient_male_female_tooltip, Messages.Patient_male_short,
				Messages.Patient_female_short, Messages.Patient_male_long, Messages.Patient_female_long);
		comboGeschlecht = new ComboViewer(comp, SWT.NONE);
		comboGeschlecht.setContentProvider(ArrayContentProvider.getInstance());
		comboGeschlecht.setInput(Gender.values());
		comboGeschlecht.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Gender) element).value();
			}
		});
		comboGeschlecht.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblGeburtsdatum = new Label(comp, SWT.NONE);
		lblGeburtsdatum.setText(Messages.Core_Enter_Birthdate);
		geburtsdatum = new CDateTime(comp, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TEXT_TRAIL);
		geburtsdatum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);

		Label lblMobile = new Label(comp, SWT.NONE);
		lblMobile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMobile.setText(Messages.KontaktBlatt_Mobile);

		textMobile = new Text(comp, SWT.BORDER);
		textMobile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

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

		Label lblFax = new Label(comp, SWT.NONE);
		lblFax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFax.setText(Messages.Core_Fax);

		textFax = new Text(comp, SWT.BORDER);
		textFax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);

		Label lblEmail = new Label(comp, SWT.NONE);
		lblEmail.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEmail.setText(Messages.Core_E_Mail);

		textEmail = new Text(comp, SWT.BORDER);
		textEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);

		Label lblBemerkungen = new Label(comp, SWT.NONE);
		lblBemerkungen.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lblBemerkungen.setText(Messages.Core_Remark);

		textBemerkungen = new Text(comp, SWT.BORDER | SWT.MULTI);
		textBemerkungen.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		super.setTitle(pat.getLabel());
		textVorname.setText(StringUtils.defaultString(pat.getFirstName()));
		textNachname.setText(StringUtils.defaultString(pat.getLastName()));
		geburtsdatum.setSelection(getGeburtsdatum());
		comboGeschlecht.setSelection(new StructuredSelection(pat.getGender()));
		textTelefon1.setText(StringUtils.defaultString(pat.getPhone1()));
		textTelefon2.setText(StringUtils.defaultString(pat.getPhone2()));
		textFax.setText(StringUtils.defaultString(pat.getFax()));
		textMobile.setText(StringUtils.defaultString(pat.getMobile()));
		textEmail.setText(StringUtils.defaultString(pat.getEmail()));
		textBemerkungen.setText(StringUtils.defaultString(pat.getComment()));

		setUnlocked(LocalLockServiceHolder.get().isLocked(pat));

		return comp;
	}

	private void init() {
		IAdaptable adapt = getElement();
		pat = (IPatient) adapt.getAdapter(IPatient.class);
	}

	private Date getGeburtsdatum() {
		LocalDateTime dob = pat.getDateOfBirth();
		if (dob != null) {
			return Date.from(dob.atZone(ZoneId.systemDefault()).toInstant());
		} else {
			return null;
		}
	}

	@Override
	protected void performApply() {
		StructuredSelection genderSelection = (StructuredSelection) comboGeschlecht.getSelection();
		if (genderSelection != null && !genderSelection.isEmpty()) {
			pat.setGender((Gender) genderSelection.getFirstElement());
		} else {
			SWTHelper.showError(Messages.PatientErfassenDialog_Error_Sex,
					Messages.PatientErfassenDialog_Sex_must_be_specified);
			return;
		}

		Date bd = geburtsdatum.getSelection();
		if (bd != null) {
			pat.setDateOfBirth(LocalDateTime.ofInstant(bd.toInstant(), ZoneId.systemDefault()));
		}

		pat.setLastName(textNachname.getText());
		pat.setFirstName(textVorname.getText());
		pat.setEmail(textEmail.getText());
		pat.setPhone1(textTelefon1.getText());
		pat.setPhone2(textTelefon2.getText());
		pat.setMobile(textMobile.getText());
		pat.setComment(textBemerkungen.getText());
		pat.setFax(textFax.getText());
		CoreModelServiceHolder.get().save(pat);
	}

	@Override
	public boolean performOk() {
		performApply();
		return true;
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		textVorname.setEditable(unlocked);
		textNachname.setEditable(unlocked);
		textTelefon1.setEditable(unlocked);
		textTelefon2.setEditable(unlocked);
		textMobile.setEditable(unlocked);
		textFax.setEditable(unlocked);
		textEmail.setEditable(unlocked);
		textBemerkungen.setEditable(unlocked);
		geburtsdatum.setEditable(unlocked);
		comboGeschlecht.getControl().setEnabled(unlocked);
	}
}
