package ch.elexis.core.ui.e4.dialog;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.time.TimeUtil;

public class IContactDetailsDialogTry extends DialogTrayWithSelectionListener {

	private Text txtFamilyName;
	private Text txtName;
	private Text txtDob;
	private Text txtStreet;
	private Text txtZip;
	private Text txtCity;
	private Text txtTelephone;
	private Text txtMobile;
	private Text txtEmail;

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.minimumWidth = 600;
		container.setLayoutData(layoutData);

		GridLayout layout = new GridLayout();
		layout.marginTop = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);

		Label lblContactDetails = new Label(container, SWT.NONE);
		lblContactDetails.setText("Kontaktdaten");
		lblContactDetails.setFont(JFaceResources.getFontRegistry().getBold("default"));
		new Label(container, SWT.NONE);

		Label lblFamilyName = new Label(container, SWT.NONE);
		lblFamilyName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFamilyName.setText(Messages.Core_Name);

		txtFamilyName = new Text(container, SWT.BORDER);
		txtFamilyName.setEditable(false);
		GridData gd_txtName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtName.minimumWidth = 300;
		txtFamilyName.setLayoutData(gd_txtName);

		Label lblName = new Label(container, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText(Messages.Core_Firstname);

		txtName = new Text(container, SWT.BORDER);
		txtName.setEditable(false);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblDob = new Label(container, SWT.NONE);
		lblDob.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDob.setText(Messages.Core_Enter_Birthdate);

		txtDob = new Text(container, SWT.BORDER);
		txtDob.setEditable(false);
		txtDob.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblStreet = new Label(container, SWT.NONE);
		lblStreet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStreet.setText(Messages.Core_Street);

		txtStreet = new Text(container, SWT.BORDER);
		txtStreet.setEditable(false);
		txtStreet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblZip = new Label(container, SWT.NONE);
		lblZip.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblZip.setText(Messages.Core_Postal_code);

		txtZip = new Text(container, SWT.BORDER);
		txtZip.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblCity = new Label(container, SWT.NONE);
		lblCity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCity.setText(Messages.Core_City);

		txtCity = new Text(container, SWT.BORDER);
		txtCity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblTelephone = new Label(container, SWT.NONE);
		lblTelephone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTelephone.setText(Messages.Core_Phone);

		txtTelephone = new Text(container, SWT.BORDER);
		txtTelephone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMobil = new Label(container, SWT.NONE);
		lblMobil.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMobil.setText(Messages.Core_Mobilephone);

		txtMobile = new Text(container, SWT.BORDER);
		txtMobile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblEmail = new Label(container, SWT.NONE);
		lblEmail.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEmail.setText(Messages.Core_E_Mail);

		txtEmail = new Text(container, SWT.BORDER);
		txtEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		container.getShell().setSize(600, 400);

		return container;
	}

	@Override
	public void selectionChanged(Object selection) {
		txtFamilyName.setText("");
		txtName.setText("");
		txtStreet.setText("");
		txtZip.setText("");
		txtCity.setText("");
		txtTelephone.setText("");
		txtMobile.setText("");
		txtEmail.setText("");
		txtDob.setText("");

		if (selection instanceof IContact contact) {
			txtFamilyName.setText(StringUtils.trimToEmpty(contact.getDescription1()));
			txtName.setText(StringUtils.trimToEmpty(contact.getDescription2()));
			txtStreet.setText(StringUtils.trimToEmpty(contact.getStreet()));
			txtZip.setText(StringUtils.trimToEmpty(contact.getZip()));
			txtCity.setText(StringUtils.trimToEmpty(contact.getCity()));
			txtTelephone.setText(StringUtils.trimToEmpty(contact.getPhone1()));
			txtMobile.setText(StringUtils.trimToEmpty(contact.getMobile()));
			txtEmail.setText(StringUtils.trimToEmpty(contact.getEmail()));

			if (contact.isPerson()) {
				txtDob.setText(TimeUtil.formatSafe(contact.asIPerson().getDateOfBirth()));
			}
		}

	}

}
