/**
 */
package ch.elexis.core.findings.templates.model;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Input Data Numeric</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataNumeric#getUnit <em>Unit</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataNumeric#getDecimalPlace <em>Decimal Place</em>}</li>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataNumeric#getDataType <em>Data Type</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataNumeric()
 * @model
 * @generated
 */
public interface InputDataNumeric extends InputData {
	/**
	 * Returns the value of the '<em><b>Unit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Unit</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unit</em>' attribute.
	 * @see #setUnit(String)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataNumeric_Unit()
	 * @model
	 * @generated
	 */
	String getUnit();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.InputDataNumeric#getUnit <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit</em>' attribute.
	 * @see #getUnit()
	 * @generated
	 */
	void setUnit(String value);

	/**
	 * Returns the value of the '<em><b>Decimal Place</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Decimal Place</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Decimal Place</em>' attribute.
	 * @see #setDecimalPlace(int)
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataNumeric_DecimalPlace()
	 * @model
	 * @generated
	 */
	int getDecimalPlace();

	/**
	 * Sets the value of the '{@link ch.elexis.core.findings.templates.model.InputDataNumeric#getDecimalPlace <em>Decimal Place</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Decimal Place</em>' attribute.
	 * @see #getDecimalPlace()
	 * @generated
	 */
	void setDecimalPlace(int value);

	/**
	 * Returns the value of the '<em><b>Data Type</b></em>' attribute.
	 * The default value is <code>"NUMERIC"</code>.
	 * The literals are from the enumeration {@link ch.elexis.core.findings.templates.model.DataType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Type</em>' attribute.
	 * @see ch.elexis.core.findings.templates.model.DataType
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataNumeric_DataType()
	 * @model default="NUMERIC" changeable="false"
	 * @generated
	 */
	DataType getDataType();

} // InputDataNumeric
