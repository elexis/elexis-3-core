package ch.elexis.core.ui.databinding;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

public class SavingUpdateValueStrategy<S, D> extends UpdateValueStrategy<S, D> {
	private boolean autoSave;
	private IModelService modelService;
	private IObservableValue<?> observable;
	
	public SavingUpdateValueStrategy(IModelService modelService, IObservableValue<?> observable){
		this.modelService = modelService;
		this.observable = observable;
		this.autoSave = true;
	}
	
	public SavingUpdateValueStrategy setAutoSave(boolean value){
		autoSave = value;
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected IStatus doSet(IObservableValue observableValue, Object value){
		IStatus ret = super.doSet(observableValue, (D) value);
		if (autoSave) {
			if (observable.getValue() instanceof Identifiable) {
				modelService.save((Identifiable) observable.getValue());
			}
		}
		return ret;
	}
}
