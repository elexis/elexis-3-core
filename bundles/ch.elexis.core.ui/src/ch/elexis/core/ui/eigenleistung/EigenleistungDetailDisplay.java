/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.eigenleistung;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;

import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.localservice.Constants;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.PersistentObject;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class EigenleistungDetailDisplay implements IDetailDisplay {
	private Text textTarif;
	private Text textCode;
	private Text textBezeichnung;
	private Text textEKP;
	private Text textVKP;
	private Text textZeit;

	@Inject
	public void selection(@Optional @Named("ch.elexis.core.ui.eigenleistung.selection") ICustomService customService) {
		if (textCode != null && !textCode.isDisposed()) {
			display(customService);
		}
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public Composite createDisplay(Composite parent, IViewSite site) {
		Composite ret = new Composite(parent, SWT.None);
		ret.setLayout(new GridLayout(2, false));

		Label lblTarif = new Label(ret, SWT.NONE);
		lblTarif.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTarif.setText("Tarif");

		textTarif = new Text(ret, SWT.BORDER);
		textTarif.setData("TEST_COMP_NAME", "EigenleistungDetailTarif_txt"); //$NON-NLS-1$
		textTarif.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textTarif.setTextLimit(20);
		textTarif.setEditable(false);

		Label lblCode = new Label(ret, SWT.NONE);
		lblCode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCode.setText("Kürzel (Code)");

		textCode = new Text(ret, SWT.BORDER);
		textCode.setData("TEST_COMP_NAME", "EigenleistungDetailCode_txt"); //$NON-NLS-1$
		textCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textCode.setTextLimit(20);
		textCode.setEditable(false);

		Label lblBezeichnung = new Label(ret, SWT.NONE);
		lblBezeichnung.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBezeichnung.setText("Bezeichnung");

		textBezeichnung = new Text(ret, SWT.BORDER | SWT.MULTI);
		textBezeichnung.setData("TEST_COMP_NAME", "EigenleistungDetailName_txt"); //$NON-NLS-1$
		textBezeichnung.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textBezeichnung.setTextLimit(80);
		textBezeichnung.setEditable(false);

		Label lblEKP = new Label(ret, SWT.NONE);
		lblEKP.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEKP.setText("Einkaufspreis (Rp.)");

		textEKP = new Text(ret, SWT.BORDER);
		textEKP.setData("TEST_COMP_NAME", "EigenleistungDetailEKP_txt"); //$NON-NLS-1$
		textEKP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textEKP.setTextLimit(6);
		textEKP.setEditable(false);

		Label lblVKP = new Label(ret, SWT.NONE);
		lblVKP.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVKP.setText("Verkaufspreis (Rp.)");

		textVKP = new Text(ret, SWT.BORDER);
		textVKP.setData("TEST_COMP_NAME", "EigenleistungDetailVKP_txt"); //$NON-NLS-1$
		textVKP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textVKP.setTextLimit(6);
		textVKP.setEditable(false);

		Label lblZeit = new Label(ret, SWT.NONE);
		lblZeit.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblZeit.setText("Zeitbedarf");

		textZeit = new Text(ret, SWT.BORDER);
		textZeit.setData("TEST_COMP_NAME", "EigenleistungDetailZeit_txt"); //$NON-NLS-1$
		textZeit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textZeit.setTextLimit(4);
		textZeit.setEditable(false);
		return null;
	}

	@Override
	public Class<? extends PersistentObject> getElementClass() {
		return Eigenleistung.class;
	}

	@Override
	public void display(Object obj) {
		if (obj instanceof ICustomService) {
			ICustomService customService = (ICustomService) obj;
			textTarif.setText(customService.getCodeSystemCode());
			textCode.setText((String) CoreModelServiceHolder.get().getEntityProperty("code", customService)); //$NON-NLS-1$
			textBezeichnung.setText(customService.getText());
			textEKP.setText(customService.getNetPrice().getCentsAsString());
			textVKP.setText(customService.getPrice().getCentsAsString());
			textZeit.setText(Integer.toString(customService.getMinutes()));
		} else {
			textTarif.setText(StringUtils.EMPTY);
			textCode.setText(StringUtils.EMPTY);
			textBezeichnung.setText(StringUtils.EMPTY);
			textEKP.setText(StringUtils.EMPTY);
			textVKP.setText(StringUtils.EMPTY);
			textZeit.setText(StringUtils.EMPTY);
		}
	}

	@Override
	public String getTitle() {
		return Constants.TYPE_NAME;
	}

}
