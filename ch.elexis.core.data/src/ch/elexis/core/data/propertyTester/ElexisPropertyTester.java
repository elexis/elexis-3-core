package ch.elexis.core.data.propertyTester;


import org.eclipse.core.expressions.PropertyTester;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.ILocalLockService.Status;

public class ElexisPropertyTester extends PropertyTester {

	private static final String PROP_STANDALONE = "STANDALONE";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if (PROP_STANDALONE.equals(property))
		{
			return Status.STANDALONE.equals(CoreHub.getLocalLockService().getStatus());
		}
		return false;
	}
}
