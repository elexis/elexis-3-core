package ch.elexis.core.findings.templates.ui.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.templates.model.CodeElement;
import ch.elexis.core.findings.templates.model.DataType;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputData;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.core.findings.templates.model.InputDataText;
import ch.elexis.core.findings.templates.model.ModelFactory;
import ch.elexis.core.findings.templates.model.ModelPackage;
import ch.elexis.core.findings.templates.model.Type;
import ch.elexis.core.findings.templates.ui.dlg.FindingsSelectionDialog;
import ch.elexis.core.findings.templates.ui.util.FindingsServiceHolder;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;

@SuppressWarnings("unchecked")
public class FindingsDetailComposite extends Composite {
	private WritableValue<FindingsTemplate> item =
		new WritableValue<FindingsTemplate>(null, FindingsTemplate.class);
	private Text textTitle;
	private Text textUnit;
	private Text txtComma;
	private ComboViewer comboType;
	private ComboViewer comboInputData;
	FindingsTemplate selection;
	private Composite compositeType;
	private Composite compositeInputData;
	private GridData minGd;
	private FindingsTemplates model;
	private Text loincCode;
	private boolean openedFromDialog;

	private List<FindingsTemplate> findingTemplatesToMove = new ArrayList<>();
	
	public FindingsDetailComposite(Composite parent, FindingsTemplates model,
		boolean openedFromDialog){
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.model = model;
		this.openedFromDialog = openedFromDialog;
		findingTemplatesToMove.clear();
	}
	
	public void createContents(){
		Label lblTitle = new Label(this, SWT.NONE);
		lblTitle.setText("Titel");
		minGd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		minGd.widthHint = 100;
		lblTitle.setLayoutData(minGd);
		
		textTitle = new Text(this, SWT.BORDER);
		textTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label lblType = new Label(this, SWT.NONE);
		lblType.setText("Typ");
		
		comboType = new ComboViewer(this, SWT.BORDER | SWT.READ_ONLY);
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		comboType.setContentProvider(contentProvider);
		comboType.setLabelProvider(new ObservableMapLabelProvider(
			EMFProperties.value(ModelPackage.Literals.INPUT_DATA_GROUP__DATA_TYPE)
				.observeDetail(contentProvider.getKnownElements())));
		
		comboType.setContentProvider(ArrayContentProvider.getInstance());
		comboType.setLabelProvider(new ComboTypeLabelProvider());
		comboType.setInput(new Type[] {
			Type.OBSERVATION_VITAL
		});
		
		comboType.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				Type type = (Type) ((StructuredSelection) event.getSelection()).getFirstElement();
				selection.setType(type);
				
				if (type.getValue() < 100) {
					compositeType.setVisible(true);
				} else {
					compositeType.setVisible(false);
					selection.setInputData(null);
				}
			}
		});
		
		compositeType = new Composite(this, SWT.NONE);
		compositeType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		compositeType.setLayout(gl);
		
		WidgetFactory.createLabel(this, "LOINC"); //$NON-NLS-1$
		
		Composite codesComposite = new Composite(this, SWT.NONE);
		codesComposite.setLayout(SWTHelper.createGridLayout(true, 2));
		codesComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		loincCode = new Text(codesComposite, SWT.BORDER | SWT.READ_ONLY);
		loincCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		loincCode.setTextLimit(80);
		Button loincCodeSelection = new Button(codesComposite, SWT.PUSH);
		loincCodeSelection.setText("..."); //$NON-NLS-1$
		loincCodeSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				SelectionDialog dialog =
					CodeSelectorFactory.getSelectionDialog("LOINC", getShell(), //$NON-NLS-1$
						"ignoreErrors");
				if (dialog.open() == SelectionDialog.OK) {
					if (dialog.getResult() != null && dialog.getResult().length > 0) {
						selectCode(Optional.of((ICodeElement) dialog.getResult()[0]));
					}
					else {
						selectCode(Optional.empty());
					}
				}
			}
		});
		
		createObservationComposite();
		
		DataBindingContext bindingContext = new DataBindingContext();
		IObservableValue<?> observeTextTitle = WidgetProperties.text(SWT.Modify).observe(textTitle);
		IObservableValue<?> observeValueTextTitle =
			EMFProperties.value(ModelPackage.Literals.FINDINGS_TEMPLATE__TITLE).observeDetail(item);
		bindingContext.bindValue(observeTextTitle, observeValueTextTitle);
	}
	
	private void selectCode(Optional<ICodeElement> optionalCodeElement)
	{
		loincCode.setText("");
		loincCode.setToolTipText("");
		if (selection != null)
		{
			if (optionalCodeElement != null) {
				if (optionalCodeElement.isPresent()) {
					CodeElement codeElement = ModelFactory.eINSTANCE.createCodeElement();
					codeElement.setCode(optionalCodeElement.get().getCode());
					codeElement.setDisplay(optionalCodeElement.get().getText());
					codeElement.setSystem(optionalCodeElement.get().getCodeSystemName());
					selection.setCodeElement(codeElement);
					
				} else {
					selection.setCodeElement(null);
				}
			}
			
			if (selection.getCodeElement() != null) {
				loincCode.setText(
					selection.getCodeElement().getCode() + ": "
						+ selection.getCodeElement().getDisplay());
				loincCode.setToolTipText(selection.getCodeElement().getDisplay());
			}
		}
	}
	
	public void createObservationComposite(){
		Label lblType = new Label(compositeType, SWT.NONE);
		lblType.setText("Datentyp");
		lblType.setLayoutData(minGd);
		comboInputData = new ComboViewer(compositeType, SWT.BORDER | SWT.READ_ONLY);
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		comboInputData.setContentProvider(contentProvider);
		comboInputData.setLabelProvider(new ObservableMapLabelProvider(
			EMFProperties.value(ModelPackage.Literals.INPUT_DATA_NUMERIC__DATA_TYPE)
				.observeDetail(contentProvider.getKnownElements())));
		
		comboInputData.setContentProvider(ArrayContentProvider.getInstance());
		comboInputData.setLabelProvider(new ComboInputDataTypeLabelProvider());
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
				updateSize();
			}
		});
	}
	
	private void updateSize(){
		
		layout(true, true);
	}

	private void updateInputData(DataType dataType){
		for (Control c : compositeInputData.getChildren()) {
			c.dispose();
		}
		layout(true, true);
		switch (dataType) {
		case GROUP:
			InputDataGroup inputDataGroup = selection.getInputData() instanceof InputDataGroup
					? (InputDataGroup) selection.getInputData()
					: ModelFactory.eINSTANCE.createInputDataGroup();

			
			Label lblGroup = new Label(compositeInputData, SWT.NONE);
			lblGroup.setText("Gruppe (Referenz)");
			lblGroup.setLayoutData(minGd);
			Composite c = new Composite(compositeInputData, SWT.NONE);
			c.setLayout(new GridLayout(2, false));
			c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			Label lblGrouplist = new Label(c, SWT.NONE);
			lblGrouplist.setText(getInputDataGroupText(inputDataGroup));
			
			Button button = new Button(c, SWT.PUSH);
			button.setText("ändern..");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					FindingsSelectionDialog findingsSelectionDialog =
						new FindingsSelectionDialog(getShell(),
							model, inputDataGroup.getFindingsTemplates(), true, selection);
					if (findingsSelectionDialog.open() == MessageDialog.OK) {
						inputDataGroup.getFindingsTemplates().clear();
						
						for (FindingsTemplate findingsTemplate : findingsSelectionDialog
							.getSelection()) {
							inputDataGroup.getFindingsTemplates().add(findingsTemplate);
							try {
								FindingsServiceHolder.findingsTemplateService
									.validateCycleDetection(
									selection, 0, 100, findingsTemplate.getTitle(), true);
							} catch (ElexisException e1) {
								inputDataGroup.getFindingsTemplates().remove(findingsTemplate);
								MessageDialog.openError(getShell(), "Befunde Vorlagen",
									e1.getMessage());
							}
						}
						selection.setInputData(inputDataGroup);
						
						lblGrouplist.setText(getInputDataGroupText(inputDataGroup));
						compositeInputData.layout(true, true);
						updateSize();
					}
				}
			});
			
			selection.setInputData(inputDataGroup);
			compositeInputData.layout();
			break;
		case GROUP_COMPONENT:
			InputDataGroupComponent inputDataGroupComponent =
				selection.getInputData() instanceof InputDataGroupComponent
						? (InputDataGroupComponent) selection.getInputData()
						: ModelFactory.eINSTANCE.createInputDataGroupComponent();
			
			Label lblGroupComponent = new Label(compositeInputData, SWT.NONE);
			lblGroupComponent.setText("Gruppe (Umfasst)");
			lblGroupComponent.setLayoutData(minGd);
			Composite cGroupComponent = new Composite(compositeInputData, SWT.NONE);
			cGroupComponent.setLayout(new GridLayout(2, false));
			cGroupComponent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			Label lblGroupComponentlist = new Label(cGroupComponent, SWT.NONE);
			lblGroupComponentlist.setText(getInputDataGroupText(inputDataGroupComponent));
			
			Button buttonGroupComponent = new Button(cGroupComponent, SWT.PUSH);
			buttonGroupComponent.setText("ändern..");
			buttonGroupComponent.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					FindingsSelectionDialog findingsSelectionDialog = new FindingsSelectionDialog(
						getShell(), model, openedFromDialog ? findingTemplatesToMove
								: inputDataGroupComponent.getFindingsTemplates(),
						true,
						selection);
					if (findingsSelectionDialog.open() == MessageDialog.OK) {
						findingTemplatesToMove.clear();
						
						for (FindingsTemplate findingsTemplate : findingsSelectionDialog
							.getSelection()) {
							// for moving a findingstemplate  first we make a copy of the selected findingstemplate and then we remove the findingstemplate later
							findingTemplatesToMove.add(findingsTemplate);
						}
						if (!openedFromDialog) {
							moveCachedFindingsTemplates();
						}
						lblGroupComponentlist
							.setText(getInputDataGroupText(inputDataGroupComponent));
						selection.setInputData(inputDataGroupComponent);
						compositeInputData.layout(true, true);
						updateSize();
					}
					
				}
			});
			
			Label lblSeparator = new Label(compositeInputData, SWT.NONE);
			lblSeparator.setText("Trenntext");
			
			Text txtSeparator = new Text(compositeInputData, SWT.BORDER);
			GridData gdTxtSeparator = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gdTxtSeparator.widthHint = 60;
			txtSeparator.setLayoutData(gdTxtSeparator);
			
			DataBindingContext dataBindingContext = new DataBindingContext();
			IObservableValue<?> wTextUnit = WidgetProperties.text(SWT.Modify).observe(txtSeparator);
			IObservableValue<?> mTextUnit =
				EMFProperties
					.value(ModelPackage.Literals.INPUT_DATA_GROUP_COMPONENT__TEXT_SEPARATOR)
					.observe(inputDataGroupComponent);
			dataBindingContext.bindValue(wTextUnit, mTextUnit);
				
			selection.setInputData(inputDataGroupComponent);
			compositeInputData.layout();
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
	
	private String getInputDataGroupText(InputData inputData){
		
		List<FindingsTemplate> findingsTemplates = new ArrayList<>();
		if (inputData instanceof InputDataGroup) {
			findingsTemplates = ((InputDataGroup) inputData).getFindingsTemplates();
		}
		else if (inputData instanceof InputDataGroupComponent) {
			if (openedFromDialog) {
				findingsTemplates.addAll(findingTemplatesToMove);
			} else {
				findingsTemplates = ((InputDataGroupComponent) inputData).getFindingsTemplates();
			}
		}
		
		StringBuffer buf = new StringBuffer();
		for (FindingsTemplate findingsTemplate : findingsTemplates) {
			if (buf.length() > 0) {
				buf.append("\n");
			}
			buf.append(findingsTemplate.getTitle());
		}
		return buf.length() == 0 ? "Nicht definiert" : buf.toString();
	}
	
	public void setSelection(FindingsTemplates model, FindingsTemplate selection){
		this.model = model;
		this.selection = selection;
		
		if (selection != null) {
			item.setValue(selection);
			comboType.setSelection(new StructuredSelection(selection.getType()));
			if (selection.getInputData() instanceof InputDataNumeric) {
				comboInputData.setSelection(new StructuredSelection(DataType.NUMERIC));
				
			} else if (selection.getInputData() instanceof InputDataText) {
				comboInputData.setSelection(new StructuredSelection(DataType.TEXT));
				
			} else if (selection.getInputData() instanceof InputDataGroup) {
				comboInputData.setSelection(new StructuredSelection(
					((InputDataGroup) selection.getInputData()).getDataType()));
			}
			else if (selection.getInputData() instanceof InputDataGroupComponent) {
				comboInputData.setSelection(new StructuredSelection(
					((InputDataGroupComponent) selection.getInputData()).getDataType()));
			}
			
			selectCode(null);
			
			this.setVisible(true);
		} else {
			this.setVisible(false);
		}
		layout();
	}
	
	public void moveCachedFindingsTemplates()
	{
		if (selection.getInputData() instanceof InputDataGroupComponent) {
			
			List<FindingsTemplate> findingsToMoveToRoot = new ArrayList<>();
			
			if (!openedFromDialog) {
				// moves the findingstemplate back to root
				for (FindingsTemplate findingsTemplate : ((InputDataGroupComponent) selection
					.getInputData()).getFindingsTemplates()) {
					if (!findingTemplatesToMove.contains(findingsTemplate)) {
						findingsToMoveToRoot.add(findingsTemplate);
					}
				}
			}
			
			// move to component
			((InputDataGroupComponent) selection.getInputData()).getFindingsTemplates().clear();
			for (FindingsTemplate findingsTemplate : findingTemplatesToMove) {
				EcoreUtil.remove(findingsTemplate);
				
				((InputDataGroupComponent) selection.getInputData()).getFindingsTemplates()
					.add(findingsTemplate);
				
			}
			
			// move back to root
			for (FindingsTemplate findingsTemplate : findingsToMoveToRoot) {
				EcoreUtil.remove(findingsTemplate);
				model.getFindingsTemplates().add(findingsTemplate);
			}
		}
		findingTemplatesToMove.clear();
	}
	
	public FindingsTemplate getResult(){
		if (openedFromDialog) {
			moveCachedFindingsTemplates();
		}
		return selection;
	}
	
	class ComboInputDataTypeLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element){
			return FindingsServiceHolder.findingsTemplateService
				.getDataTypeAsText((DataType) element);
		}
	}
	
	class ComboTypeLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element){
			return FindingsServiceHolder.findingsTemplateService.getTypeAsText((Type) element);
		}
	}
	
}
