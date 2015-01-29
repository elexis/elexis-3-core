package ch.elexis.core.ui.laboratory.dialogs;

import java.util.Calendar;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.views.controls.LaborSelectionComposite;
import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

public class EditLabResultDialog extends TitleAreaDialog {
	
	private Composite editComposite;
	private Text resultTxt;
	private Text unitTxt;
	private Text refMaleTxt;
	private Text refFemaleTxt;
	private LaborSelectionComposite originSelection;
	private DateTime observationDate;
	private DateTime analyseDate;
	private DateTime transmissionDate;
	private DateTime observationTime;
	private DateTime analyseTime;
	private DateTime transmissionTime;
	
	private LabResult result;
	
	public EditLabResultDialog(Shell parentShell, LabResult act){
		super(parentShell);
		result = act;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		getShell().setText(Messages.EditLabResultDialog_shellTitle);
		setTitle(Messages.EditLabResultDialog_title);
		setMessage(String.format(Messages.EditLabResultDialog_message, result.getItem().getLabel(),
			result.getPatient().getLabel()));
		
		editComposite = new Composite(parent, SWT.NONE);
		editComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		editComposite.setLayout(new GridLayout(2, false));
		
		Label lbl = new Label(editComposite, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelValue);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		if (result.getItem().getTyp() == typ.TEXT) {
			resultTxt = new Text(editComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			data.heightHint = 100;
			resultTxt.setLayoutData(data);
		} else {
			resultTxt = new Text(editComposite, SWT.BORDER);
			resultTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		
		lbl = new Label(editComposite, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelUnit);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		unitTxt = new Text(editComposite, SWT.BORDER);
		unitTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		unitTxt.setTextLimit(80);
		
		lbl = new Label(editComposite, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelRefMale);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		refMaleTxt = new Text(editComposite, SWT.BORDER);
		refMaleTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		refMaleTxt.setTextLimit(80);
		
		lbl = new Label(editComposite, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelRefFemale);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		refFemaleTxt = new Text(editComposite, SWT.BORDER);
		refFemaleTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		refFemaleTxt.setTextLimit(80);
		
		lbl = new Label(editComposite, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelLab);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		originSelection = new LaborSelectionComposite(editComposite, SWT.NONE);
		originSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lbl = new Label(editComposite, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelTime);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Group grp = new Group(editComposite, SWT.NONE);
		grp.setLayout(new GridLayout(3, false));
		grp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lbl = new Label(grp, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelObservation);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		observationTime = new DateTime(grp, SWT.TIME);
		observationTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		observationDate = new DateTime(grp, SWT.DATE);
		observationDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lbl = new Label(grp, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelTransmission);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		transmissionTime = new DateTime(grp, SWT.TIME);
		transmissionTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		transmissionDate = new DateTime(grp, SWT.DATE);
		transmissionDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lbl = new Label(grp, SWT.NONE);
		lbl.setText(Messages.EditLabResultDialog_labelAnalyse);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		analyseTime = new DateTime(grp, SWT.TIME);
		analyseTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		analyseDate = new DateTime(grp, SWT.DATE);
		analyseDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		updateModelToTarget();
		
		return editComposite;
	}
	
	private String getUnit(){
		String ret = result.getUnit();
		if (ret == null || ret.isEmpty()) {
			ret = result.getItem().getEinheit();
		}
		return ret;
	}
	
	private String getRefMale(){
		String ret = result.getRefMale();
		if (ret == null || ret.isEmpty()) {
			ret = result.getItem().getRefM();
		}
		return ret;
	}
	
	private String getRefFemale(){
		String ret = result.getRefFemale();
		if (ret == null || ret.isEmpty()) {
			ret = result.getItem().getRefW();
		}
		return ret;
	}
	
	private TimeTool getTransmissionDate(){
		TimeTool ret = result.getTransmissionTime();
		if (ret == null) {
			ret = result.getDateTime();
		}
		return ret;
	}
	
	private TimeTool getAnalyseDate(){
		TimeTool ret = result.getAnalyseTime();
		if (ret == null) {
			ret = result.getDateTime();
		}
		return ret;
	}
	
	private TimeTool getSampleDate(){
		TimeTool ret = result.getObservationTime();
		if (ret == null) {
			ret = result.getDateTime();
		}
		return ret;
	}
	
	private void updateDateTimeModelToTarget(){
		TimeTool date = getTransmissionDate();
		transmissionTime.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
			date.get(Calendar.SECOND));
		transmissionDate.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
			date.get(Calendar.DAY_OF_MONTH));
		
		date = getSampleDate();
		observationTime.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
			date.get(Calendar.SECOND));
		observationDate.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
			date.get(Calendar.DAY_OF_MONTH));
		
		date = getAnalyseDate();
		analyseTime.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
			date.get(Calendar.SECOND));
		analyseDate.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
			date.get(Calendar.DAY_OF_MONTH));
	}
	
	private void updateDateTimeTargetToModel(){
		TimeTool date = new TimeTool();
		
		getTime(transmissionTime, date);
		getDate(transmissionDate, date);
		result.setTransmissionTime(date);
		
		getTime(observationTime, date);
		getDate(observationDate, date);
		result.setObservationTime(date);
		
		getTime(analyseTime, date);
		getDate(analyseDate, date);
		result.setAnalyseTime(date);
	}
	
	private void getTime(DateTime widget, TimeTool time){
		time.set(Calendar.HOUR_OF_DAY, widget.getHours());
		time.set(Calendar.MINUTE, widget.getMinutes());
		time.set(Calendar.SECOND, widget.getSeconds());
	}
	
	private void getDate(DateTime widget, TimeTool date){
		date.set(Calendar.YEAR, widget.getYear());
		date.set(Calendar.MONTH, widget.getMonth());
		date.set(Calendar.DAY_OF_MONTH, widget.getDay());
	}
	
	private void updateModelToTarget(){
		if (result != null) {
			if (result.getItem().getTyp() == typ.NUMERIC
				|| result.getItem().getTyp() == typ.ABSOLUTE) {
				resultTxt.setText(result.getResult());
				unitTxt.setText(getUnit());
				refMaleTxt.setText(getRefMale());
				refFemaleTxt.setText(getRefFemale());
			} else if (result.getItem().getTyp() == typ.TEXT) {
				if (result.isLongText()) {
					resultTxt.setText(result.getComment());
				} else {
					resultTxt.setText(result.getResult());
				}
				unitTxt.setText(getUnit());
				unitTxt.setEnabled(false);
				refMaleTxt.setText(getRefMale());
				refMaleTxt.setEnabled(false);
				refFemaleTxt.setText(getRefFemale());
				refFemaleTxt.setEnabled(false);
			}
			updateDateTimeModelToTarget();
			
			originSelection.setKontakt(result.getOrigin());
		}
	}
	
	private void updateTargetToModel(){
		if (result != null) {
			if (result.getItem().getTyp() == typ.NUMERIC
				|| result.getItem().getTyp() == typ.ABSOLUTE) {
				result.setResult(resultTxt.getText());
				
				result.setUnit(unitTxt.getText());
				result.setRefMale(refMaleTxt.getText());
				result.setRefFemale(refFemaleTxt.getText());
				
				updateDateTimeTargetToModel();
			} else if (result.getItem().getTyp() == typ.TEXT) {
				if (result.isLongText()) {
					result.setResult("text"); //$NON-NLS-1$
					result.set(LabResult.COMMENT, resultTxt.getText());
				} else {
					// convert to long text
					if (resultTxt.getText().length() < 200) {
						result.setResult(resultTxt.getText());
					} else {
						result.setResult("text"); //$NON-NLS-1$
						result.set(LabResult.COMMENT, resultTxt.getText());
					}
				}
				
				updateDateTimeTargetToModel();
			}
			result.setOrigin(originSelection.getKontakt());
		}
	}
	
	private boolean isValid(){
		if (resultTxt.getText().length() < 1) {
			setErrorMessage(Messages.EditLabResultDialog_errorNoResult);
			return false;
		}
		if (!resultTxt.getText().isEmpty() && result.getItem().getTyp() == typ.NUMERIC) {
			try {
				String numeric = resultTxt.getText();
				if (numeric.startsWith("<") || numeric.startsWith(">")) {
					numeric = numeric.substring(1, numeric.length());
				}
				Float.parseFloat(numeric);
			} catch (NumberFormatException e) {
				setErrorMessage(Messages.EditLabResultDialog_errorResultNotNumber);
				return false;
			}
		}
		
		if (!resultTxt.getText().isEmpty() && result.getItem().getTyp() == typ.ABSOLUTE) {
			if (!LabResult.isValidAbsoluteRefValue(resultTxt.getText())) {
				setErrorMessage(Messages.EditLabItemDialog_errorResultNotAbsolute);
				return false;
			}
		}
		
		if (!refMaleTxt.getText().isEmpty()) {
			if (result.getItem().getTyp() == typ.ABSOLUTE) {
				if (!LabResult.isValidAbsoluteRefValue(refMaleTxt.getText())) {
					setErrorMessage(Messages.EditLabItemDialog_errorRefMaleNotAbsolute);
					return false;
				}
			} else if (!LabResult.isValidNumericRefValue(refMaleTxt.getText())) {
				setErrorMessage(Messages.EditLabResultDialog_errorRefMaleNotNumber);
				return false;
			}
		}
		if (!refFemaleTxt.getText().isEmpty()) {
			if (result.getItem().getTyp() == typ.ABSOLUTE) {
				if (!LabResult.isValidAbsoluteRefValue(refFemaleTxt.getText())) {
					setErrorMessage(Messages.EditLabItemDialog_errorRefFemaleNotAbsolute);
					return false;
				}
			} else if (!LabResult.isValidNumericRefValue(refFemaleTxt.getText())) {
				setErrorMessage(Messages.EditLabResultDialog_errorRefFemaleNotNumber);
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	protected void okPressed(){
		if (!isValid()) {
			return;
		}
		updateTargetToModel();
		super.okPressed();
	}
}
