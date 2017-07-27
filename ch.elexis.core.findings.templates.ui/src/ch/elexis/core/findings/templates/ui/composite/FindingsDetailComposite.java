package ch.elexis.core.findings.templates.ui.composite;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.templates.model.DataType;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.core.findings.templates.model.InputDataText;
import ch.elexis.core.findings.templates.model.ModelFactory;
import ch.elexis.core.findings.templates.model.ModelPackage;
import ch.elexis.core.findings.templates.model.Type;

@SuppressWarnings("unchecked")
public class FindingsDetailComposite extends Composite {
	private WritableValue<FindingsTemplate> item =
		new WritableValue<FindingsTemplate>(null, FindingsTemplate.class);
	private Text textTitle;
	private Text textUnit;
	private Text txtComma;
	private ComboViewer comboType;
	private ComboViewer comboInputData;
	private Label txtCodes;
	FindingsTemplate selection;
	private Composite compositeType;
	private Composite compositeInputData;
	private GridData minGd;
	
	public FindingsDetailComposite(Composite parent){
		super(parent, SWT.BORDER);
		this.setLayout(new GridLayout(2, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	public void createContents(){
		Label lblTitle = new Label(this, SWT.NONE);
		lblTitle.setText("Titel");
		minGd = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		minGd.widthHint = 80;
		lblTitle.setLayoutData(minGd);
		
		textTitle = new Text(this, SWT.BORDER);
		textTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label lblType = new Label(this, SWT.NONE);
		lblType.setText("Typ");
		
		comboType = new ComboViewer(this, SWT.BORDER);
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		comboType.setContentProvider(contentProvider);
		comboType.setLabelProvider(new ObservableMapLabelProvider(
			EMFProperties.value(ModelPackage.Literals.INPUT_DATA_GROUP__DATA_TYPE)
				.observeDetail(contentProvider.getKnownElements())));
		
		comboType.setContentProvider(ArrayContentProvider.getInstance());
		comboType.setLabelProvider(new LabelProvider());
		comboType.setInput(Type.VALUES);
		
		comboType.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				Type type = (Type) ((StructuredSelection) event.getSelection()).getFirstElement();
				selection.setType(type);
				switch (type) {
				
				case OBSERVATION:
					compositeType.setVisible(true);
					break;
				case CONDITION:
				case EVALUATION:
				case PROCEDURE:
					compositeType.setVisible(false);
					break;
				default:
					break;
				
				}
			}
		});
		
		Label lblCodes = new Label(this, SWT.NONE);
		lblCodes.setText("Codes");
		
		Composite compCodes = new Composite(this, SWT.NONE);
		compCodes.setLayout(new GridLayout(2, false));
		
		txtCodes = new Label(compCodes, SWT.NONE);
		txtCodes.setText("");
		
		Button button = new Button(compCodes, SWT.PUSH);
		button.setText("ändern");
		
		compositeType = new Composite(this, SWT.NONE);
		compositeType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		compositeType.setLayout(gl);
		
		createObservationComposite();
		
		DataBindingContext bindingContext = new DataBindingContext();
		IObservableValue<?> observeTextTitle = WidgetProperties.text(SWT.Modify).observe(textTitle);
		IObservableValue<?> observeValueTextTitle =
			EMFProperties.value(ModelPackage.Literals.FINDINGS_TEMPLATE__TITLE).observeDetail(item);
		bindingContext.bindValue(observeTextTitle, observeValueTextTitle);
	}
	
	public void createObservationComposite(){
		Label lblType = new Label(compositeType, SWT.NONE);
		lblType.setText("Datentyp");
		lblType.setLayoutData(minGd);
		comboInputData = new ComboViewer(compositeType, SWT.BORDER);
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		comboInputData.setContentProvider(contentProvider);
		comboInputData.setLabelProvider(new ObservableMapLabelProvider(
			EMFProperties.value(ModelPackage.Literals.INPUT_DATA_NUMERIC__DATA_TYPE)
				.observeDetail(contentProvider.getKnownElements())));
		
		comboInputData.setContentProvider(ArrayContentProvider.getInstance());
		comboInputData.setLabelProvider(new LabelProvider());
		comboInputData.setInput(DataType.VALUES);
		
		compositeInputData = new Composite(compositeType, SWT.NONE);
		compositeInputData.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		compositeInputData.setLayout(gl);
		
		comboInputData.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				updateInputData(
					(DataType) ((StructuredSelection) event.getSelection()).getFirstElement());
			}
		});
	}

	private void updateInputData(DataType dataType){
		for (Control c : compositeInputData.getChildren()) {
			c.dispose();
		}
		switch (dataType) {
		case GROUP:
			InputDataGroup inputDataGroup = selection.getInputData() instanceof InputDataGroup
					? (InputDataGroup) selection.getInputData()
					: ModelFactory.eINSTANCE.createInputDataGroup();
			selection.setInputData(inputDataGroup);
			break;
		case NUMERIC:
			InputDataNumeric inputDataNumeric = selection.getInputData() instanceof InputDataNumeric
					? (InputDataNumeric) selection.getInputData()
					: ModelFactory.eINSTANCE.createInputDataNumeric();
			Label lblUnit = new Label(compositeInputData, SWT.NONE);
			lblUnit.setText("Einheit");
			lblUnit.setLayoutData(minGd);
			textUnit = new Text(compositeInputData, SWT.BORDER);
			textUnit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			Label lblComma = new Label(compositeInputData, SWT.NONE);
			lblComma.setText("Kommastellen");
			
			txtComma = new Text(compositeInputData, SWT.BORDER);
			txtComma.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			DataBindingContext ctx = new DataBindingContext();
			IObservableValue<?> widgetValue =
				WidgetProperties.text(SWT.Modify).observe(txtComma);
			IObservableValue<?> modelValue =
				EMFProperties.value(ModelPackage.Literals.INPUT_DATA_NUMERIC__DECIMAL_PLACE)
					.observe(inputDataNumeric);
			
			EMFUpdateValueStrategy modelToTarget = new EMFUpdateValueStrategy();
			modelToTarget.setConverter(new Converter(Integer.class, String.class) {
				@Override
				public Object convert(Object fromObject){
					return "" + fromObject;
				}
			});
			EMFUpdateValueStrategy targetToModel = new EMFUpdateValueStrategy();
			targetToModel.setConverter(new Converter(String.class, Integer.class) {
				@Override
				public Object convert(Object fromObject){
					try {
						return Integer.parseInt((String) fromObject);
					} catch (NumberFormatException e) {
						return 0;
					}
				}
			});
			ctx.bindValue(widgetValue, modelValue, targetToModel, modelToTarget);
			
			widgetValue = WidgetProperties.text(SWT.Modify).observe(textUnit);
			modelValue = EMFProperties.value(ModelPackage.Literals.INPUT_DATA_NUMERIC__UNIT)
				.observe(inputDataNumeric);
			ctx.bindValue(widgetValue, modelValue);
			
			selection.setInputData(inputDataNumeric);
			compositeInputData.layout();
			break;
		case TEXT:
			InputDataText inputDataText = selection.getInputData() instanceof InputDataText
					? (InputDataText) selection.getInputData()
					: ModelFactory.eINSTANCE.createInputDataText();
			selection.setInputData(inputDataText);
			break;
		default:
			break;
		}
	}
	
	public void setSelection(FindingsTemplate findingsTemplate){
		this.selection = findingsTemplate;
		
		if (selection != null) {
			item.setValue(selection);
			txtCodes.setText(
				selection.getCode() == null ? "Nicht definiert" : selection.getCode().getLabel());
			
			comboType.setSelection(new StructuredSelection(selection.getType()));
			if (selection.getInputData() instanceof InputDataNumeric) {
				comboInputData.setSelection(new StructuredSelection(DataType.NUMERIC));
				
			} else if (selection.getInputData() instanceof InputDataText) {
				comboInputData.setSelection(new StructuredSelection(DataType.TEXT));
				
			} else if (selection.getInputData() instanceof InputDataGroup) {
				comboInputData.setSelection(new StructuredSelection(DataType.GROUP));
			}
			this.setVisible(true);
		} else {
			this.setVisible(false);
		}
	}
	
	public FindingsTemplate getSelection(){
		return selection;
	}
	
}
