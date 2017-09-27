package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;

public class FallCopyCommand extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(FallCopyCommand.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IWorkbenchPage activePage =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		FallDetailView fallDetailView = (FallDetailView) activePage.findView(FallDetailView.ID);
		Fall activeFall = fallDetailView.getActiveFall();
		if (activeFall == null)
			return null;
		Fall clone = createFallClone(activeFall);
		
		// only ask for consultation transfer if case is still open 
		if (activeFall.isOpen()) {
			Konsultation[] consultations = activeFall.getBehandlungen(true);
			if (consultations != null && consultations.length > 0) {
				boolean transferCons =
					SWTHelper.askYesNo(Messages.FallCopyCommand_RelatedConsultations,
						Messages.FallCopyCommand_TransferConsultations
							+ Messages.FallCopyCommand_AttentionTransferConsultations);
				if (transferCons) {
					logger.debug("trying to transfer consulations");
					for (Konsultation cons : consultations) {
						if (cons.isEditable(false)) {
							cons.setFall(clone);
						}
					}
				}
			}
		}
		return null;
	}
	
	private Fall createFallClone(Fall fall){
		return fall.createCopy();
	}
}
