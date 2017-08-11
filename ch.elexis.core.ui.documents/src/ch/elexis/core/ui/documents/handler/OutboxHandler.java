package ch.elexis.core.ui.documents.handler;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.LoggerFactory;

import at.medevit.elexis.outbox.model.OutboxElementType;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.documents.service.OutboxElementServiceHolder;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;

public class OutboxHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		
		Patient elexisPatient = ElexisEventDispatcher.getSelectedPatient();
		Mandant elexisMandant = ElexisEventDispatcher.getSelectedMandator();
		
		if (elexisPatient != null && elexisMandant != null && elexisPatient.exists()
			&& elexisMandant.exists()) {
			Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof StructuredSelection
				&& !((StructuredSelection) selection).isEmpty()) {
				List<?> iDocuments = ((StructuredSelection) selection).toList();
				
				int size = 0;
				for (Object documentToExport : iDocuments) {
					if (documentToExport instanceof IDocument) {
						IDocument iDocument = (IDocument) documentToExport;
						if (createOutboxElement(elexisPatient, elexisMandant, iDocument)) {
							size++;
						} else {
							LoggerFactory.getLogger(getClass())
								.warn("Cannot create outbox element for document with id {}",
									iDocument.getId());
						}
					}
				}
				if (size > 0) {
					MessageDialog.openInformation(shell, "Dokumente",
						size == 1 ? "Das Dokument wurde erfolgreich in die Outbox abgelegt."
								: size + " Dokumente wurden erfolgreich in die Outbox abgelegt.");
				}
			}
		}
		return null;
	}
	
	private boolean createOutboxElement(Patient patient, Mandant mandant, IDocument document){
		Optional<IPersistentObject> po =
			DocumentStoreServiceHolder.getService().getPersistenceObject(document);
		if (po.isPresent()) {
			OutboxElementServiceHolder.getService().createOutboxElement(patient, mandant,
				OutboxElementType.DB.getPrefix() + DocumentStoreServiceHolder.getService()
					.getPersistenceObject(document).get().storeToString());
			return true;
		}
		return false;
		
	}
}
