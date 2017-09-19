/**
 */
package ch.elexis.core.findings.templates.model;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Input Data Text</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.findings.templates.model.InputDataText#getDataType <em>Data Type</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataText()
 * @model
 * @generated
 */
public interface InputDataText extends InputData {
	/**
	 * Returns the value of the '<em><b>Data Type</b></em>' attribute.
	 * The default value is <code>"TEXT"</code>.
	 * The literals are from the enumeration {@link ch.elexis.core.findings.templates.model.DataType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Type</em>' attribute.
	 * @see ch.elexis.core.findings.templates.model.DataType
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataText_DataType()
	 * @model default="TEXT" changeable="false"
	 * @generated
	 */
	DataType getDataType();

} // InputDataText
