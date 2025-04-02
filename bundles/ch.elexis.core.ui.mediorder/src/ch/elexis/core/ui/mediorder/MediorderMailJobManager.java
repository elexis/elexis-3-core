package ch.elexis.core.ui.mediorder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.MailConstants;
import ch.elexis.core.mail.MailTextTemplate;
import ch.elexis.core.mail.PreferenceConstants;
import ch.elexis.core.mail.ui.handlers.SendMailNoUiHandler;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.ITextReplacementService;

public class MediorderMailJobManager {

	private final Set<IPatient> pendingPatients = ConcurrentHashMap.newKeySet();
	private final AtomicBoolean isJobRunning = new AtomicBoolean(false);
	private Logger logger = LoggerFactory.getLogger(MediorderMailJobManager.class);

	private IModelService coreModelService;
	private IContextService contextService;
	private IStickerService stickerService;
	private IConfigService configService;
	private ITextReplacementService textReplacementService;

	public MediorderMailJobManager(IModelService coreModelService, IContextService contextService,
			IStickerService stickerService, IConfigService configService,
			ITextReplacementService textReplacementService) {
		this.coreModelService = coreModelService;
		this.contextService = contextService;
		this.stickerService = stickerService;
		this.configService = configService;
		this.textReplacementService = textReplacementService;
	}

	/**
	 * Send a mail to the patient as soon as all entries have
	 * {@link MediorderEntryState#IN_STOCK}. {@link ISticker} mediorderSendMail is
	 * used to check if the email already has been sent. Ensures no parallel jobs
	 * are running, new patients will be processed in the next cycle.
	 * 
	 * @param patients
	 */
	public void scheduleMailJob(List<IPatient> patients) {
		Optional<ISticker> sticker = coreModelService.load(Constants.MEDIORDER_MAIL_STICKER, ISticker.class);
		if (sticker.isEmpty()) {
			logger.error("no mediorderMailSent sticker found");
			return;
		}

		ISticker mediorderMailSticker = sticker.get();
		pendingPatients.addAll(patients);
		if (!isJobRunning.compareAndSet(false, true)) {
			logger.debug("Mail job is already running patients will be processed");
			return;
		}

		Job mailJob = new Job("mediorderMailSent") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					while (!pendingPatients.isEmpty()) {
						Set<IPatient> patientsToProcess = new HashSet<>(pendingPatients);
						pendingPatients.removeAll(patientsToProcess);

						for (IPatient patient : patientsToProcess) {
							if (!stickerService.hasSticker(patient, mediorderMailSticker)) {
								try {
									ExecutionEvent event = new ExecutionEvent(null, setParameters(configService,
											contextService, textReplacementService, patient), null, null);
									if (new SendMailNoUiHandler().execute(event) == null) {
										stickerService.addSticker(mediorderMailSticker, patient);
									}
								} catch (ExecutionException e) {
									logger.warn("Error sending mail to patient {} : {}", patient.getLabel(),
											e.getMessage());
								}
							}
						}
					}
				} finally {
					isJobRunning.set(false);
				}
				return Status.OK_STATUS;
			}
		};
		mailJob.schedule();
	}

	private Map<String, String> setParameters(IConfigService configService, IContextService contextService,
			ITextReplacementService textReplacementService, IPatient patient) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("ch.elexis.core.mail.ui.sendMailNoUi.accountid",
				configService.get(PreferenceConstants.PREF_DEFAULT_MAIL_ACCOUNT, null));
		parameters.put("ch.elexis.core.mail.ui.sendMailNoUi.to", patient.getEmail());
		Optional<ITextTemplate> template = MailTextTemplate.load(Constants.MEDIORDER_MAIL_TEMPLATE);
		if (template.isPresent()) {
			String subject = (String) template.get().getExtInfo(MailConstants.TEXTTEMPLATE_SUBJECT);
			parameters.put("ch.elexis.core.mail.ui.sendMailNoUi.subject",
					subject == null ? template.get().getName() : subject);

			IContext context = contextService.createNamedContext("mediorder_mail_context");
			context.setTyped(patient);
			String preparedText = textReplacementService.performReplacement(context, template.get().getTemplate());
			parameters.put("ch.elexis.core.mail.ui.sendMailNoUi.text", preparedText);
		}

		return parameters;
	}

}
