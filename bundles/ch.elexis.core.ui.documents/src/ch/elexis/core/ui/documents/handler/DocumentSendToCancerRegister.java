package ch.elexis.core.ui.documents.handler;

import static ch.elexis.core.constants.XidConstants.DOMAIN_AHV;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IXid;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.documents.fhir.FhirChCrlDocumentBundle;
import ch.elexis.data.Mandant;

public class DocumentSendToCancerRegister extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Optional<IPatient> activePatient = ContextServiceHolder.get().getActivePatient();
		if (activePatient.isPresent()) {
			IXid ahv = activePatient.get().getXid(DOMAIN_AHV);
			if (ahv == null || (ahv.getDomainId() == null || ahv.getDomainId().length() < 13)) {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(),
					"Senden an Krebsregister",
					"Der Patient hat keine AHV Nummer mit min. 13 Zeichen.");
				return null;
			}
			
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection instanceof StructuredSelection
				&& !((StructuredSelection) selection).isEmpty()) {
				IDocument iDocument =
					(IDocument) ((StructuredSelection) selection).getFirstElement();
				Optional<IMandator> author = getAuthor(iDocument);
				if (author.isPresent()) {
					FhirChCrlDocumentBundle fhirBundle = new FhirChCrlDocumentBundle(iDocument,
						activePatient.get(), author.get());
					try {
						fhirBundle.writeTo(new File(CoreHub.getWritableUserDir(), "krg_test.xml"));
					} catch (IOException e) {
						LoggerFactory.getLogger(getClass()).error("Couldn ot create file", e);
						MessageDialog.openInformation(Display.getDefault().getActiveShell(),
							"Senden an Krebsregister", "Datei konnte nicht erstellt werden");
					}
				} else {
					MessageDialog.openInformation(Display.getDefault().getActiveShell(),
						"Senden an Krebsregister", "Kein Autor Mandant ausgewählt");
				}
			}
		} else {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(),
				"Senden an Krebsregister", "Kein Patient ausgewählt");
		}
		return null;
	}
	
	private Optional<IMandator> getAuthor(IDocument iDocument){
		if (iDocument.getAuthor() != null && iDocument.getAuthor().isMandator()) {
			Optional<IMandator> ret =
				CoreModelServiceHolder.get().load(iDocument.getAuthor().getId(), IMandator.class);
			if (ret.isPresent()) {
				return ret;
			}
		} else {
			KontaktSelektor ksl =
				new KontaktSelektor(Display.getDefault().getActiveShell(), Mandant.class,
					"Mandant Auswahl",
					"Der Autor des Dokuments auswählen. Der Autor wird dann dem Dokument zugewiesen.",
					new String[] {
						Mandant.FLD_SHORT_LABEL, Mandant.FLD_NAME1, Mandant.FLD_NAME2
					});
			if (ksl.open() == Window.OK) {
				Mandant mandator = (Mandant) ksl.getSelection();
				if (mandator != null) {
					Optional<IMandator> mandatorContact =
						CoreModelServiceHolder.get().load(mandator.getId(), IMandator.class);
					mandatorContact.ifPresent(m -> iDocument.setAuthor(m));
					return mandatorContact;
				}
			}
		}
		return Optional.empty();
	}
}
