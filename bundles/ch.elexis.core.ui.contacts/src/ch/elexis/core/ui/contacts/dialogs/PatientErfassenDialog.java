/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.contacts.dialogs;

import static ch.elexis.core.constants.XidConstants.DOMAIN_AHV;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.util.FormatValidator;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Person.PersonDataException;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.TimeTool.TimeFormatException;

public class PatientErfassenDialog extends TitleAreaDialog {
	HashMap<String, String> fld;
	Text tName, tVorname, tGebDat, tStrasse, tPlz, tOrt, tTel, tMail, tAHV;
	Combo cbSex;
	Patient result;
	Object po;

	public Patient getResult() {
		return result;
	}

	public PatientErfassenDialog(final Shell parent, final HashMap<String, String> fields) {
		super(parent);
		fld = fields;
	}

	private String getField(String name) {
		String ret = fld.get(name);
		if (ret == null) {
			ret = StringTool.leer;
		}
		return ret;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2, false));
		new Label(ret, SWT.NONE).setText(Messages.Core_Name); // $NON-NLS-1$
		tName = new Text(ret, SWT.BORDER);
		tName.setText(getField(Patient.FLD_NAME));
		tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.Core_Firstname); // $NON-NLS-1$
		tVorname = new Text(ret, SWT.BORDER);
		tVorname.setText(getField(Patient.FLD_FIRSTNAME));
		tVorname.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.Sex); // $NON-NLS-1$
		cbSex = new Combo(ret, SWT.SINGLE);
		String toolTip = String.format(Messages.Patient_male_female_tooltip, Messages.Patient_male_short,
				Messages.Patient_female_short, Messages.Patient_male_long, Messages.Patient_female_long);
		cbSex.setToolTipText(toolTip);
		cbSex.setItems(new String[] { Messages.Patient_male_short, Messages.Patient_female_short });
		if (StringTool.isNothing(getField(Patient.FLD_SEX))) {
			cbSex.select(0);
		} else {
			cbSex.select(StringTool.isFemale(getField(Patient.FLD_FIRSTNAME)) ? 1 : 0);
		}
		new Label(ret, SWT.NONE).setText(Messages.Core_Enter_Birthdate); // $NON-NLS-1$
		tGebDat = new Text(ret, SWT.BORDER);
		tGebDat.setText(getField(Patient.FLD_DOB));
		tGebDat.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		new Label(ret, SWT.NONE).setText(Messages.Core_Street); // $NON-NLS-1$
		tStrasse = new Text(ret, SWT.BORDER);
		tStrasse.setText(getField(Patient.FLD_STREET));
		tStrasse.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		new Label(ret, SWT.NONE).setText(Messages.Core_Postal_code); // $NON-NLS-1$
		tPlz = new Text(ret, SWT.BORDER);
		tPlz.setText(getField(Patient.FLD_ZIP));
		tPlz.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		new Label(ret, SWT.NONE).setText(Messages.Core_City); // $NON-NLS-1$
		tOrt = new Text(ret, SWT.BORDER);
		tOrt.setText(getField(Patient.FLD_PLACE));
		tOrt.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		new Label(ret, SWT.NONE).setText(Messages.Core_Phone); // $NON-NLS-1$
		tTel = new Text(ret, SWT.BORDER);
		tTel.setText(getField(Patient.FLD_PHONE1));
		tTel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		new Label(ret, SWT.NONE).setText(Messages.Core_E_Mail); // $NON-NLS-1$
		tMail = new Text(ret, SWT.BORDER);
		tMail.setText(getField(Patient.FLD_E_MAIL));
		tMail.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		final Color defaultMailColor = tMail.getForeground();
		final Color red = UiDesk.getColor(UiDesk.COL_RED);

		tMail.addModifyListener(e -> {
			var text = tMail.getText();

			if (!text.isEmpty() && !FormatValidator.isValidMailAddress(text)) {
				tMail.setForeground(red);
				setEnableOKButton(false);
				return;
			} 

			tMail.setForeground(defaultMailColor);
			setEnableOKButton(true);
		});

		new Label(ret, SWT.NONE).setText(Messages.Patientenblatt2_ahvNumber); // $NON-NLS-1$
		tAHV = new Text(ret, SWT.BORDER);
		tAHV.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		final Color defaultAHVColor = tAHV.getForeground();
		tAHV.setTextLimit(16);
		tAHV.setMessage("756.XXXX.XXXX.XX");

		tAHV.addVerifyListener(event -> {
			var text = tAHV.getText();

			if (StringUtils.isNumeric(event.text.replaceAll("\\.", ""))) {
				if (text.length() == 2 || text.length() == 7 || text.length() == 12) {
					event.text = event.text + ".";
					return;
				}

				text += event.text;
				var isValid = FormatValidator.isValidAHVNum(text);
				event.doit = text.length() <= 16 ? true : isValid;
			} else if (event.keyCode != 8) {
				event.doit = false;
			}
		});

		tAHV.addModifyListener(e -> {
			var text = tAHV.getText();

			if (!text.isEmpty() && !FormatValidator.isValidAHVNum(text)) {
				tAHV.setForeground(red);
				setEnableOKButton(false);
				return;
			}
			tAHV.setForeground(defaultAHVColor);
			setEnableOKButton(true);
		});
		return ret;
	}


	private void setEnableOKButton(boolean enabled) {
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	@Override
	public void create() {
		super.create();
		setMessage(Messages.PatientErfassenDialog_pleaseEnterPersonalia); // $NON-NLS-1$
		setTitle(Messages.PatientErfassenDialog_enterData); // $NON-NLS-1$
		getShell().setText(Messages.PatientErfassenDialog_enterPatient); // $NON-NLS-1$
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
	}

	@Override
	protected void okPressed() {
		final String[] ret = new String[9];
		ret[0] = tName.getText();
		ret[1] = tVorname.getText();
		int idx = cbSex.getSelectionIndex();
		if (idx == 1 || cbSex.getText().contentEquals(Messages.Patient_female_short)) {
			ret[2] = Patient.FEMALE; // German w for weiblich = female
		} else if (idx == 0 || cbSex.getText().contentEquals(Messages.Patient_male_short)) {
			ret[2] = Patient.MALE;
		} else if (idx == -1) {
			SWTHelper.showError(Messages.PatientErfassenDialog_Error_Sex,
					Messages.PatientErfassenDialog_Sex_must_be_specified);
			return;
		}
		ret[3] = tGebDat.getText();
		try {
			TimeTool check = null;
			if (!StringTool.isNothing(ret[3])) {
				check = new TimeTool(ret[3], true);
			}
			ret[4] = tStrasse.getText();
			ret[5] = tPlz.getText();
			ret[6] = tOrt.getText();
			ret[7] = tTel.getText();
			ret[8] = tMail.getText();
			Query<Kontakt> qbe = new Query<Kontakt>(Kontakt.class);
			qbe.add("Bezeichnung1", Query.EQUALS, ret[0], true); //$NON-NLS-1$
			qbe.add("Bezeichnung2", Query.EQUALS, ret[1], true); //$NON-NLS-1$
			if (check != null)
				qbe.add(Person.BIRTHDATE, Query.EQUALS, check.toDBString(false), true);
			List<Kontakt> list = qbe.execute();
			if ((list != null) && (!list.isEmpty())) {
				Kontakt k = list.get(0);
				if (k.istPerson()) {
					k.set(Kontakt.FLD_IS_PATIENT, StringConstants.ONE);
					if (MessageDialog.openConfirm(getShell(), Messages.PatientErfassenDialog_personExists,
							Messages.PatientErfassenDialog_personWithThisNameExists) == false) {
						// abort dialog
						super.cancelPressed();
						return;
					}
				}
			}
			result = new Patient(ret[0], ret[1], check, ret[2]);

			String formattedAHV = tAHV.getText();

			if (!formattedAHV.isEmpty()) {
				formattedAHV = FormatValidator.getFormattedAHVNum(formattedAHV);
				result.addXid(DOMAIN_AHV, formattedAHV, true);
			}

			AcquireLockUi.aquireAndRun(result, new ILockHandler() {

				@Override
				public void lockFailed() {
					result.delete();
				}

				@Override
				public void lockAcquired() {
					result.set(
							new String[] { Kontakt.FLD_STREET, Kontakt.FLD_ZIP, Kontakt.FLD_PLACE, Kontakt.FLD_PHONE1,
									Kontakt.FLD_E_MAIL },
							new String[] { ret[4], ret[5], ret[6], ret[7], ret[8] });

					ElexisEventDispatcher.fireSelectionEvent(result);
				}
			});
			super.okPressed();
		} catch (TimeFormatException e) {
			ExHandler.handle(e);
			SWTHelper.showError("Falsches Datumsformat", "Das Geburtsdatum kann nicht interpretiert werden");
			return;
		} catch (PersonDataException pe) {
			ExHandler.handle(pe);
			SWTHelper.showError("Unplausible Angaben", "Bitte überprüfen Sie die Eingabe nochmals: " + pe.getMessage());
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
