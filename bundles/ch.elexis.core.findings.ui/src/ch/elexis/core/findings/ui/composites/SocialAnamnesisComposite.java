package ch.elexis.core.findings.ui.composites;

import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.codes.TransientCoding;
import ch.elexis.core.findings.ui.model.AbstractBeanAdapter;
import ch.elexis.core.findings.ui.model.ObservationBeanAdapter;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;

public class SocialAnamnesisComposite extends Composite {

	private StyledText textObservation;
	protected WritableValue<AbstractBeanAdapter<IObservation>> item = new WritableValue<>();

	public SocialAnamnesisComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		textObservation = new StyledText(this, SWT.NONE | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		textObservation.setAlwaysShowScrollBars(true);

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 100;
		gridData.heightHint = 100;
		textObservation.setLayoutData(gridData);

		initDataBindings();
	}

	public void setInput(Optional<IObservation> input) {
		if (textObservation != null) {
			item.setValue(new ObservationBeanAdapter(
					input.isPresent() ? input.get() : FindingsServiceComponent.getService().create(IObservation.class))
					.category(ObservationCategory.SOCIALHISTORY)
					.coding(new TransientCoding(ObservationCode.ANAM_SOCIAL)).autoSave(true));
		}
	}

	protected void initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		ISWTObservableValue<String> target = WidgetProperties.text(SWT.Modify).observeDelayed(150, textObservation);
		IObservableValue<String> model = PojoProperties.value(AbstractBeanAdapter.class, "text", String.class)
				.observeDetail(item);
		bindingContext.bindValue(target, model, null, null);
	}
}