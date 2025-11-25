package ch.elexis.core.spotlight.ui.controls.detail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailComposite;
import ch.elexis.core.spotlight.ui.controls.AbstractSpotlightResultEntryDetailComposite;
import ch.elexis.core.spotlight.ui.controls.SpotlightSearchHelper;
import ch.elexis.core.spotlight.ui.internal.SpotlightShell;
import ch.elexis.core.spotlight.ui.internal.SpotlightUiUtil;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import jakarta.inject.Inject;

public class PatientDetailComposite extends AbstractSpotlightResultEntryDetailComposite
		implements ISpotlightResultEntryDetailComposite {

	private static final String PATIENT_LABEL_FONT = "patient-label-font";

	@Inject
	private IStickerService stickerService;
	@Inject
	private IEncounterService encounterService;

	private IModelService coreModelService;
	private PatientDetailCompositeUtil util;

	private Label lblPatientlabel;
	private Composite stickerComposite;
	private StyledText styledText;

	private IPatient selectedPatient;
	private IAppointment selectedNextAppointment;
	private IAppointment selectedPreviousAppointment;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 */
	public PatientDetailComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginTop = 0;
		setLayout(gridLayout);

		util = new PatientDetailCompositeUtil();
		coreModelService = CoreModelServiceHolder.get();

		lblPatientlabel = new Label(this, SWT.NONE);

		Font patientLabelFont;
		if (JFaceResources.getFontRegistry().hasValueFor(PATIENT_LABEL_FONT)) {
			patientLabelFont = JFaceResources.getFontRegistry().get(PATIENT_LABEL_FONT);
		} else {
			FontData[] fontData = lblPatientlabel.getFont().getFontData();
			fontData[0].setHeight(fontData[0].getHeight() + 1);
			JFaceResources.getFontRegistry().put(PATIENT_LABEL_FONT, fontData);
			patientLabelFont = JFaceResources.getFontRegistry().get(PATIENT_LABEL_FONT);
		}
		lblPatientlabel.setFont(patientLabelFont);
		lblPatientlabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		lblPatientlabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		lblPatientlabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		// Patient Stickers
		stickerComposite = new Composite(this, SWT.NONE);
		FillLayout fl_stickerComposite = new FillLayout(SWT.HORIZONTAL);
		fl_stickerComposite.spacing = 5;
		stickerComposite.setLayout(fl_stickerComposite);
		GridData gd_stickerComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_stickerComposite.heightHint = 16;
		stickerComposite.setLayoutData(gd_stickerComposite);

		styledText = new StyledText(this, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		styledText.setEditable(false);
		// TODO setEnabled?
		styledText.setBackground(getBackground());
		styledText.setTabStops(new int[] { 100, 350 });
		styledText.setLineSpacingProvider(lineIndex -> 3);
		styledText.addListener(SWT.MouseDown, event -> {
			int offset = styledText.getOffsetAtPoint(new Point(event.x, event.y));
			if (offset != -1) {
				try {
					StyleRange sr = styledText.getStyleRangeAtOffset(offset);
					if (sr != null && sr.data instanceof String) {
						String linkAction = (String) sr.data;
						handleAltKeyPressed(linkAction.charAt(0));
					}
				} catch (IllegalArgumentException e) {
					// no character under event.x, event.y
				}
			}
		});

		setSpotlightEntry(null);

	}

	@Override
	public boolean setFocus() {
		return styledText.setFocus();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void setSpotlightEntry(ISpotlightResultEntry resultEntry) {

		selectedPatient = null;
		selectedNextAppointment = null;
		selectedPreviousAppointment = null;
		clearPopulatePatientLabelComposite(null);
		clearPopulateStickerComposite(null);
		updateStyledText(null);

		if (resultEntry != null) {
			lblPatientlabel.setText("Lade Datensatz ...");
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (resultEntry != null) {
						Optional<Object> object = resultEntry.getObject();
						if (!object.isPresent()) {
							String patientId = resultEntry.getLoaderString();
							selectedPatient = coreModelService.load(patientId, IPatient.class).orElse(null);
						} else {
							selectedPatient = (IPatient) object.get();
						}
					}

					clearPopulatePatientLabelComposite(selectedPatient);
					clearPopulateStickerComposite(selectedPatient);
					updateStyledText(selectedPatient);

					layout(true);
				}
			});
		}
	}

	//@formatter:off
	private final String TEMPLATE = "Stammarzt\t{0}\r\n"
			+ "Versicherung\t{1}\r\n"
			+ "Konto\t{2}\t<m0>ALT+B</m>\r\n\r\n"
			+ "<lt>Nächster Termin</l>\t{3}\t<m0>ALT+T</m>\r\n"
			+ "<l0>Letzter Termin</l>\t{4}\r\n"
			+ "<ll>Letztes Labor</l>\t{5}\t<m0>ALT+L</m>\r\n\r\n"
			+ "<lk>Letzte Kons</l>\t{6}\t<m0>ALT+K</m>\r\n"
			+ "{7}\r\n\r\n"
			+ "<lm>Fixmedikation</l>\t\t<m0>ALT+M</m>\r\n"
			+ "{8}";
	//@formatter:on

	private void updateStyledText(IPatient patient) {

		styledText.setStyleRange(null);

		Object[] values = new Object[9];
		Arrays.fill(values, "-");

		values[0] = util.getFormattedFamilyDoctor(patient);
		values[1] = util.getFormattedInsurance(coreModelService, patient);
		values[2] = util.getFormattedPatientBalance(coreModelService, patient);

		selectedNextAppointment = util.getNextAppointment(coreModelService, patient);
		values[3] = util.getAppointmentLabel(selectedNextAppointment);

		selectedPreviousAppointment = util.getPreviousAppointment(coreModelService, patient);
		values[4] = util.getAppointmentLabel(selectedPreviousAppointment);

		values[5] = util.getFormattedLatestLaboratoryDate(coreModelService, patient);

		if (patient != null) {
			IEncounter _latestEncounter = encounterService.getLatestEncounter(patient).orElse(null);
			if (_latestEncounter != null) {
				values[6] = util.formatDate(_latestEncounter.getDate());
				String encounterText = _latestEncounter.getHeadVersionInPlaintext().trim().replaceAll(StringUtils.LF,
						StringUtils.SPACE);
				String[] encounterValue = new String[4];
				final int stepWidth = 65; // TODO calculate by dimension?
				encounterValue[0] = StringUtils.substring(encounterText, 0, stepWidth);
				encounterValue[1] = StringUtils.substring(encounterText, stepWidth, stepWidth * 2);
				encounterValue[2] = StringUtils.substring(encounterText, stepWidth * 2, stepWidth * 3);
				encounterValue[3] = StringUtils.substring(encounterText, stepWidth * 3, stepWidth * 4);
				values[7] = StringUtils.join(encounterValue, StringUtils.LF);
			}
		}

		values[8] = util.getFormattedFixedMedication(coreModelService, patient);

		String text = MessageFormat.format(TEMPLATE, values);
		StyleRange[] styleRanges = generateStyleRanges(text);
		String replaceAll = text.replaceAll("<(.+?)>", StringUtils.EMPTY);

		styledText.setText(replaceAll);
		styledText.setStyleRanges(styleRanges);
		SpotlightShell shell = (SpotlightShell) getShell();
		String currentSearchText = shell.getSearchText();
		int count = SpotlightSearchHelper.highlightSearchText(styledText, currentSearchText);
		shell.setlableText(count);
	}

	private final Pattern TAG_PATTERN = Pattern.compile("<([a-z])([0a-z])>(.+?)</[a-z]>");

	private StyleRange[] generateStyleRanges(String text) {
		List<StyleRange> ranges = new ArrayList<>();

		int matchedFormatChars = 0;

		Matcher matcher = TAG_PATTERN.matcher(text);
		while (matcher.find()) {
			StyleRange sr = new StyleRange();
			String tag = matcher.group(1).intern();
			String keybindingCode = matcher.group(2).intern();
			sr.start = matcher.start() - matchedFormatChars;
			sr.length = matcher.group(3).length();
			switch (tag) {
			case "l":
				sr.underline = true;
				sr.underlineStyle = SWT.UNDERLINE_LINK;
				sr.data = keybindingCode;
				break;
			case "m":
				sr.foreground = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
				break;
			default:
				break;
			}
			matchedFormatChars += 8; // the no of surrounding chars removed
			ranges.add(sr);
		}

		return ranges.toArray(new StyleRange[] {});
	}

	private void clearPopulateStickerComposite(IPatient patient) {
		util.clearComposite(stickerComposite);
		if (patient != null) {
			List<ISticker> stickers = stickerService.getStickers(patient);
			for (ISticker sticker : stickers) {
				Label label = new Label(stickerComposite, SWT.None);
				label.setForeground(CoreUiUtil.getColorForString(sticker.getForeground()));
				label.setBackground(CoreUiUtil.getColorForString(sticker.getBackground()));
				label.setText(sticker.getLabel());
			}
		}
		stickerComposite.layout();
	}

	private void clearPopulatePatientLabelComposite(IPatient patient) {
		if (patient == null) {
			lblPatientlabel.setText(StringUtils.EMPTY);
		} else {
			lblPatientlabel.setText(patient.getLabel() + " (" + patient.getAgeInYears() + " Jahre)");
		}
	}

	@Override
	public Category appliedForCategory() {
		return Category.PATIENT;
	}

	@Override
	public boolean handleAltKeyPressed(int keyCode) {
		Object selectedElement = null;

		if (selectedPatient != null) {
			switch (keyCode) {
			case 'b':
			case 'B':
				selectedElement = SpotlightUiUtil.ACTION_SHOW_BALANCE + selectedPatient.getId();
				break;
			case 't':
			case 'T':
				String id = (selectedNextAppointment != null) ? selectedNextAppointment.getId() : null;
				if (id != null) {
					selectedElement = SpotlightUiUtil.ACTION_SHOW_APPOINTMENT + id;
				}
				break;
			case 'k':
			case 'K':
				selectedElement = SpotlightUiUtil.ACTION_SHOW_LATEST_ENCOUNTER + selectedPatient.getId();
				break;
			case 'l':
			case 'L':
				selectedElement = SpotlightUiUtil.ACTION_SHOW_LATEST_LABORATORY + selectedPatient.getId();
				break;
			case 'm':
			case 'M':
				selectedElement = SpotlightUiUtil.ACTION_SHOW_FIXED_MEDICATION + selectedPatient.getId();
				break;
			default:
				break;
			}
		}

		if (selectedElement != null) {
			SpotlightShell shell = (SpotlightShell) getShell();
			shell.setSelectedElement(selectedElement);
			return shell.handleSelectedElement();
		}

		return false;
	}
}