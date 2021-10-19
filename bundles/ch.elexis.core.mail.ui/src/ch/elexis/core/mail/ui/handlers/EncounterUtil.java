package ch.elexis.core.mail.ui.handlers;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.ui.cons.KonsExtension;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.data.Konsultation;

public class EncounterUtil {
	
	private static Map<String, IIdentifiedRunnable> getIdentifiedRunnablesMap(){
		List<IIdentifiedRunnable> available = TaskServiceHolder.get().getIdentifiedRunnables();
		if (available != null && !available.isEmpty()) {
			Map<String, IIdentifiedRunnable> ret = new HashMap<>();
			available.stream().forEach(ir -> ret.put(ir.getId(), ir));
			return ret;
		}
		return Collections.emptyMap();
	}
	
	public static void addMailToEncounter(ITaskDescriptor taskDescriptor){
		Optional<IEncounter> encounter = getActiveEncounter();
		if (encounter.isPresent()) {
			String label = getTaskDescriptorText(taskDescriptor); //$NON-NLS-1$ //$NON-NLS-2$
			Konsultation.load(encounter.get().getId()).addXRef(KonsExtension.EXTENSION_ID,
				taskDescriptor.getId(), -1, label);
		}
	}
	
	public static String getTaskDescriptorText(ITaskDescriptor taskDescriptor){
		StringBuilder sb = new StringBuilder();
		
		if ("sendMailFromContext".equals(taskDescriptor.getIdentifiedRunnableId())) {
			Optional<ITask> execution = TaskServiceHolder.get().findLatestExecution(taskDescriptor);
			MailMessage msg = MailMessage.fromMap((Map) taskDescriptor.getRunContext().get("message"));
			if (msg != null) {
				sb.append("Mail an ").append(msg.getTo());
				execution.ifPresent(task -> {
					sb.append(" versendet "
						+ task.getFinishedAt()
							.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
				});
				return sb.toString();
			}
		}
		return sb.toString();
	}
	
	private static Optional<IEncounter> getActiveEncounter(){
		Optional<IEncounter> ret = ContextServiceHolder.get().getRootContext().getTyped(IEncounter.class);
		Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();
		if (ret.isPresent() && patient.isPresent()
			&& ret.get().getCoverage().getPatient().equals(patient.get())) {
			return ret;
		}
		if (patient.isPresent()) {
			ret = EncounterServiceHolder.get().getLatestEncounter(patient.get());
			return ret;
		}
		return Optional.empty();
	}
}
