package ch.elexis.core.data.propertyTester;


import org.eclipse.core.expressions.PropertyTester;

import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;


public class ElexisPropertyTester extends PropertyTester {

	private static final String PROP_STANDALONE = "STANDALONE";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if (PROP_STANDALONE.equals(property))
		{
			return ConnectionStatus.STANDALONE.equals(ElexisServerServiceHolder.get().getConnectionStatus());
		}
		return false;
	}
}
