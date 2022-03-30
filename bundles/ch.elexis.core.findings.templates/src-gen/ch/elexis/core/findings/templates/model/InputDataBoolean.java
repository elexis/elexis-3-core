/**
 */
package ch.elexis.core.findings.templates.model;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Input
 * Data Boolean</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link ch.elexis.core.findings.templates.model.InputDataBoolean#getDataType
 * <em>Data Type</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataBoolean()
 * @model
 * @generated
 */
public interface InputDataBoolean extends InputData {
	/**
	 * Returns the value of the '<em><b>Data Type</b></em>' attribute. The default
	 * value is <code>"BOOLEAN"</code>. The literals are from the enumeration
	 * {@link ch.elexis.core.findings.templates.model.DataType}. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Data Type</em>' attribute.
	 * @see ch.elexis.core.findings.templates.model.DataType
	 * @see ch.elexis.core.findings.templates.model.ModelPackage#getInputDataBoolean_DataType()
	 * @model default="BOOLEAN" changeable="false"
	 * @generated
	 */
	DataType getDataType();

} // InputDataBoolean
