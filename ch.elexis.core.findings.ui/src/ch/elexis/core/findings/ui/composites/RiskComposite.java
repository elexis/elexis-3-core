package ch.elexis.core.findings.ui.composites;

import java.util.Collections;
import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.data.Patient;

public class RiskComposite extends Composite {
	
	private StyledText textOberservation = null;
	
	protected WritableValue item = new WritableValue(null, RiskText.class);
	
	public RiskComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, false));
		textOberservation =
			new StyledText(this, SWT.NONE | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		textOberservation.setAlwaysShowScrollBars(true); //if false horizontal scrollbar blinks on typing
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.widthHint = 100;
		gd.heightHint = 100;
		textOberservation.setLayoutData(gd);
		initDataBindings();
	}
	
	public void setInput(Optional<IObservation> input){
		if (textOberservation != null) {
			item.setValue(new RiskText(input.isPresent() ? input.get() : null));
		}
	}
	
	protected void initDataBindings(){
		DataBindingContext bindingContext = new DataBindingContext();
		IObservableValue target =
			WidgetProperties.text(SWT.Modify).observeDelayed(1500, textOberservation);
		IObservableValue model =
			PojoProperties.value(RiskText.class, "text", String.class).observeDetail(item);
		
		bindingContext.bindValue(target, model, null, null);
	}
	
	class RiskText {
		IObservation iObservation;
		
		public RiskText(IObservation iObservation){
			super();
			this.iObservation = iObservation;
		}
		
		public void setText(String text){
			if (iObservation == null && text != null && text.length() > 0) {
				Patient patient = ElexisEventDispatcher.getSelectedPatient();
				if (patient != null && patient.exists()) {
					iObservation = FindingsServiceComponent.getService().create(IObservation.class);
					iObservation.setPatientId(patient.getId());
					iObservation.setCategory(ObservationCategory.SOCIALHISTORY);
					iObservation.setCoding(Collections
						.singletonList(new TransientCoding(ObservationCode.ANAM_RISK)));
				}
			}
			
			if (iObservation != null) {
				iObservation.setText(text);
			}
		}
		
		public String getText(){
			if (iObservation != null) {
				return iObservation.getText().orElse("");
			}
			return "";
		}
	}
}
