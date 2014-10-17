package ch.elexis.core.ui.laboratory.controls.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.dialogs.DateTimeSelectorDialog;
import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.elexis.data.LabResult;
import ch.rgw.tools.TimeTool;

public class ChangeResultsDateSelection extends SelectionAdapter {
	
	private TreeViewerColumn column;
	private LaborResultsComposite composite;

	public ChangeResultsDateSelection(TreeViewerColumn column,
		LaborResultsComposite laborResultsComposite){
		this.column = column;
		this.composite = laborResultsComposite;
	}

	@Override
	public void widgetSelected(SelectionEvent e){
		TimeTool date =
			(TimeTool) column.getColumn().getData(LaborResultsComposite.COLUMN_DATE_KEY);
		DateTimeSelectorDialog dsd =
			new DateTimeSelectorDialog(composite.getShell(), date != null ? date : new TimeTool());
		if (dsd.open() == Dialog.OK) {
			TimeTool sel = dsd.getSelectedDate();
			LabResult.changeObservationTime(ElexisEventDispatcher.getSelectedPatient(), date, sel);
			composite.reload();
		}
	}
}
