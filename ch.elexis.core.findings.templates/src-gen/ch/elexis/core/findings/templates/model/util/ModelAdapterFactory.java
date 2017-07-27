/**
 */
package ch.elexis.core.findings.templates.model.util;

import ch.elexis.core.findings.templates.model.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.findings.templates.model.ModelPackage
 * @generated
 */
public class ModelAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ModelPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = ModelPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelSwitch<Adapter> modelSwitch =
		new ModelSwitch<Adapter>() {
			@Override
			public Adapter caseFindingsTemplates(FindingsTemplates object) {
				return createFindingsTemplatesAdapter();
			}
			@Override
			public Adapter caseFindingsTemplate(FindingsTemplate object) {
				return createFindingsTemplateAdapter();
			}
			@Override
			public Adapter caseInputDataNumeric(InputDataNumeric object) {
				return createInputDataNumericAdapter();
			}
			@Override
			public Adapter caseInputDataText(InputDataText object) {
				return createInputDataTextAdapter();
			}
			@Override
			public Adapter caseInputDataGroup(InputDataGroup object) {
				return createInputDataGroupAdapter();
			}
			@Override
			public Adapter caseInputData(InputData object) {
				return createInputDataAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.findings.templates.model.FindingsTemplates <em>Findings Templates</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplates
	 * @generated
	 */
	public Adapter createFindingsTemplatesAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.findings.templates.model.FindingsTemplate <em>Findings Template</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.findings.templates.model.FindingsTemplate
	 * @generated
	 */
	public Adapter createFindingsTemplateAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.findings.templates.model.InputDataNumeric <em>Input Data Numeric</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.findings.templates.model.InputDataNumeric
	 * @generated
	 */
	public Adapter createInputDataNumericAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.findings.templates.model.InputDataText <em>Input Data Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.findings.templates.model.InputDataText
	 * @generated
	 */
	public Adapter createInputDataTextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.findings.templates.model.InputDataGroup <em>Input Data Group</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.findings.templates.model.InputDataGroup
	 * @generated
	 */
	public Adapter createInputDataGroupAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.findings.templates.model.InputData <em>Input Data</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.findings.templates.model.InputData
	 * @generated
	 */
	public Adapter createInputDataAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //ModelAdapterFactory
