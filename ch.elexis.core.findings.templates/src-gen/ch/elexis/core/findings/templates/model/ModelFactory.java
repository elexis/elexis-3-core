/**
 */
package ch.elexis.core.findings.templates.model;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.findings.templates.model.ModelPackage
 * @generated
 */
public interface ModelFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelFactory eINSTANCE = ch.elexis.core.findings.templates.model.impl.ModelFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Findings Templates</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Findings Templates</em>'.
	 * @generated
	 */
	FindingsTemplates createFindingsTemplates();

	/**
	 * Returns a new object of class '<em>Findings Template</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Findings Template</em>'.
	 * @generated
	 */
	FindingsTemplate createFindingsTemplate();

	/**
	 * Returns a new object of class '<em>Input Data Numeric</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Input Data Numeric</em>'.
	 * @generated
	 */
	InputDataNumeric createInputDataNumeric();

	/**
	 * Returns a new object of class '<em>Input Data Text</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Input Data Text</em>'.
	 * @generated
	 */
	InputDataText createInputDataText();

	/**
	 * Returns a new object of class '<em>Input Data Group</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Input Data Group</em>'.
	 * @generated
	 */
	InputDataGroup createInputDataGroup();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ModelPackage getModelPackage();

} //ModelFactory
