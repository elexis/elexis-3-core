package ch.elexis.core.ui.laboratory.controls.util;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.elexis.core.ui.laboratory.controls.Messages;
import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LaborResultsLabelProvider extends ColumnLabelProvider {
	
	private TreeViewerColumn column;
	
	public LaborResultsLabelProvider(TreeViewerColumn column){
		this.column = column;
	}
	
	@Override
	public String getText(Object element){
		if (element instanceof LaborItemResults) {
			TimeTool date =
				(TimeTool) column.getColumn().getData(LaborResultsComposite.COLUMN_DATE_KEY);
			if (date != null) {
				List<LabResult> results =
					((LaborItemResults) element).getResult(date.toString(TimeTool.DATE_COMPACT));
				if (results != null) {
					StringBuilder sb = new StringBuilder();
					for (LabResult labResult : results) {
						if (sb.length() == 0) {
							sb.append(getResultString(labResult));
						} else {
							sb.append(" / "); //$NON-NLS-1$
							sb.append(getResultString(labResult));
						}
					}
					return sb.toString();
				}
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	private String getResultString(LabResult labResult){
		if (labResult.getItem().getTyp() == typ.DOCUMENT) {
			return Messages.LaborResultsComposite_Open;
		} else if (labResult.getItem().getTyp() == typ.TEXT) {
			return getNonEmptyResultString(labResult);
		} else {
			int digits = labResult.getItem().getDigits();
			String result = getNonEmptyResultString(labResult);
			if (digits == 0) {
				return result;
			} else {
				try {
					Float resultNumeric = Float.parseFloat(result);
					return String.format("%." + digits + "f", resultNumeric); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (NumberFormatException e) {
					return result;
				}
			}
		}
	}
	
	private String getNonEmptyResultString(LabResult labResult){
		String result = labResult.getResult();
		if (result != null && result.isEmpty()) {
			result = "?"; //$NON-NLS-1$
		}
		if (labResult.getItem().getTyp() == typ.TEXT) {
			if (labResult.isLongText()) {
				result = labResult.getComment();
				if (result.length() > 20) {
					result = result.substring(0, 20);
				}
			}
		}
		return result;
	}
	
	private String getUnitAndReferenceString(LabResult labResult){
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(labResult.getUnit()).append("]");
		if (labResult.getPatient().getGeschlecht().equals(Patient.MALE)) {
			sb.append("[").append(labResult.getRefMale()).append("]");
		} else {
			sb.append("[").append(labResult.getRefFemale()).append("]");
		}
		return sb.toString();
	}
	
	private String getCommentString(LabResult labResult){
		StringBuilder sb = new StringBuilder();
		String comment = labResult.getComment();
		if (!comment.isEmpty()) {
			sb.append("\n").append(comment);
		}
		return sb.toString();
	}
	
	@Override
	public String getToolTipText(Object element){
		if (element instanceof LaborItemResults) {
			TimeTool date =
				(TimeTool) column.getColumn().getData(LaborResultsComposite.COLUMN_DATE_KEY);
			if (date != null) {
				List<LabResult> results =
					((LaborItemResults) element).getResult(date.toString(TimeTool.DATE_COMPACT));
				if (results != null) {
					StringBuilder sb = new StringBuilder();
					for (LabResult labResult : results) {
						TimeTool time = labResult.getObservationTime();
						if (time == null) {
							time = labResult.getDateTime();
						}
						if (sb.length() == 0) {
							sb.append(time.toString(TimeTool.TIME_FULL));
							sb.append(" - "); //$NON-NLS-1$
							sb.append(getResultString(labResult));
							sb.append(getUnitAndReferenceString(labResult));
							sb.append(getCommentString(labResult));
						} else {
							sb.append(",\n"); //$NON-NLS-1$
							sb.append(time.toString(TimeTool.TIME_FULL));
							sb.append(" - "); //$NON-NLS-1$
							sb.append(getResultString(labResult));
							sb.append(getUnitAndReferenceString(labResult));
							sb.append(getCommentString(labResult));
						}
					}
					return sb.toString();
				}
			}
		}
		return null;
	}
	
	@Override
	public Color getForeground(Object element){
		if (element instanceof LaborItemResults) {
			TimeTool date =
				(TimeTool) column.getColumn().getData(LaborResultsComposite.COLUMN_DATE_KEY);
			if (date != null) {
				List<LabResult> results =
					((LaborItemResults) element).getResult(date.toString(TimeTool.DATE_COMPACT));
				if (results != null) {
					boolean pathologic = false;
					for (LabResult labResult : results) {
						if (labResult.isFlag(LabResult.PATHOLOGIC)) {
							pathologic = true;
							break;
						}
					}
					if (pathologic) {
						return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
					}
				}
			}
		}
		return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	}
}
