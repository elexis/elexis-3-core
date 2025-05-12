package ch.elexis.core.mediorder.mail.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.MailConstants;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.MailTextTemplate;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mediorder.MediorderUtil;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;

public class MediorderMailIdentifiedRunnable implements IIdentifiedRunnable {

	private IModelService coreModelService;
	private IContextService contextService;
	private ITextReplacementService textReplacementService;
	private IStockService stockService;
	private IStickerService stickerService;
	
	public MediorderMailIdentifiedRunnable(IModelService coreModelService, IContextService contextService,
			ITextReplacementService textReplacementService,
			IStockService stockService,
			IStickerService stickerser) {
		this.coreModelService = coreModelService;
		this.contextService = contextService;
		this.textReplacementService = textReplacementService;
		this.stockService = stockService;
		this.stickerService = stickerser;
	}
	
	@Override
	public String getId() {
		return Constants.RUNNABLE_ID;
	}

	@Override
	public String getLocalizedDescription() {
		return Constants.DESCRIPTION;
	}

	@Override
	public Map<String, Serializable> getDefaultRunContext() {
		Map<String, Serializable> defaultRunContext = new HashMap<>();
		defaultRunContext.put(Constants.RUNCONTEXT_ACCOUNT, RunContextParameter.VALUE_MISSING_REQUIRED);
		Optional<ITextTemplate> template = MailTextTemplate.load(Constants.MAIL_TEMPLATE_ID);
		defaultRunContext.put(Constants.RUNCONTEXT_TEMPLATE,
				template.map(ITextTemplate::getName).orElse(RunContextParameter.VALUE_MISSING_REQUIRED));
		return defaultRunContext;
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext, IProgressMonitor progressMonitor,
			Logger logger) throws TaskException {

		Optional<ISticker> sticker = coreModelService.load(ch.elexis.core.mediorder.Constants.MEDIORDER_MAIL_STICKER_ID,
				ISticker.class);
		if(sticker.isEmpty()) {
			throw new TaskException(TaskException.EXECUTION_ERROR, "no mediorderMailSend sticker found");
		}
		
		for (IStock stock : stockService.getAllPatientStock()) {
			IPatient patient = stock.getOwner().asIPatient();
			if (!stickerService.hasSticker(patient, sticker.get())) {
				if (MediorderUtil.calculateStockState(stock) == 1) {
					if (sendMediorderMail(runContext.get(Constants.RUNCONTEXT_ACCOUNT).toString(),
							createMailMessage(patient)).isOK()) {
						stickerService.addSticker(sticker.get(), patient);
					} else {
						throw new TaskException(TaskException.EXECUTION_ERROR, "mediorder mail not send");
					}
				}
			}
		}
		return null;
	}

	private MailMessage createMailMessage(IPatient patient) {
		Optional<ITextTemplate> template = MailTextTemplate.load(Constants.MAIL_TEMPLATE_ID);
		String subject = (String) template.get().getExtInfo(MailConstants.TEXTTEMPLATE_SUBJECT);
		IContext context = contextService.createNamedContext("mediorder_mail_context"); //$NON-NLS-1$
		context.setTyped(patient);
		String preparedText = textReplacementService.performReplacement(context, template.get().getTemplate());
		return new MailMessage().to(patient.getEmail())
				.subject(subject.isBlank() || subject == null ? template.get().getName() : subject).text(preparedText);
	}

	private IStatus sendMediorderMail(String accountId, MailMessage message) {
		Optional<ITaskDescriptor> taskDescriptor = TaskUtil.createSendMailTaskDescriptor(accountId, message);
		if (taskDescriptor.isEmpty()) {
			return Status.CANCEL_STATUS;
		}

		try {
			ITask task = TaskUtil.executeTaskSync(taskDescriptor.get(), new NullProgressMonitor());
			return task.isSucceeded() ? Status.OK_STATUS : Status.CANCEL_STATUS;
		} catch (TaskException e) {
			LoggerFactory.getLogger(TaskUtil.class).error("Error executing mail task", e);
			return Status.CANCEL_STATUS;
		}
	}
}
