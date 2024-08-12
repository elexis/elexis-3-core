package ch.elexis.core.findings.ui.composites;

import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IAllergyIntolerance.AllergyIntoleranceCategory;
import ch.elexis.core.findings.ui.model.AllergyIntoleranceBeanAdapter;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;

public class AllergyIntoleranceComposite extends Composite {

	private ComboViewer categoryViewer;

	private StyledText textOberservation = null;

	protected WritableValue<AllergyIntoleranceBeanAdapter> item = new WritableValue<>();

	public AllergyIntoleranceComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		categoryViewer = new ComboViewer(this);
		categoryViewer.setContentProvider(new ArrayContentProvider());
		categoryViewer.setInput(AllergyIntoleranceCategory.values());
		categoryViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AllergyIntoleranceCategory) {
					return ((AllergyIntoleranceCategory) element).getLocalized();
				}
				return super.getText(element);
			}
		});

		textOberservation = new StyledText(this, SWT.NONE | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		textOberservation.setAlwaysShowScrollBars(true); // if false horizontal scrollbar blinks on typing
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.widthHint = 100;
		gd.heightHint = 100;
		textOberservation.setLayoutData(gd);
		initDataBindings();
	}

	public void setAllergyIntolerance(Optional<IAllergyIntolerance> input) {
		if (textOberservation != null) {
			AllergyIntoleranceBeanAdapter adapter = new AllergyIntoleranceBeanAdapter(
					input.isPresent() ? input.get()
							: FindingsServiceComponent.getService().create(IAllergyIntolerance.class));
			adapter.autoSave(true);
			item.setValue(adapter);
		}
	}

	public Optional<IAllergyIntolerance> getAllergyIntolerance() {
		if (item.getValue() != null) {
			return Optional.ofNullable((IAllergyIntolerance) item.getValue().getFinding());
		}
		return Optional.empty();
	}

	protected void initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();

		IViewerObservableValue<Object> targetObservable = ViewerProperties.singleSelection().observe(categoryViewer);
		IObservableValue<Object> modelObservable = PojoProperties.value(AllergyIntoleranceBeanAdapter.class, "category")
				.observeDetail(item);
		bindingContext.bindValue(targetObservable, modelObservable);

		ISWTObservableValue<String> target = WidgetProperties.text(SWT.Modify).observeDelayed(1500, textOberservation);
		IObservableValue<Object> model = PojoProperties.value(AllergyIntoleranceBeanAdapter.class, "text")
				.observeDetail(item);
		bindingContext.bindValue(target, model, null, null);
	}
}
