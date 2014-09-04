/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.controls;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.model.IContact;
import ch.elexis.core.types.ContactGender;
import ch.elexis.core.types.CountryCode;
import ch.elexis.core.ui.contacts.proposalProvider.CityInformationProposalProvider;
import ch.elexis.core.ui.contacts.proposalProvider.ContactGeonames;
import ch.elexis.core.ui.contacts.proposalProvider.StreetInformationProposalProvider;
import ch.elexis.core.ui.contacts.proposalProvider.TitleProposalProvider;
import ch.elexis.core.ui.contacts.proposalProvider.ZipInformationProposalProvider;
import ch.elexis.core.ui.contacts.views.provider.ContactSelectorObservableMapLabelProvider;
import ch.elexis.core.ui.contacts.views.provider.TableDecoratingLabelProvider;
import ch.elexis.core.ui.icons.Images;

// TODO Use icons from elexis?
public class StammDatenComposite extends AbstractComposite {
	
	private static Logger log = LoggerFactory.getLogger(StammDatenComposite.class);
	
	private IContact kontakt;
	
	private Label lblHeadline;
	private Label lblContactType;
	
	private Text txtTitleFront;
	private GridData gd_txtTitleFront;
	private Text txtFirstName;
	private Text txtFamilyName;
	private Text txtTitleBack;
	private GridData gd_txtTitleBack;
	private GridData gd_comboSex;
	private Text txtZIP;
	private ZipInformationProposalProvider zipIP = new ZipInformationProposalProvider();
	private Text txtCity;
	private CityInformationProposalProvider cityIP = new CityInformationProposalProvider();
	private Text txtStreet;
	private StreetInformationProposalProvider streetIP = new StreetInformationProposalProvider();
	private Text txtTelefon;
	private Text txtTelefon2;
	private Text txtMobil;
	private Text txtFax;
	private Text txtEmail;
	private Text txtWebsite;
	private Text txtNotes;
	private ComboViewer comboViewerCountry;
	private DateTime dateTimeDob;
	private GridData gd_dateTimeDob;
	
	private Composite compositeHeader;
	private ComboViewer comboViewerSex;
	
	private TabbedPropertySheetWidgetFactory tpsf = null;
	
	public static final String PERSON_LABEL = "Vollständiger Name, Geschlecht, Geburtsdatum";
	public static final String ORGANIZATION_LABEL = "Bezeichnung, Zusatz";
	private Label lblCode;
	
	private ILabelDecorator decorator;
	private TableDecoratingLabelProvider tdlp;
	
	public StammDatenComposite(Composite parent, int style, TabbedPropertySheetPage tpsp){
		this(parent, style);
		tpsf = tpsp.getWidgetFactory();
		if (tpsf != null) {
			tpsf.adapt(compositeHeader);
			tpsf.paintBordersFor(compositeHeader);
			tpsf.adapt(lblContactType, true, false);
			tpsf.adapt(lblHeadline, true, false);
			tpsf.adapt(lblCode, true, true);
			tpsf.adapt(comboViewerSex.getCombo());
			tpsf.paintBordersFor(comboViewerSex.getCombo());
			tpsf.paintBordersFor(dateTimeDob);
			tpsf.adapt(dateTimeDob);
			tpsf.adapt(comboViewerCountry.getCombo());
			tpsf.paintBordersFor(comboViewerCountry.getCombo());
			tpsf.adapt(btnEmail, true, true);
			tpsf.adapt(btnWebsite, true, true);
			tpsf.adapt(txtNotes, true, true);
		}
	}
	
	public StammDatenComposite(Composite parent, int style){
		super(parent, style);
		decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
		tdlp =
			new TableDecoratingLabelProvider(new ContactSelectorObservableMapLabelProvider(
				new ObservableMap[] {}), decorator);
		
		setLayout(new GridLayout(6, false));
		
		{ // HEADER
			compositeHeader = new Composite(this, SWT.NONE);
			compositeHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 6, 1));
			compositeHeader.setLayout(new GridLayout(3, false));
			
			lblContactType = new Label(compositeHeader, SWT.NONE);
			
			lblHeadline = new Label(compositeHeader, SWT.NONE);
			lblHeadline.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			lblCode = new Label(compositeHeader, SWT.NONE);
			GridData gd_lblCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_lblCode.widthHint = 40;
			lblCode.setLayoutData(gd_lblCode);
		}
		
		{ // TYPE_PERSON - Title prefix
			txtTitleFront = new Text(this, SWT.BORDER);
			gd_txtTitleFront = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_txtTitleFront.widthHint = 60;
			txtTitleFront.setLayoutData(gd_txtTitleFront);
			txtTitleFront.setMessage("Titel");
			ContentProposalAdapter cpaTitleFront =
				new ContentProposalAdapter(txtTitleFront, new TextContentAdapter(),
					new TitleProposalProvider(TitleProposalProvider.TITLE_POSITION_PREFIX), null,
					null);
			cpaTitleFront.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		}
		
		txtFirstName = new Text(this, SWT.BORDER);
		txtFirstName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtFamilyName = new Text(this, SWT.BORDER);
		txtFamilyName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		{ // TYPE_PERSON - Title back
			txtTitleBack = new Text(this, SWT.BORDER);
			gd_txtTitleBack = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_txtTitleBack.widthHint = 60;
			txtTitleBack.setLayoutData(gd_txtTitleBack);
			txtTitleBack.setMessage("Titel");
			ContentProposalAdapter cpaTitleBack =
				new ContentProposalAdapter(txtTitleBack, new TextContentAdapter(),
					new TitleProposalProvider(TitleProposalProvider.TITLE_POSITION_SUFFIX), null,
					null);
			cpaTitleBack.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		}
		
		{ // TYPE_PERSON - Sex
			comboViewerSex = new ComboViewer(this, SWT.NONE);
			gd_comboSex = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			comboViewerSex.getCombo().setLayoutData(gd_comboSex);
			comboViewerSex.setContentProvider(ArrayContentProvider.getInstance());
			comboViewerSex.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					switch ((ContactGender) element) {
					case FEMALE:
						return "W";
					case MALE:
						return "M";
					case UNDEFINED:
						return "X";
					default:
						return "?";
					}
				}
			});
			comboViewerSex.setInput(ContactGender.values());
		}
		{ // TYPE_PERSON - Birthday
			dateTimeDob = new DateTime(this, SWT.BORDER | SWT.LONG);
			gd_dateTimeDob = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			dateTimeDob.setLayoutData(gd_dateTimeDob);
		}
		
		Group grpAddress = new Group(this, SWT.NONE);
		grpAddress.setLayout(new GridLayout(3, false));
		grpAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		grpAddress.setText("Adresse");
		{
			// TODO: Default selection according to current Mandant's country
			comboViewerCountry = new ComboViewer(grpAddress, SWT.NONE);
			GridData gd_combo_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_combo_1.widthHint = 55;
			comboViewerCountry.getCombo().setLayoutData(gd_combo_1);
			comboViewerCountry.setContentProvider(ArrayContentProvider.getInstance());
			comboViewerCountry.setInput(CountryCode.values());
			comboViewerCountry.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event){
					CountryCode selCountry =
						(CountryCode) ((StructuredSelection) event.getSelection())
							.getFirstElement();
					if (selCountry == CountryCode.NDF) {
						comboViewerCountry.getCombo().setForeground(
							Display.getCurrent().getSystemColor(SWT.COLOR_RED));
						ContactGeonames.setCountry(null);
					} else {
						comboViewerCountry.getCombo().setForeground(
							Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
						ContactGeonames.setCountry(selCountry);
					}
					
				}
			});
		}
		{
			txtCity = new Text(grpAddress, SWT.BORDER);
			txtCity.setMessage("Ortschaft");
			txtCity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			ContentProposalAdapter cpaCity =
				new ContentProposalAdapter(txtCity, new TextContentAdapter(), cityIP, null, null);
			cpaCity.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			cpaCity.addContentProposalListener(new IContentProposalListener() {
				@Override
				public void proposalAccepted(IContentProposal proposal){
					txtZIP.setText(cityIP.findZipForCityName(proposal.getContent()));
				}
			});
		}
		{ // ZIP
			txtZIP = new Text(grpAddress, SWT.BORDER);
			txtZIP.setMessage("PLZ");
			txtZIP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			txtZIP.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e){
					String currZip = ((Text) e.widget).getText();
					if (currZip == null || currZip.length() < 4)
						return;
					streetIP.setZip(currZip);
				}
			});
			
			ContentProposalAdapter cpaZip =
				new ContentProposalAdapter(txtZIP, new TextContentAdapter(), zipIP, null, null);
			cpaZip.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			cpaZip.addContentProposalListener(new IContentProposalListener() {
				@Override
				public void proposalAccepted(IContentProposal proposal){
					txtCity.setText(zipIP.findCityNameForZip(proposal.getContent()));
				}
			});
		}
		
		Label lblImageCountry = new Label(grpAddress, SWT.NONE);
		GridData gd_lblImageCountry = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblImageCountry.widthHint = 55;
		lblImageCountry.setLayoutData(gd_lblImageCountry);
		
		{
			txtStreet = new Text(grpAddress, SWT.BORDER);
			txtStreet.setMessage("Strasse, Hausnummer");
			txtStreet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			
			ContentProposalAdapter cpaStreet =
				new ContentProposalAdapter(txtStreet, new TextContentAdapter(), streetIP, null,
					null);
			cpaStreet.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		}
		
		Group grpKontaktdaten = new Group(this, SWT.NONE);
		grpKontaktdaten.setText("Kontaktdaten");
		grpKontaktdaten.setLayout(new GridLayout(4, false));
		grpKontaktdaten.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		
		Label lblImgPhone = new Label(grpKontaktdaten, SWT.NONE);
		lblImgPhone.setImage(Images.IMG_TELEPHONE.getImage());
		lblImgPhone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtTelefon = new Text(grpKontaktdaten, SWT.BORDER);
		txtTelefon.setMessage("Telefon-Nummer");
		txtTelefon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtTelefon.addFocusListener(preDialFocuListener);
		
		Label lblImgePhone2 = new Label(grpKontaktdaten, SWT.NONE);
		lblImgePhone2.setImage(Images.IMG_TELEPHONE.getImage());
		lblImgePhone2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtTelefon2 = new Text(grpKontaktdaten, SWT.BORDER);
		txtTelefon2.setMessage("Telefon-Nummer");
		txtTelefon2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtTelefon2.addFocusListener(preDialFocuListener);
		
		Label lblImgMobilePhone = new Label(grpKontaktdaten, SWT.NONE);
		lblImgMobilePhone.setImage(Images.IMG_MOBILEPHONE.getImage());
		lblImgMobilePhone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtMobil = new Text(grpKontaktdaten, SWT.BORDER);
		txtMobil.setMessage("Handy-Nummer");
		txtMobil.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtMobil.addFocusListener(preDialFocuListener);
		
		Label lblImgFax = new Label(grpKontaktdaten, SWT.NONE);
		lblImgFax.setImage(Images.IMG_FAX.getImage());
		lblImgFax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		txtFax = new Text(grpKontaktdaten, SWT.BORDER);
		txtFax.setMessage("Fax-Nummer");
		txtFax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtFax.addFocusListener(preDialFocuListener);
		
		btnEmail = new Button(grpKontaktdaten, SWT.FLAT);
		btnEmail.setImage(Images.IMG_MAIL.getImage());
		btnEmail.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnEmail.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				try {
					URI uriMailTo = new URI("mailto", txtEmail.getText(), null);
					Desktop.getDesktop().mail(uriMailTo);
				} catch (URISyntaxException | IOException ex) {
					log.warn("Error opening URI", ex);
				}
			}
		});
		
		txtEmail = new Text(grpKontaktdaten, SWT.BORDER);
		txtEmail.setMessage("E-Mail Adresse");
		txtEmail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnWebsite = new Button(grpKontaktdaten, SWT.FLAT);
		btnWebsite.setImage(Images.IMG_WEB.getImage());
		btnWebsite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnWebsite.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				try {
					URI uriURL = new URI(txtWebsite.getText());
					if (!uriURL.isAbsolute()) {
						uriURL = new URI("http://" + txtWebsite.getText());
					}
					Desktop.getDesktop().browse(uriURL);
				} catch (URISyntaxException | IOException ex) {
					log.warn("Error opening URI", ex);
				}
			}
		});
		
		txtWebsite = new Text(grpKontaktdaten, SWT.BORDER);
		txtWebsite.setMessage("Webseite");
		txtWebsite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		{ // Notes
			Group grpNotes = new Group(this, SWT.NONE);
			grpNotes.setLayout(new GridLayout(1, false));
			grpNotes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 7, 1));
			grpNotes.setText("Notizen");
			txtNotes = new Text(grpNotes, SWT.V_SCROLL | SWT.WRAP);
			GridData gd_txtNotes = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd_txtNotes.heightHint = (int) (5 * txtNotes.getFont().getFontData()[0].height);
			txtNotes.setLayoutData(gd_txtNotes);
		}
		
		initDataBindings();
	}
	
	@Override
	public void setContact(ContactBean k){
		this.kontakt = k;
		contactObservable.setValue(kontakt);
		
		switch (kontakt.getContactType()) {
		case PERSON:
			lblContactType.setImage(tdlp.getColumnImage(kontakt, 0));
			comboViewerSex.getCombo().setVisible(true);
			gd_comboSex.exclude = false;
			txtTitleBack.setVisible(true);
			gd_txtTitleBack.exclude = false;
			txtTitleFront.setVisible(true);
			gd_txtTitleFront.exclude = false;
			dateTimeDob.setVisible(true);
			gd_dateTimeDob.exclude = false;
			lblHeadline.setText(PERSON_LABEL);
			txtFirstName.setMessage("Vorname");
			txtFamilyName.setMessage("Nachname");
			if (kontakt.getCode() != null) {
				lblCode.setText("#" + kontakt.getCode());
			} else {
				lblCode.setText("");
			}
			break;
		case ORGANIZATION:
			lblContactType.setImage(tdlp.getColumnImage(kontakt, 0));
			comboViewerSex.getCombo().setVisible(false);
			gd_comboSex.exclude = true;
			txtTitleFront.setVisible(false);
			gd_txtTitleFront.exclude = true;
			txtTitleBack.setVisible(false);
			gd_txtTitleBack.exclude = true;
			dateTimeDob.setVisible(false);
			gd_dateTimeDob.exclude = true;
			txtFirstName.setMessage("Bezeichnung 1");
			txtFamilyName.setMessage("Bezeichnung 2");
			lblHeadline.setText(ORGANIZATION_LABEL);
			lblCode.setText("");
			break;
		default:
			break;
		}
		layout();
	}
	
	protected void initDataBindings(){
		DataBindingContext bindingContext = new DataBindingContext();
		
		Text[] control =
			{
				txtTitleFront, txtFirstName, txtFamilyName, txtTitleBack, txtZIP, txtCity,
				txtStreet, txtTelefon, txtTelefon2, txtFax, txtEmail, txtWebsite, txtMobil,
				txtNotes
			};
		String[] property =
			{
				"titel", "description2", "description1", "titelSuffix", "zip", "city", "street",
				"phone1", "phone2", "fax", "email", "website", "mobile", "comment"
			};
		
		for (int i = 0; i < control.length; i++) {
			bindValue(control[i], property[i], bindingContext);
		}
		
		// DateTime
		// TODO
		// IObservableValue dateTimeObserveWidget = SWTObservables.observeSelection(dateTimeDob);
		// IObservableValue dateTimeObserveValue =
		// BeansObservables.observeDetailValue(contactObservable, "dateOfBirth",
		// org.joda.time.DateTime.class);
		// UpdateValueStrategy targetToModel = new UpdateValueStrategy();
		// targetToModel.setConverter(new DateDateTimeConverter());
		// UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
		// modelToTarget.setConverter(new DateTimeDateConverter());
		// bindingContext.bindValue(dateTimeObserveWidget, dateTimeObserveValue, targetToModel,
		// modelToTarget);
		
		// Warum liegt denn hier Stroh rum?
		IObservableValue sexObserver = ViewersObservables.observeSingleSelection(comboViewerSex);
		bindingContext.bindValue(sexObserver,
			BeansObservables.observeDetailValue(contactObservable, "gender", ContactGender.class),
			null, null);
		
		IObservableValue countryObserver =
			ViewersObservables.observeSingleSelection(comboViewerCountry);
		bindingContext.bindValue(countryObserver,
			BeansObservables.observeDetailValue(contactObservable, "country", CountryCode.class),
			null, null);
	}
	
	/**
	 * This {@link FocusListener} sets and unsets the dial prefix of a country for phones.
	 */
	FocusListener preDialFocuListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent e){
			Text text = ((Text) e.widget);
			if (text.getText().equalsIgnoreCase(ContactGeonames.getDialPrefix()))
				text.setText("");
		}
		
		@Override
		public void focusGained(FocusEvent e){
			Text text = ((Text) e.widget);
			if (text.getText().length() == 0)
				text.setText(ContactGeonames.getDialPrefix());
		}
	};
	private Button btnWebsite;
	private Button btnEmail;
	
}
