package ch.elexis.core.mail.ui.property;

import ch.elexis.core.mail.ui.client.MailClientComponent;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	
	public PropertyTester(){
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("available".equals(property)) { //$NON-NLS-1$
			return MailClientComponent.getMailClient() != null;
		}
		return false;
	}
}
