package ch.elexis.core.ui.databinding;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

public class SavingUpdateValueStrategy extends UpdateValueStrategy {
	private boolean autoSave;
	private IModelService modelService;
	
	public SavingUpdateValueStrategy(IModelService modelService){
		this.modelService = modelService;
		this.autoSave = true;
	}
	
	public void setAutoSave(boolean value){
		autoSave = value;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected IStatus doSet(IObservableValue observableValue, Object value){
		IStatus ret = super.doSet(observableValue, value);
		if (autoSave) {
			if (observableValue.getValue() instanceof Identifiable) {
				modelService.save((Identifiable) observableValue.getValue());
			}
		}
		return ret;
	}
}
