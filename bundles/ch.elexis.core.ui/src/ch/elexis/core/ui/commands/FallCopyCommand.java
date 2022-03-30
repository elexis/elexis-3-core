package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.rgw.tools.Result;

public class FallCopyCommand extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(FallCopyCommand.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		FallDetailView fallDetailView = (FallDetailView) activePage.findView(FallDetailView.ID);
		Fall activeFall = fallDetailView.getActiveFall();
		if (activeFall != null) {
			ICoverage clone = createFallClone(NoPoUtil.loadAsIdentifiable(activeFall, ICoverage.class).orElse(null));
			if (clone != null) {
				// only ask for consultation transfer if case is still open
				if (activeFall.isOpen()) {
					Konsultation[] consultations = activeFall.getBehandlungen(true);
					if (consultations != null && consultations.length > 0) {
						boolean transferCons = SWTHelper.askYesNo(Messages.FallCopyCommand_RelatedConsultations,
								Messages.FallCopyCommand_TransferConsultations
										+ Messages.FallCopyCommand_AttentionTransferConsultations);
						if (transferCons) {
							logger.debug("trying to transfer consulations");
							for (Konsultation cons : consultations) {
								if (cons.isEditable(false)) {
									Result<IEncounter> result = EncounterServiceHolder.get().transferToCoverage(
											NoPoUtil.loadAsIdentifiable(cons, IEncounter.class).get(), clone, false);
									if (!result.isOK()) {
										SWTHelper.alert("Error", result.toString());
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private ICoverage createFallClone(ICoverage fall) {
		if (fall != null) {
			return CoverageServiceHolder.get().createCopy(fall);
		}
		return null;
	}
}
