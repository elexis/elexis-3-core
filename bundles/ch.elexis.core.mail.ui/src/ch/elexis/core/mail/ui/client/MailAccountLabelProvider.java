package ch.elexis.core.mail.ui.client;

import org.eclipse.jface.viewers.LabelProvider;

public class MailAccountLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof String accountId) {
			return MailClientComponent.getMailboxAccountService().getAccount(accountId)
					.map(account -> account.displayName() + " <" + account.emailAddress() + ">") //$NON-NLS-1$ //$NON-NLS-2$
					.orElse(accountId);
		}
		return super.getText(element);
	}
}
