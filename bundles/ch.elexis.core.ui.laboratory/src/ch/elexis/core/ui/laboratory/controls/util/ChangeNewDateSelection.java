package ch.elexis.core.ui.laboratory.controls.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.rgw.tools.TimeTool;

public class ChangeNewDateSelection extends SelectionAdapter {
	
	private TreeViewerColumn column;
	private LaborResultsComposite composite;

	public ChangeNewDateSelection(TreeViewerColumn column,
		LaborResultsComposite laborResultsComposite){
		this.column = column;
		this.composite = laborResultsComposite;
	}

	@Override
	public void widgetSelected(SelectionEvent e){
		TimeTool date =
			(TimeTool) column.getColumn().getData(LaborResultsComposite.COLUMN_DATE_KEY);
		DateSelectorDialog dsd =
			new DateSelectorDialog(composite.getShell(), date != null ? date : new TimeTool());
		if (dsd.open() == Dialog.OK) {
			TimeTool sel = dsd.getSelectedDate();
			column.getColumn().setData(LaborResultsComposite.COLUMN_DATE_KEY, sel);
			column.getColumn().setText("Neu (" + sel.toString(TimeTool.DATE_GER) + ")");
			composite.reload();
		}
	}
}
