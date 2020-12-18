package ch.elexis.core.spotlight.ui.internal;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.swt.program.Program;
import org.slf4j.LoggerFactory;

import ch.elexis.core.documents.DocumentStore;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

public class SpotlightUiUtil {
	
	@Inject
	private IContextService contextService;
	
	@Inject
	private DocumentStore documentStore;
	
	public void handleEnter(ISpotlightResultEntry selected){
		if (selected == null) {
			return;
		}
		
		Category category = selected.getCategory();
		String objectId = selected.getLoaderString();
		
		switch (category) {
		case PATIENT:
			IPatient patient =
				CoreModelServiceHolder.get().load(objectId, IPatient.class).orElse(null);
			contextService.setActivePatient(patient);
			break;
		case DOCUMENT:
			IDocument document = documentStore
				.loadDocument(objectId, documentStore.getDefaultDocumentStore().getId())
				.orElse(null);
			if (document != null) {
				try {
					File tmpFile = File.createTempFile("doc", document.getExtension());
					tmpFile.deleteOnExit();
					documentStore.saveContentToFile(document, tmpFile.getAbsolutePath());
					Program program = Program.findProgram(document.getExtension());
					if (program != null) {
						program.execute(tmpFile.getAbsolutePath());
					}
				} catch (IOException | ElexisException e) {
					LoggerFactory.getLogger(getClass()).info("Exception opening document [{}]",
						objectId, e);
				}
			}
			break;
		case ENCOUNTER:
			IEncounter encounter =
				CoreModelServiceHolder.get().load(objectId, IEncounter.class).orElse(null);
			if (encounter != null) {
				contextService.getRootContext().setTyped(encounter);
			}
			// TODO open kons view?
			break;
		default:
			System.out.println("No default enter action");
			break;
		}
	}
	
}
