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
	 * The feature id for the '<em><b>Statistics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATISTICS__STATISTICS = 0;

	/**
	 * The feature id for the '<em><b>From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATISTICS__FROM = 1;

	/**
	 * The feature id for the '<em><b>To</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATISTICS__TO = 2;

	/**
	 * The number of structural features of the '<em>Statistics</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STATISTICS_FEATURE_COUNT = 3;

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
	 * The feature id for the '<em><b>Action</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC__ACTION = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC__VALUE = 1;

	/**
	 * The feature id for the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC__TIME = 2;

	/**
	 * The feature id for the '<em><b>Action Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC__ACTION_TYPE = 3;

	/**
	 * The number of structural features of the '<em>IStatistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISTATISTIC_FEATURE_COUNT = 4;

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
	 * The feature id for the '<em><b>Action</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_STATISTIC__ACTION = ISTATISTIC__ACTION;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_STATISTIC__VALUE = ISTATISTIC__VALUE;

	/**
	 * The feature id for the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_STATISTIC__TIME = ISTATISTIC__TIME;

	/**
	 * The feature id for the '<em><b>Action Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_STATISTIC__ACTION_TYPE = ISTATISTIC__ACTION_TYPE;

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
	 * The meta object id for the '{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl <em>Event Statistic</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ch.elexis.core.ui.usage.model.impl.EventStatisticImpl
	 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getEventStatistic()
	 * @generated
	 */
	int EVENT_STATISTIC = 3;

	/**
	 * The feature id for the '<em><b>Action</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC__ACTION = ISTATISTIC__ACTION;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC__VALUE = ISTATISTIC__VALUE;

	/**
	 * The feature id for the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC__TIME = ISTATISTIC__TIME;

	/**
	 * The feature id for the '<em><b>Action Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC__ACTION_TYPE = ISTATISTIC__ACTION_TYPE;

	/**
	 * The feature id for the '<em><b>Min Duration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC__MIN_DURATION = ISTATISTIC_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Max Duration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC__MAX_DURATION = ISTATISTIC_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Avg Duration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC__AVG_DURATION = ISTATISTIC_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Last Start</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC__LAST_START = ISTATISTIC_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Event Statistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC_FEATURE_COUNT = ISTATISTIC_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Event Statistic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_STATISTIC_OPERATION_COUNT = ISTATISTIC_OPERATION_COUNT + 0;

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
	 * Returns the meta object for the containment reference list '{@link ch.elexis.core.ui.usage.model.Statistics#getStatistics <em>Statistics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Statistics</em>'.
	 * @see ch.elexis.core.ui.usage.model.Statistics#getStatistics()
	 * @see #getStatistics()
	 * @generated
	 */
	EReference getStatistics_Statistics();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.Statistics#getFrom <em>From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>From</em>'.
	 * @see ch.elexis.core.ui.usage.model.Statistics#getFrom()
	 * @see #getStatistics()
	 * @generated
	 */
	EAttribute getStatistics_From();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.Statistics#getTo <em>To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>To</em>'.
	 * @see ch.elexis.core.ui.usage.model.Statistics#getTo()
	 * @see #getStatistics()
	 * @generated
	 */
	EAttribute getStatistics_To();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.IStatistic#getAction <em>Action</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Action</em>'.
	 * @see ch.elexis.core.ui.usage.model.IStatistic#getAction()
	 * @see #getIStatistic()
	 * @generated
	 */
	EAttribute getIStatistic_Action();

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
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.IStatistic#getTime <em>Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Time</em>'.
	 * @see ch.elexis.core.ui.usage.model.IStatistic#getTime()
	 * @see #getIStatistic()
	 * @generated
	 */
	EAttribute getIStatistic_Time();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.IStatistic#getActionType <em>Action Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Action Type</em>'.
	 * @see ch.elexis.core.ui.usage.model.IStatistic#getActionType()
	 * @see #getIStatistic()
	 * @generated
	 */
	EAttribute getIStatistic_ActionType();

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
	 * Returns the meta object for class '{@link ch.elexis.core.ui.usage.model.EventStatistic <em>Event Statistic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Event Statistic</em>'.
	 * @see ch.elexis.core.ui.usage.model.EventStatistic
	 * @generated
	 */
	EClass getEventStatistic();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.EventStatistic#getMinDuration <em>Min Duration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Min Duration</em>'.
	 * @see ch.elexis.core.ui.usage.model.EventStatistic#getMinDuration()
	 * @see #getEventStatistic()
	 * @generated
	 */
	EAttribute getEventStatistic_MinDuration();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.EventStatistic#getMaxDuration <em>Max Duration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Duration</em>'.
	 * @see ch.elexis.core.ui.usage.model.EventStatistic#getMaxDuration()
	 * @see #getEventStatistic()
	 * @generated
	 */
	EAttribute getEventStatistic_MaxDuration();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.EventStatistic#getAvgDuration <em>Avg Duration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Avg Duration</em>'.
	 * @see ch.elexis.core.ui.usage.model.EventStatistic#getAvgDuration()
	 * @see #getEventStatistic()
	 * @generated
	 */
	EAttribute getEventStatistic_AvgDuration();

	/**
	 * Returns the meta object for the attribute '{@link ch.elexis.core.ui.usage.model.EventStatistic#getLastStart <em>Last Start</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last Start</em>'.
	 * @see ch.elexis.core.ui.usage.model.EventStatistic#getLastStart()
	 * @see #getEventStatistic()
	 * @generated
	 */
	EAttribute getEventStatistic_LastStart();

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
		 * The meta object literal for the '<em><b>Statistics</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STATISTICS__STATISTICS = eINSTANCE.getStatistics_Statistics();

		/**
		 * The meta object literal for the '<em><b>From</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STATISTICS__FROM = eINSTANCE.getStatistics_From();

		/**
		 * The meta object literal for the '<em><b>To</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STATISTICS__TO = eINSTANCE.getStatistics_To();

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
		 * The meta object literal for the '<em><b>Action</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTATISTIC__ACTION = eINSTANCE.getIStatistic_Action();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTATISTIC__VALUE = eINSTANCE.getIStatistic_Value();

		/**
		 * The meta object literal for the '<em><b>Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTATISTIC__TIME = eINSTANCE.getIStatistic_Time();

		/**
		 * The meta object literal for the '<em><b>Action Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISTATISTIC__ACTION_TYPE = eINSTANCE.getIStatistic_ActionType();

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
		 * The meta object literal for the '{@link ch.elexis.core.ui.usage.model.impl.EventStatisticImpl <em>Event Statistic</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ch.elexis.core.ui.usage.model.impl.EventStatisticImpl
		 * @see ch.elexis.core.ui.usage.model.impl.ModelPackageImpl#getEventStatistic()
		 * @generated
		 */
		EClass EVENT_STATISTIC = eINSTANCE.getEventStatistic();

		/**
		 * The meta object literal for the '<em><b>Min Duration</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT_STATISTIC__MIN_DURATION = eINSTANCE.getEventStatistic_MinDuration();

		/**
		 * The meta object literal for the '<em><b>Max Duration</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT_STATISTIC__MAX_DURATION = eINSTANCE.getEventStatistic_MaxDuration();

		/**
		 * The meta object literal for the '<em><b>Avg Duration</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT_STATISTIC__AVG_DURATION = eINSTANCE.getEventStatistic_AvgDuration();

		/**
		 * The meta object literal for the '<em><b>Last Start</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT_STATISTIC__LAST_START = eINSTANCE.getEventStatistic_LastStart();

	}

} //ModelPackage
