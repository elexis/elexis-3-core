package ch.elexis.core.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.views.BriefAuswahl;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.TextView;
import ch.elexis.core.ui.views.controls.GenericSearchSelectionDialog;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class BriefNewHandler extends AbstractHandler implements IHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.commands.BriefNew";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Patient pat = ElexisEventDispatcher.getSelectedPatient();
		if (pat == null) {
			MessageDialog.openInformation(UiDesk.getTopShell(), Messages.BriefAuswahlNoPatientSelected,
					Messages.BriefAuswahlNoPatientSelected);
			return null;
		}

		Fall selectedFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
		if (selectedFall == null) {
			SelectFallDialog sfd = new SelectFallDialog(UiDesk.getTopShell());
			sfd.open();
			if (sfd.result != null) {
				ElexisEventDispatcher.fireSelectionEvent(sfd.result);
			} else {
				MessageDialog.openInformation(UiDesk.getTopShell(), Messages.TextView_NoCaseSelected, // $NON-NLS-1$
						Messages.TextView_SaveNotPossibleNoCaseAndKonsSelected); // $NON-NLS-1$
				return null;
			}
		}

		Konsultation selectedKonsultation = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
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
			Query<Brief> qbe = new Query<Brief>(Brief.class);
			qbe.add(Brief.FLD_TYPE, Query.EQUALS, Brief.TEMPLATE);
			qbe.add(Brief.FLD_KONSULTATION_ID, Query.NOT_EQUAL, "SYS");
			qbe.startGroup();
			qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, ElexisEventDispatcher.getSelectedMandator().getId());
			qbe.or();
			qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, StringTool.leer);
			qbe.endGroup();
			List<Brief> lBrief = qbe.execute();

			GenericSearchSelectionDialog dialog = new GenericSearchSelectionDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), lBrief,
					ch.elexis.core.l10n.Messages.DocumentSelectDialog_schooseTemplateForLetter,
					ch.elexis.core.ui.dialogs.Messages.DocumentSelectDialog_createLetterWithTemplate,
					ch.elexis.core.ui.dialogs.Messages.DocumentSelectDialog_pleaseSelectTemplateFromList, null,
					SWT.SINGLE);

			Brief result;
			String subject = null;
			Kontakt address = null;

			if (dialog.open() == Dialog.OK) {
				tv = (TextView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(TextView.ID);

				IStructuredSelection sel = (IStructuredSelection) dialog.getSelection();
				if ((sel != null) && (!sel.isEmpty())) {
					result = (Brief) sel.getFirstElement();
					subject = result.getBetreff();

					if (StringTool.isNothing(subject)) {
						subject = result.getBetreff();
					}
				}

				if (dialog.getSelection() != null) {
					tv.createDocument((Brief) dialog.getSelection().getFirstElement(), subject, address);
					tv.setName();

					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.findView(BriefAuswahl.ID);
					if (viewPart instanceof BriefAuswahl) {
						((BriefAuswahl) viewPart).refreshCV(CommonViewer.Message.update_keeplabels);
					}
				}
			}

		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return null;
	}

}
