/**
 */
package ch.elexis.core.findings.templates.model;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see ch.elexis.core.findings.templates.model.ModelFactory
 * @model kind="package"
 * @generated
 */
public interface ModelPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://ch.elexis.core.findings.templates";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ch.elexis.core.findings.templates";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelPackage eINSTANCE = ch.elexis.core.findings.templates.model.impl.ModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link ch.elexis.core.findings.templates.model.impl.FindingsTemplatesImpl <em>Findings Templates</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.templates.model.impl.FindingsTemplatesImpl
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getFindingsTemplates()
	 * @generated
	 */
	int FINDINGS_TEMPLATES = 0;

	/**
	 * The feature id for the '<em><b>Findings Templates</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATES__FINDINGS_TEMPLATES = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATES__ID = 1;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATES__TITLE = 2;

	/**
	 * The number of structural features of the '<em>Findings Templates</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATES_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Findings Templates</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATES_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.findings.templates.model.impl.FindingsTemplateImpl <em>Findings Template</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.templates.model.impl.FindingsTemplateImpl
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getFindingsTemplate()
	 * @generated
	 */
	int FINDINGS_TEMPLATE = 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATE__CODE = 1;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATE__TITLE = 2;

	/**
	 * The feature id for the '<em><b>Input Data</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATE__INPUT_DATA = 3;

	/**
	 * The number of structural features of the '<em>Findings Template</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATE_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Findings Template</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINDINGS_TEMPLATE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.findings.templates.model.InputData <em>Input Data</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.templates.model.InputData
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getInputData()
	 * @generated
	 */
	int INPUT_DATA = 5;

	/**
	 * The number of structural features of the '<em>Input Data</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_FEATURE_COUNT = 0;

	/**
	 * The number of operations of the '<em>Input Data</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.findings.templates.model.impl.InputDataNumericImpl <em>Input Data Numeric</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.templates.model.impl.InputDataNumericImpl
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getInputDataNumeric()
	 * @generated
	 */
	int INPUT_DATA_NUMERIC = 2;

	/**
	 * The feature id for the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_NUMERIC__UNIT = INPUT_DATA_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Decimal Place</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_NUMERIC__DECIMAL_PLACE = INPUT_DATA_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Data Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_NUMERIC__DATA_TYPE = INPUT_DATA_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Input Data Numeric</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_NUMERIC_FEATURE_COUNT = INPUT_DATA_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Input Data Numeric</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_NUMERIC_OPERATION_COUNT = INPUT_DATA_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.findings.templates.model.impl.InputDataTextImpl <em>Input Data Text</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.templates.model.impl.InputDataTextImpl
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getInputDataText()
	 * @generated
	 */
	int INPUT_DATA_TEXT = 3;

	/**
	 * The feature id for the '<em><b>Data Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TEXT__DATA_TYPE = INPUT_DATA_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Input Data Text</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TEXT_FEATURE_COUNT = INPUT_DATA_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Input Data Text</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_TEXT_OPERATION_COUNT = INPUT_DATA_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.findings.templates.model.impl.InputDataGroupImpl <em>Input Data Group</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.templates.model.impl.InputDataGroupImpl
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getInputDataGroup()
	 * @generated
	 */
	int INPUT_DATA_GROUP = 4;

	/**
	 * The feature id for the '<em><b>Findings Templates</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_GROUP__FINDINGS_TEMPLATES = INPUT_DATA_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Data Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_GROUP__DATA_TYPE = INPUT_DATA_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Input Data Group</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_GROUP_FEATURE_COUNT = INPUT_DATA_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Input Data Group</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_DATA_GROUP_OPERATION_COUNT = INPUT_DATA_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.findings.templates.model.DataType <em>Data Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.templates.model.DataType
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getDataType()
	 * @generated
	 */
	int DATA_TYPE = 6;

	/**
	 * The meta object id for the '{@link ch.elexis.core.findings.templates.model.Type <em>Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.templates.model.Type
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getType()
	 * @generated
	 */
	int TYPE = 7;

	/**
	 * The meta object id for the '<em>Local Coding</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.findings.fhir.po.codes.LocalCoding
	 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getLocalCoding()
	 * @generated
	 */
	int LOCAL_CODING = 8;


	/**
	 * Returns the meta object for class '{@link ch.elexis.core.findings.templates.model.FindingsTemplates <em>Findings Templates</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Findings Templates</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplates
	 * @generated
	 */
	EClass getFindingsTemplates();

	/**
	 * Returns the meta object for the containment reference list '{@link ch.elexis.core.findings.templates.model.FindingsTemplates#getFindingsTemplates <em>Findings Templates</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Findings Templates</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplates#getFindingsTemplates()
	 * @see #getFindingsTemplates()
	 * @generated
	 */
	EReference getFindingsTemplates_FindingsTemplates();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.FindingsTemplates#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplates#getId()
	 * @see #getFindingsTemplates()
	 * @generated
	 */
	EAttribute getFindingsTemplates_Id();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.FindingsTemplates#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplates#getTitle()
	 * @see #getFindingsTemplates()
	 * @generated
	 */
	EAttribute getFindingsTemplates_Title();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.findings.templates.model.FindingsTemplate <em>Findings Template</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Findings Template</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplate
	 * @generated
	 */
	EClass getFindingsTemplate();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplate#getType()
	 * @see #getFindingsTemplate()
	 * @generated
	 */
	EAttribute getFindingsTemplate_Type();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getCode <em>Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Code</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplate#getCode()
	 * @see #getFindingsTemplate()
	 * @generated
	 */
	EAttribute getFindingsTemplate_Code();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplate#getTitle()
	 * @see #getFindingsTemplate()
	 * @generated
	 */
	EAttribute getFindingsTemplate_Title();

	/**
	 * Returns the meta object for the containment reference '{@link ch.elexis.core.findings.templates.model.FindingsTemplate#getInputData <em>Input Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Input Data</em>'.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplate#getInputData()
	 * @see #getFindingsTemplate()
	 * @generated
	 */
	EReference getFindingsTemplate_InputData();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.findings.templates.model.InputDataNumeric <em>Input Data Numeric</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Input Data Numeric</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataNumeric
	 * @generated
	 */
	EClass getInputDataNumeric();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.InputDataNumeric#getUnit <em>Unit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataNumeric#getUnit()
	 * @see #getInputDataNumeric()
	 * @generated
	 */
	EAttribute getInputDataNumeric_Unit();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.InputDataNumeric#getDecimalPlace <em>Decimal Place</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Decimal Place</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataNumeric#getDecimalPlace()
	 * @see #getInputDataNumeric()
	 * @generated
	 */
	EAttribute getInputDataNumeric_DecimalPlace();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.InputDataNumeric#getDataType <em>Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Type</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataNumeric#getDataType()
	 * @see #getInputDataNumeric()
	 * @generated
	 */
	EAttribute getInputDataNumeric_DataType();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.findings.templates.model.InputDataText <em>Input Data Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Input Data Text</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataText
	 * @generated
	 */
	EClass getInputDataText();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.InputDataText#getDataType <em>Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Type</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataText#getDataType()
	 * @see #getInputDataText()
	 * @generated
	 */
	EAttribute getInputDataText_DataType();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.findings.templates.model.InputDataGroup <em>Input Data Group</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Input Data Group</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataGroup
	 * @generated
	 */
	EClass getInputDataGroup();

	/**
	 * Returns the meta object for the reference list '{@link ch.elexis.core.findings.templates.model.InputDataGroup#getFindingsTemplates <em>Findings Templates</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Findings Templates</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataGroup#getFindingsTemplates()
	 * @see #getInputDataGroup()
	 * @generated
	 */
	EReference getInputDataGroup_FindingsTemplates();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.findings.templates.model.InputDataGroup#getDataType <em>Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Type</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputDataGroup#getDataType()
	 * @see #getInputDataGroup()
	 * @generated
	 */
	EAttribute getInputDataGroup_DataType();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.findings.templates.model.InputData <em>Input Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Input Data</em>'.
	 * @see ch.elexis.core.findings.templates.model.InputData
	 * @generated
	 */
	EClass getInputData();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.findings.templates.model.DataType <em>Data Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Data Type</em>'.
	 * @see ch.elexis.core.findings.templates.model.DataType
	 * @generated
	 */
	EEnum getDataType();

	/**
	 * Returns the meta object for enum '{@link ch.elexis.core.findings.templates.model.Type <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Type</em>'.
	 * @see ch.elexis.core.findings.templates.model.Type
	 * @generated
	 */
	EEnum getType();

	/**
	 * Returns the meta object for data type '{@link ch.elexis.core.findings.fhir.po.codes.LocalCoding <em>Local Coding</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Local Coding</em>'.
	 * @see ch.elexis.core.findings.fhir.po.codes.LocalCoding
	 * @model instanceClass="ch.elexis.core.findings.fhir.po.codes.LocalCoding"
	 * @generated
	 */
	EDataType getLocalCoding();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelFactory getModelFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link ch.elexis.core.findings.templates.model.impl.FindingsTemplatesImpl <em>Findings Templates</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.templates.model.impl.FindingsTemplatesImpl
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getFindingsTemplates()
		 * @generated
		 */
		EClass FINDINGS_TEMPLATES = eINSTANCE.getFindingsTemplates();

		/**
		 * The meta object literal for the '<em><b>Findings Templates</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FINDINGS_TEMPLATES__FINDINGS_TEMPLATES = eINSTANCE.getFindingsTemplates_FindingsTemplates();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FINDINGS_TEMPLATES__ID = eINSTANCE.getFindingsTemplates_Id();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FINDINGS_TEMPLATES__TITLE = eINSTANCE.getFindingsTemplates_Title();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.findings.templates.model.impl.FindingsTemplateImpl <em>Findings Template</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.templates.model.impl.FindingsTemplateImpl
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getFindingsTemplate()
		 * @generated
		 */
		EClass FINDINGS_TEMPLATE = eINSTANCE.getFindingsTemplate();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FINDINGS_TEMPLATE__TYPE = eINSTANCE.getFindingsTemplate_Type();

		/**
		 * The meta object literal for the '<em><b>Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FINDINGS_TEMPLATE__CODE = eINSTANCE.getFindingsTemplate_Code();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FINDINGS_TEMPLATE__TITLE = eINSTANCE.getFindingsTemplate_Title();

		/**
		 * The meta object literal for the '<em><b>Input Data</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FINDINGS_TEMPLATE__INPUT_DATA = eINSTANCE.getFindingsTemplate_InputData();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.findings.templates.model.impl.InputDataNumericImpl <em>Input Data Numeric</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.templates.model.impl.InputDataNumericImpl
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getInputDataNumeric()
		 * @generated
		 */
		EClass INPUT_DATA_NUMERIC = eINSTANCE.getInputDataNumeric();

		/**
		 * The meta object literal for the '<em><b>Unit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_NUMERIC__UNIT = eINSTANCE.getInputDataNumeric_Unit();

		/**
		 * The meta object literal for the '<em><b>Decimal Place</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_NUMERIC__DECIMAL_PLACE = eINSTANCE.getInputDataNumeric_DecimalPlace();

		/**
		 * The meta object literal for the '<em><b>Data Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_NUMERIC__DATA_TYPE = eINSTANCE.getInputDataNumeric_DataType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.findings.templates.model.impl.InputDataTextImpl <em>Input Data Text</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.templates.model.impl.InputDataTextImpl
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getInputDataText()
		 * @generated
		 */
		EClass INPUT_DATA_TEXT = eINSTANCE.getInputDataText();

		/**
		 * The meta object literal for the '<em><b>Data Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_TEXT__DATA_TYPE = eINSTANCE.getInputDataText_DataType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.findings.templates.model.impl.InputDataGroupImpl <em>Input Data Group</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.templates.model.impl.InputDataGroupImpl
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getInputDataGroup()
		 * @generated
		 */
		EClass INPUT_DATA_GROUP = eINSTANCE.getInputDataGroup();

		/**
		 * The meta object literal for the '<em><b>Findings Templates</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference INPUT_DATA_GROUP__FINDINGS_TEMPLATES = eINSTANCE.getInputDataGroup_FindingsTemplates();

		/**
		 * The meta object literal for the '<em><b>Data Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT_DATA_GROUP__DATA_TYPE = eINSTANCE.getInputDataGroup_DataType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.findings.templates.model.InputData <em>Input Data</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.templates.model.InputData
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getInputData()
		 * @generated
		 */
		EClass INPUT_DATA = eINSTANCE.getInputData();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.findings.templates.model.DataType <em>Data Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.templates.model.DataType
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getDataType()
		 * @generated
		 */
		EEnum DATA_TYPE = eINSTANCE.getDataType();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.findings.templates.model.Type <em>Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.templates.model.Type
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getType()
		 * @generated
		 */
		EEnum TYPE = eINSTANCE.getType();

		/**
		 * The meta object literal for the '<em>Local Coding</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.findings.fhir.po.codes.LocalCoding
		 * @see ch.elexis.core.findings.templates.model.impl.ModelPackageImpl#getLocalCoding()
		 * @generated
		 */
		EDataType LOCAL_CODING = eINSTANCE.getLocalCoding();

	}

} //ModelPackage
