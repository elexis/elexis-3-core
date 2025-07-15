package ch.elexis.core.mail.internal;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.tasks.model.ITaskService;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

@Startup
@Component(immediate = true)
public class MailClientRunnableFactory implements IIdentifiedRunnableFactory {

	@Inject
	@Reference
	ITaskService taskService;

	@Inject
	@Reference
	IMailClient mailClient;

	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables() {
		return Collections.singletonList(new SendMailRunnable(mailClient));
	}

	@PostConstruct
	@Activate
	public void activate() {
		taskService.bindIIdentifiedRunnableFactory(this);
	}

	@PreDestroy
	@Deactivate
	public void deactivate() {
		taskService.unbindIIdentifiedRunnableFactory(this);
	}
}
