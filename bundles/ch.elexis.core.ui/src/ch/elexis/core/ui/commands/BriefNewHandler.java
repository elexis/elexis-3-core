package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.views.BriefAuswahl;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.TextView;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.rgw.tools.ExHandler;

public class BriefNewHandler extends AbstractHandler implements IHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.commands.BriefNew";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Patient pat = ElexisEventDispatcher.getSelectedPatient();
		if (pat == null) {
			MessageDialog.openInformation(UiDesk.getTopShell(),
				Messages.BriefAuswahlNoPatientSelected, Messages.BriefAuswahlNoPatientSelected);
			return null;
		}
		
		Fall selectedFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
		if (selectedFall == null) {
			SelectFallDialog sfd = new SelectFallDialog(UiDesk.getTopShell());
			sfd.open();
			if (sfd.result != null) {
				ElexisEventDispatcher.fireSelectionEvent(sfd.result);
			} else {
				MessageDialog.openInformation(UiDesk.getTopShell(),
					Messages.TextView_NoCaseSelected, //$NON-NLS-1$
					Messages.TextView_SaveNotPossibleNoCaseAndKonsSelected); //$NON-NLS-1$
				return null;
			}
		}
		
		Konsultation selectedKonsultation =
			(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		if (selectedKonsultation == null) {
			Konsultation k = pat.getLetzteKons(false);
			if (k == null) {
				k = ((Fall) ElexisEventDispatcher.getSelected(Fall.class)).neueKonsultation();
				k.setMandant(CoreHub.actMandant);
			}
			ElexisEventDispatcher.fireSelectionEvent(k);
		}
		
		TextView tv = null;
		try {
			DocumentSelectDialog bs = new DocumentSelectDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				CoreHub.actMandant, DocumentSelectDialog.TYPE_CREATE_DOC_WITH_TEMPLATE);
			if (bs.open() == Dialog.OK) {
				tv = (TextView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(TextView.ID);
				// trick: just supply a dummy address for creating the doc
				Kontakt address = null;
				if (DocumentSelectDialog
					.getDontAskForAddresseeForThisTemplate(bs.getSelectedDocument()))
					address = Kontakt.load("-1"); //$NON-NLS-1$
				tv.createDocument(bs.getSelectedDocument(), bs.getBetreff(), address);
				tv.setName();
				
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(BriefAuswahl.ID);
				if (viewPart instanceof BriefAuswahl) {
					((BriefAuswahl) viewPart).refreshCV(CommonViewer.Message.update_keeplabels);
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return null;
	}
	
}
