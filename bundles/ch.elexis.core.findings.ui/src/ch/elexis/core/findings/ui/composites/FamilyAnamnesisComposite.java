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

import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.ui.model.AbstractBeanAdapter;
import ch.elexis.core.findings.ui.model.FamilyMemberHistoryBeanAdapter;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;

public class FamilyAnamnesisComposite extends Composite {

	private StyledText textOberservation = null;

	protected WritableValue<AbstractBeanAdapter<IFamilyMemberHistory>> item = new WritableValue<>();

	public FamilyAnamnesisComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		textOberservation = new StyledText(this, SWT.NONE | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		textOberservation.setAlwaysShowScrollBars(true); // if false horizontal scrollbar blinks on typing
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.widthHint = 100;
		gd.heightHint = 100;
		textOberservation.setLayoutData(gd);
		initDataBindings();
	}

	public void setInput(Optional<IFamilyMemberHistory> input) {
		if (textOberservation != null) {
			item.setValue(new FamilyMemberHistoryBeanAdapter(input.isPresent()
					? input.get()
					: FindingsServiceComponent.getService().create(IFamilyMemberHistory.class)).autoSave(true));
		}
	}

	protected void initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		IObservableValue target = WidgetProperties.text(SWT.Modify).observeDelayed(1500, textOberservation);
		IObservableValue model = PojoProperties.value(FamilyMemberHistoryBeanAdapter.class, "text", String.class)
				.observeDetail(item);

		bindingContext.bindValue(target, model, null, null);
	}
}
