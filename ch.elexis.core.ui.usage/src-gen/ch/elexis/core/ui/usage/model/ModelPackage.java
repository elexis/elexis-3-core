/**
 */
package ch.elexis.core.ui.usage.model;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
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
 * @see ch.elexis.core.ui.usage.model.ModelFactory
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
	String eNS_URI = "http://ch.elexis.core.ui.usage";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ch.elexis.core.ui.usage";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelPackage eINSTANCE = ch.elexis.core.ui.usage.model.impl.ModelPackageImpl.init();

	/**
	 * The meta object id for the '{@link ch.elexis.core.ui.usage.model.impl.StatisticsImpl <em>Statistics</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.ui.usage.model.impl.StatisticsImpl
	 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getStatistics()
	 * @generated
	 */
	int STATISTICS = 0;

	/**
	 * The feature id for the '<em><b>Statistics</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATISTICS__STATISTICS = 0;

	/**
	 * The number of structural features of the '<em>Statistics</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATISTICS_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Statistics</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATISTICS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.ui.usage.model.IStatistic <em>IStatistic</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.ui.usage.model.IStatistic
	 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getIStatistic()
	 * @generated
	 */
	int ISTATISTIC = 1;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC__TITLE = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC__VALUE = 1;

	/**
	 * The number of structural features of the '<em>IStatistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>IStatistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.ui.usage.model.impl.SimpleStatisticImpl <em>Simple Statistic</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.ui.usage.model.impl.SimpleStatisticImpl
	 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getSimpleStatistic()
	 * @generated
	 */
	int SIMPLE_STATISTIC = 2;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_STATISTIC__TITLE = ISTATISTIC__TITLE;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_STATISTIC__VALUE = ISTATISTIC__VALUE;

	/**
	 * The number of structural features of the '<em>Simple Statistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_STATISTIC_FEATURE_COUNT = ISTATISTIC_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Simple Statistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_STATISTIC_OPERATION_COUNT = ISTATISTIC_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link ch.elexis.core.ui.usage.model.impl.RelationalStatisticImpl <em>Relational Statistic</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.ui.usage.model.impl.RelationalStatisticImpl
	 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getRelationalStatistic()
	 * @generated
	 */
	int RELATIONAL_STATISTIC = 3;

	/**
	 * The feature id for the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONAL_STATISTIC__TITLE = ISTATISTIC__TITLE;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONAL_STATISTIC__VALUE = ISTATISTIC__VALUE;

	/**
	 * The feature id for the '<em><b>From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONAL_STATISTIC__FROM = ISTATISTIC_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONAL_STATISTIC__TO = ISTATISTIC_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Relational Statistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONAL_STATISTIC_FEATURE_COUNT = ISTATISTIC_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Relational Statistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONAL_STATISTIC_OPERATION_COUNT = ISTATISTIC_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link ch.elexis.core.ui.usage.model.Statistics <em>Statistics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Statistics</em>'.
	 * @see ch.elexis.core.ui.usage.model.Statistics
	 * @generated
	 */
	EClass getStatistics();

	/**
	 * Returns the meta object for the reference '{@link ch.elexis.core.ui.usage.model.Statistics#getStatistics <em>Statistics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Statistics</em>'.
	 * @see ch.elexis.core.ui.usage.model.Statistics#getStatistics()
	 * @see #getStatistics()
	 * @generated
	 */
	EReference getStatistics_Statistics();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.ui.usage.model.IStatistic <em>IStatistic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>IStatistic</em>'.
	 * @see ch.elexis.core.ui.usage.model.IStatistic
	 * @generated
	 */
	EClass getIStatistic();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.IStatistic#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Title</em>'.
	 * @see ch.elexis.core.ui.usage.model.IStatistic#getTitle()
	 * @see #getIStatistic()
	 * @generated
	 */
	EAttribute getIStatistic_Title();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.IStatistic#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see ch.elexis.core.ui.usage.model.IStatistic#getValue()
	 * @see #getIStatistic()
	 * @generated
	 */
	EAttribute getIStatistic_Value();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.ui.usage.model.SimpleStatistic <em>Simple Statistic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Simple Statistic</em>'.
	 * @see ch.elexis.core.ui.usage.model.SimpleStatistic
	 * @generated
	 */
	EClass getSimpleStatistic();

	/**
	 * Returns the meta object for class '{@link ch.elexis.core.ui.usage.model.RelationalStatistic <em>Relational Statistic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Relational Statistic</em>'.
	 * @see ch.elexis.core.ui.usage.model.RelationalStatistic
	 * @generated
	 */
	EClass getRelationalStatistic();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.RelationalStatistic#getFrom <em>From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>From</em>'.
	 * @see ch.elexis.core.ui.usage.model.RelationalStatistic#getFrom()
	 * @see #getRelationalStatistic()
	 * @generated
	 */
	EAttribute getRelationalStatistic_From();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.RelationalStatistic#getTo <em>To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>To</em>'.
	 * @see ch.elexis.core.ui.usage.model.RelationalStatistic#getTo()
	 * @see #getRelationalStatistic()
	 * @generated
	 */
	EAttribute getRelationalStatistic_To();

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
		 * The meta object literal for the '{@link ch.elexis.core.ui.usage.model.impl.StatisticsImpl <em>Statistics</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.ui.usage.model.impl.StatisticsImpl
		 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getStatistics()
		 * @generated
		 */
		EClass STATISTICS = eINSTANCE.getStatistics();

		/**
		 * The meta object literal for the '<em><b>Statistics</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STATISTICS__STATISTICS = eINSTANCE.getStatistics_Statistics();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.ui.usage.model.IStatistic <em>IStatistic</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.ui.usage.model.IStatistic
		 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getIStatistic()
		 * @generated
		 */
		EClass ISTATISTIC = eINSTANCE.getIStatistic();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTATISTIC__TITLE = eINSTANCE.getIStatistic_Title();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTATISTIC__VALUE = eINSTANCE.getIStatistic_Value();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.ui.usage.model.impl.SimpleStatisticImpl <em>Simple Statistic</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.ui.usage.model.impl.SimpleStatisticImpl
		 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getSimpleStatistic()
		 * @generated
		 */
		EClass SIMPLE_STATISTIC = eINSTANCE.getSimpleStatistic();

		/**
		 * The meta object literal for the '{@link ch.elexis.core.ui.usage.model.impl.RelationalStatisticImpl <em>Relational Statistic</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.ui.usage.model.impl.RelationalStatisticImpl
		 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getRelationalStatistic()
		 * @generated
		 */
		EClass RELATIONAL_STATISTIC = eINSTANCE.getRelationalStatistic();

		/**
		 * The meta object literal for the '<em><b>From</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RELATIONAL_STATISTIC__FROM = eINSTANCE.getRelationalStatistic_From();

		/**
		 * The meta object literal for the '<em><b>To</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RELATIONAL_STATISTIC__TO = eINSTANCE.getRelationalStatistic_To();

	}

} //ModelPackage
