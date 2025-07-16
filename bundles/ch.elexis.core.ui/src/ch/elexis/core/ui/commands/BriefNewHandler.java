package ch.elexis.core.ui.commands;

import java.util.List;
import java.util.Optional;

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

import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.views.BriefAuswahl;
import ch.elexis.core.ui.views.TextView;
import ch.elexis.core.ui.views.controls.GenericSearchSelectionDialog;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class BriefNewHandler extends AbstractHandler implements IHandler {
	public static final String CMD_ID = "ch.elexis.core.ui.commands.BriefNew"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPatient pat = ContextServiceHolder.get().getActivePatient().orElse(null);
		if (pat == null) {
			MessageDialog.openInformation(UiDesk.getTopShell(), Messages.Core_No_patient_selected,
					Messages.Core_No_patient_selected);
			return null;
		}

		ICoverage selectedFall = ContextServiceHolder.get().getActiveCoverage().orElse(null);
		if (selectedFall == null) {
			SelectFallDialog sfd = new SelectFallDialog(UiDesk.getTopShell());
			sfd.open();
			if (sfd.result != null) {
				selectedFall = NoPoUtil.loadAsIdentifiable(sfd.result, ICoverage.class).orElse(null);
				ContextServiceHolder.get().setActiveCoverage(selectedFall);
			} else {
				MessageDialog.openInformation(UiDesk.getTopShell(), Messages.TextView_NoCaseSelected, // $NON-NLS-1$
						Messages.TextView_SaveNotPossibleNoCaseAndKonsSelected); // $NON-NLS-1$
				return null;
			}
		}

		IEncounter selectedKonsultation = ContextServiceHolder.get().getTyped(IEncounter.class).orElse(null);
		if (selectedKonsultation == null) {
			selectedKonsultation = EncounterServiceHolder.get().getLatestEncounter(pat).orElse(null);
			if (selectedKonsultation == null) {
				Optional<IMandator> activeMandator = ContextServiceHolder.get().getActiveMandator();
				if (activeMandator.isPresent()) {
					selectedKonsultation = new IEncounterBuilder(CoreModelServiceHolder.get(), selectedFall,
							activeMandator.get()).buildAndSave();
					EncounterServiceHolder.get().addDefaultDiagnosis(selectedKonsultation);
				}
			}
			ContextServiceHolder.get().setTyped(selectedKonsultation);
		}

		TextView tv = null;
		try {
			Query<Brief> qbe = new Query<>(Brief.class);
			qbe.add(Brief.FLD_TYPE, Query.EQUALS, Brief.TEMPLATE);
			qbe.add(Brief.FLD_KONSULTATION_ID, Query.NOT_EQUAL, "SYS"); //$NON-NLS-1$
			qbe.startGroup();
			qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS,
					ContextServiceHolder.get().getActiveMandator().get().getId());
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

				IStructuredSelection sel = dialog.getSelection();
				if ((sel != null) && (!sel.isEmpty())) {
					result = (Brief) sel.getFirstElement();
					subject = result.getBetreff();

					if (StringTool.isNothing(subject)) {
						subject = result.getBetreff();
					}
				}

				if (dialog.getSelection() != null) {
					if (DocumentSelectDialog
							.getDontAskForAddresseeForThisTemplate((Brief) dialog.getSelection().getFirstElement()))
						address = Kontakt.load("-1"); //$NON-NLS-1$
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
