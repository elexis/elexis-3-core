package ch.elexis.core.findings.ui.composites;

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
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.data.Patient;

public class FamilyAnamnesisComposite extends Composite {
	
	private StyledText textOberservation = null;
	
	protected WritableValue item = new WritableValue(null, FamAnamnesisText.class);
	
	public FamilyAnamnesisComposite(Composite parent, int style){
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
	
	public void setInput(Optional<IFamilyMemberHistory> input){
		if (textOberservation != null) {
			item.setValue(new FamAnamnesisText(input.isPresent() ? input.get() : null));
		}
	}
	
	protected void initDataBindings(){
		DataBindingContext bindingContext = new DataBindingContext();
		IObservableValue target =
			WidgetProperties.text(SWT.Modify).observeDelayed(1500, textOberservation);
		IObservableValue model =
			PojoProperties.value(FamAnamnesisText.class, "text", String.class).observeDetail(item);
		
		bindingContext.bindValue(target, model, null, null);
	}
	
	class FamAnamnesisText {
		IFamilyMemberHistory iFamilyMemberHistory;
		
		public FamAnamnesisText(IFamilyMemberHistory iFamilyMemberHistory){
			super();
			this.iFamilyMemberHistory = iFamilyMemberHistory;
		}
		
		public void setText(String text){
			if (iFamilyMemberHistory == null && text != null && text.length() > 0) {
				Patient patient = ElexisEventDispatcher.getSelectedPatient();
				if (patient != null && patient.exists()) {
					iFamilyMemberHistory =
						FindingsServiceComponent.getService().getFindingsFactory()
							.createFamilyMemberHistory();
					iFamilyMemberHistory.setPatientId(patient.getId());
				}
			}
			
			if (iFamilyMemberHistory != null) {
				iFamilyMemberHistory.setText(text);
			}
		}
		
		public String getText(){
			if (iFamilyMemberHistory != null) {
				return iFamilyMemberHistory.getText().orElse("");
			}
			return "";
		}
	}
}
