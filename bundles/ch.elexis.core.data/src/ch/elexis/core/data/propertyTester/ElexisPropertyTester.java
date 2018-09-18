package ch.elexis.core.data.propertyTester;


import org.eclipse.core.expressions.PropertyTester;

import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.services.ILocalLockService.Status;


public class ElexisPropertyTester extends PropertyTester {

	private static final String PROP_STANDALONE = "STANDALONE";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if (PROP_STANDALONE.equals(property))
		{
			return Status.STANDALONE.equals(LocalLockServiceHolder.get().getStatus());
		}
		return false;
	}
}
