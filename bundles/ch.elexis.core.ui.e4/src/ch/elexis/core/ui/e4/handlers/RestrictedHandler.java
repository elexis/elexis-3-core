package ch.elexis.core.ui.e4.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.CanExecute;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.services.IAccessControlService;

public class RestrictedHandler {

	@Inject
	private IAccessControlService accessControlService;

	private final EvaluatableACE acessControlEntity;

	public RestrictedHandler(EvaluatableACE acessControlEntity) {
		this.acessControlEntity = acessControlEntity;
	}

	@CanExecute
	public boolean canExecute() {
		return accessControlService.evaluate(acessControlEntity);
	}

}
