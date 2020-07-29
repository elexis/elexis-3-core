package ch.elexis.core.mail.internal;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;

@Component
public class MailClientRunnableFactory implements IIdentifiedRunnableFactory {
	
	@Reference
	private IMailClient mailClient;
	
	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables(){
		return Collections.singletonList(new SendMailRunnable(mailClient));
	}
}
