package ch.elexis.core.ui.importer.div.importers;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.data.interfaces.ILabResult;
import ch.elexis.core.importer.div.importers.ImportHandler;
import ch.elexis.core.importer.div.importers.TransientLabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.importer.div.importers.dialog.QueryOverwriteDialog;

public class DefaultLabImportUiHandler extends ImportHandler {
	
	@Override
	public OverwriteState askOverwrite(IPatient patient, ILabResult oldResult,
		TransientLabResult newResult){
		QueryOverwriteDialogRunnable runnable =
			new QueryOverwriteDialogRunnable(patient, oldResult, newResult);
		Display.getDefault().syncExec(runnable);
		int retVal = runnable.result;
		
		if (retVal == IDialogConstants.YES_TO_ALL_ID) {
			return OverwriteState.OVERWRITEALL;
		} else if (retVal == IDialogConstants.YES_ID) {
			return OverwriteState.OVERWRITE;
		}
		return OverwriteState.IGNORE;
	}
	
	/**
	 * Open overwrite dialog with a result value.
	 * 
	 * @author thomashu
	 */
	private class QueryOverwriteDialogRunnable implements Runnable {
		int result;
		private IPatient pat;
		private ILabResult oldResult;
		private TransientLabResult newResult;
		
		public QueryOverwriteDialogRunnable(IPatient pat, ILabResult oldResult,
			TransientLabResult newResult){
			this.pat = pat;
			this.oldResult = oldResult;
			this.newResult = newResult;
		}
		
		@Override
		public void run(){
			StringBuilder message = new StringBuilder();
			message.append("Alter Wert\n").append(oldResult.getLabel());
			message.append("\n");
			message.append("Neuer Wert\n").append(newResult.getLabel());
			message.append("\n\n");
			message.append(Messages.HL7Parser_AskOverwrite);
			QueryOverwriteDialog qod =
				new QueryOverwriteDialog(UiDesk.getTopShell(),
					Messages.HL7Parser_LabAlreadyImported + pat.getLabel(), message.toString());
			result = qod.open();
		}
	}
}
