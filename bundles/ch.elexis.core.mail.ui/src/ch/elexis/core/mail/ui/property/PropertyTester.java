package ch.elexis.core.mail.ui.property;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.ui.client.MailClientComponent;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	
	public PropertyTester(){
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("sendavailable".equals(property)) { //$NON-NLS-1$
			if (MailClientComponent.getMailClient() != null) {
				List<String> accounts = MailClientComponent.getMailClient().getAccounts();
				for (String string : accounts) {
					Optional<MailAccount> account =
						MailClientComponent.getMailClient().getAccount(string);
					if (account.isPresent()) {
						if (account.get().getType() == TYPE.SMTP) {
							return true;
						}
					}
				}
			}
			return false;
		}
		return false;
	}
}
