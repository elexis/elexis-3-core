package ch.elexis.core.spotlight.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.program.Program;
import org.slf4j.LoggerFactory;

import ch.elexis.core.documents.DocumentStore;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IDocumentConverter;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.utils.OsgiServiceUtil;
import jakarta.inject.Inject;

@SuppressWarnings("restriction")
public class SpotlightUiUtil {

	public static final String ACTION_SHOW_LATEST_LABORATORY = "sll::";
	public static final String ACTION_SHOW_LATEST_ENCOUNTER = "sle::";
	public static final String ACTION_SHOW_FIXED_MEDICATION = "sfm::";
	// show dialog for a specific appointment, requires appointmentId
	public static final String ACTION_SHOW_APPOINTMENT = "sam::";
	// show patients balance requires patientId
	public static final String ACTION_SHOW_BALANCE = "sb::";

	@Inject
	private IContextService contextService;

	@Inject
	private DocumentStore documentStore;

	@Inject
	private IEncounterService encounterService;

	@Inject
	private ECommandService commandService;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private IEclipseContext eclipseContext;

	private EPartService partService;

	public SpotlightUiUtil(EPartService partService) {
		this.partService = partService;
	}

	private boolean handleEnter(ISpotlightResultEntry selected) {
		if (selected == null) {
			return false;
		}

		Category category = selected.getCategory();
		String objectId = selected.getLoaderString();

		switch (category) {
		case PATIENT:
			IPatient patient = CoreModelServiceHolder.get().load(objectId, IPatient.class).orElse(null);
			contextService.getRootContext().setTyped(patient);
			return true;
		case DOCUMENT:
			IDocument document = documentStore.loadDocument(objectId, documentStore.getDefaultDocumentStore().getId())
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
					LoggerFactory.getLogger(getClass()).info("Exception opening document [{}]", objectId, e);
				}
			}
			return true;
		case ENCOUNTER:
			IEncounter encounter = CoreModelServiceHolder.get().load(objectId, IEncounter.class).orElse(null);
			return handleEnter(encounter);
		default:
			System.out.println("No default enter action");
			return false;
		}
	}

	private boolean handleEnter(IEncounter encounter) {
		if (encounter != null) {
			contextService.getRootContext().setTyped(encounter.getPatient());
			contextService.getRootContext().setTyped(encounter);
			partService.showPart("ch.elexis.Konsdetail", PartState.ACTIVATE);
			return true;
		}
		return false;
	}

	private boolean handleEnter(IAppointment appointment) {
		if (appointment != null) {
			IContact contact = appointment.getContact();
			if (contact != null) {
				IPatient patient = CoreModelServiceHolder.get().load(contact.getId(), IPatient.class).orElse(null);
				contextService.getRootContext().setTyped(patient);
				return true;
			}
		}
		return false;
	}

	private boolean handleEnter(String string) {
		if (string.startsWith(Category.PATIENT.name())) {
			String patientId = string.substring(Category.PATIENT.name().length() + 2);
			IPatient patient = CoreModelServiceHolder.get().load(patientId, IPatient.class).orElse(null);
			if (patient == null) {
				System.out.println("Could not load patient " + patientId);
			}
			contextService.getRootContext().setTyped(patient);
			return patient != null;
		} else if (string.startsWith(ACTION_SHOW_BALANCE)) {
			return performActionShowBalance(string.substring(ACTION_SHOW_BALANCE.length()));

		} else if (string.startsWith(ACTION_SHOW_LATEST_LABORATORY)) {
			return performActionShowLatestLaboratory(string.substring(ACTION_SHOW_LATEST_LABORATORY.length()));

		} else if (string.startsWith(ACTION_SHOW_LATEST_ENCOUNTER)) {
			return performActionShowLatestEncounter(string.substring(ACTION_SHOW_LATEST_ENCOUNTER.length()));
		}
		return false;
	}

	private boolean performActionShowLatestEncounter(String patientId) {
		boolean ok = handleEnter(Category.PATIENT.name() + "::" + patientId);
		if (ok) {
			IPatient patient = contextService.getActivePatient().orElse(null);
			if (patient != null) {
				IEncounter latestEncounter = encounterService.getLatestEncounter(patient).orElse(null);
				if (latestEncounter != null) {
					return handleEnter(latestEncounter);
				}
			}
		}
		return false;
	}

	private boolean performActionShowLatestLaboratory(String patientId) {
		boolean ok = handleEnter(Category.PATIENT.name() + "::" + patientId);
		if (ok) {
			partService.showPart("ch.elexis.Labor", PartState.ACTIVATE);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean handleEnter(Object selectedElement) {
		if (selectedElement instanceof IAppointment) {
			return handleEnter((IAppointment) selectedElement);
		} else if (selectedElement instanceof IEncounter) {
			return handleEnter((IEncounter) selectedElement);
		} else if (selectedElement instanceof ISpotlightResultEntry) {
			return handleEnter((ISpotlightResultEntry) selectedElement);
		} else if (selectedElement instanceof Supplier<?>) {
			return ((Supplier<Boolean>) selectedElement).get();
		} else if (selectedElement instanceof String) {
			return handleEnter((String) selectedElement);
		}
		return false;
	}

	private boolean performActionShowBalance(String patientId) {
		boolean ok = handleEnter(Category.PATIENT.name() + "::" + patientId);
		if (ok) {
			ParameterizedCommand command = commandService.createCommand(
					"ch.elexis.core.ui.e4.command.part.show.invoicelist",
					Collections.singletonMap("filterOnCurrentPatient", "true"));
			// needs EPartService of main window https://stackoverflow.com/a/50861257/905817
			eclipseContext.set(EPartService.class, partService);
			handlerService.executeHandler(command, eclipseContext);
			return true;
		}
		return false;
	}

	/**
	 * Handles the selection of a document in the Spotlight search and initiates the
	 * PDF preview update. This method determines whether the selected element from
	 * the Spotlight search is a document. If so, it enables the PDF preview
	 * composite for displaying the document. For non-document categories, it
	 * disables the PDF preview composite. It then retrieves the document using the
	 * document's ID and updates the PDF preview in the SpotlightShell.
	 *
	 * @param firstElement    The selected element from the Spotlight search
	 *                        results.
	 * @param _spotlightShell The instance of SpotlightShell used to update the PDF
	 *                        preview.
	 * @return true if the method completes successfully. This return value is
	 *         currently not used to indicate the success of document loading or
	 *         preview updating.
	 */
	public boolean handleDocumentSelectionAndPreview(Object firstElement, SpotlightShell _spotlightShell) {
		ISpotlightResultEntry selectedElement = (ISpotlightResultEntry) firstElement;
		String objectId = selectedElement.getLoaderString();
		IDocument document = documentStore.loadDocument(objectId, documentStore.getDefaultDocumentStore().getId())
				.orElse(null);
		if (document != null && "docx".equalsIgnoreCase(document.getExtension())) {
			Optional<IDocumentConverter> converterService = OsgiServiceUtil.getService(IDocumentConverter.class);
			if (converterService.isPresent() && converterService.get().isAvailable()) {
				try {
					Optional<File> pdfFile = converterService.get().convertToPdf(document);
					if (pdfFile.isPresent()) {
						FileInputStream pdfStream = new FileInputStream(pdfFile.get());
						_spotlightShell.updatePdfPreview(pdfStream);
						_spotlightShell.adjustShellSize(true);
						return true;
					}
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).error("Error converting document [" + document + "]", e);
				} finally {
					OsgiServiceUtil.ungetService(converterService.get());
				}
			}
		} else if (selectedElement.getCategory() == Category.DOCUMENT && document != null
				&& "pdf".equalsIgnoreCase(document.getExtension())) {
			try (InputStream pdfStream = document.getContent()) {
				_spotlightShell.updatePdfPreview(pdfStream);
				_spotlightShell.adjustShellSize(true);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			_spotlightShell.adjustShellSize(false);
		}
		return true;
	}
}