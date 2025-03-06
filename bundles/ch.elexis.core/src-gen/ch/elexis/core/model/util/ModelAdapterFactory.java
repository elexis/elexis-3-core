/**
 * Copyright (c) 2018 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model.util;

import ch.elexis.core.model.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.model.ModelPackage
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
			public Adapter caseIdentifiable(Identifiable object) {
				return createIdentifiableAdapter();
			}
			@Override
			public Adapter caseDeleteable(Deleteable object) {
				return createDeleteableAdapter();
			}
			@Override
			public Adapter caseIXid(IXid object) {
				return createIXidAdapter();
			}
			@Override
			public Adapter caseIContact(IContact object) {
				return createIContactAdapter();
			}
			@Override
			public Adapter caseIPerson(IPerson object) {
				return createIPersonAdapter();
			}
			@Override
			public Adapter caseIOrganization(IOrganization object) {
				return createIOrganizationAdapter();
			}
			@Override
			public Adapter caseILaboratory(ILaboratory object) {
				return createILaboratoryAdapter();
			}
			@Override
			public Adapter caseIPatient(IPatient object) {
				return createIPatientAdapter();
			}
			@Override
			public Adapter caseIMandator(IMandator object) {
				return createIMandatorAdapter();
			}
			@Override
			public Adapter caseIUser(IUser object) {
				return createIUserAdapter();
			}
			@Override
			public Adapter caseIUserGroup(IUserGroup object) {
				return createIUserGroupAdapter();
			}
			@Override
			public Adapter caseILabItem(ILabItem object) {
				return createILabItemAdapter();
			}
			@Override
			public Adapter caseILabResult(ILabResult object) {
				return createILabResultAdapter();
			}
			@Override
			public Adapter caseILabOrder(ILabOrder object) {
				return createILabOrderAdapter();
			}
			@Override
			public Adapter caseILabMapping(ILabMapping object) {
				return createILabMappingAdapter();
			}
			@Override
			public Adapter caseIDocument(IDocument object) {
				return createIDocumentAdapter();
			}
			@Override
			public Adapter caseIHistory(IHistory object) {
				return createIHistoryAdapter();
			}
			@Override
			public Adapter caseICategory(ICategory object) {
				return createICategoryAdapter();
			}
			@Override
			public Adapter caseIDocumentLetter(IDocumentLetter object) {
				return createIDocumentLetterAdapter();
			}
			@Override
			public Adapter caseIDocumentTemplate(IDocumentTemplate object) {
				return createIDocumentTemplateAdapter();
			}
			@Override
			public Adapter caseISticker(ISticker object) {
				return createIStickerAdapter();
			}
			@Override
			public Adapter caseICodeElement(ICodeElement object) {
				return createICodeElementAdapter();
			}
			@Override
			public Adapter caseICodeElementBlock(ICodeElementBlock object) {
				return createICodeElementBlockAdapter();
			}
			@Override
			public Adapter caseIBillable(IBillable object) {
				return createIBillableAdapter();
			}
			@Override
			public Adapter caseIBillableVerifier(IBillableVerifier object) {
				return createIBillableVerifierAdapter();
			}
			@Override
			public <T extends IBillable> Adapter caseIBillableOptifier(IBillableOptifier<T> object) {
				return createIBillableOptifierAdapter();
			}
			@Override
			public Adapter caseIService(IService object) {
				return createIServiceAdapter();
			}
			@Override
			public Adapter caseICustomService(ICustomService object) {
				return createICustomServiceAdapter();
			}
			@Override
			public Adapter caseIArticle(IArticle object) {
				return createIArticleAdapter();
			}
			@Override
			public Adapter caseIArticleDefaultSignature(IArticleDefaultSignature object) {
				return createIArticleDefaultSignatureAdapter();
			}
			@Override
			public Adapter caseIDiagnosis(IDiagnosis object) {
				return createIDiagnosisAdapter();
			}
			@Override
			public Adapter caseIFreeTextDiagnosis(IFreeTextDiagnosis object) {
				return createIFreeTextDiagnosisAdapter();
			}
			@Override
			public Adapter caseIDiagnosisReference(IDiagnosisReference object) {
				return createIDiagnosisReferenceAdapter();
			}
			@Override
			public Adapter caseIDiagnosisTree(IDiagnosisTree object) {
				return createIDiagnosisTreeAdapter();
			}
			@Override
			public Adapter caseICustomDiagnosis(ICustomDiagnosis object) {
				return createICustomDiagnosisAdapter();
			}
			@Override
			public Adapter caseICoverage(ICoverage object) {
				return createICoverageAdapter();
			}
			@Override
			public Adapter caseIBillingSystemFactor(IBillingSystemFactor object) {
				return createIBillingSystemFactorAdapter();
			}
			@Override
			public Adapter caseIConfig(IConfig object) {
				return createIConfigAdapter();
			}
			@Override
			public Adapter caseIUserConfig(IUserConfig object) {
				return createIUserConfigAdapter();
			}
			@Override
			public Adapter caseIPeriod(IPeriod object) {
				return createIPeriodAdapter();
			}
			@Override
			public Adapter caseWithExtInfo(WithExtInfo object) {
				return createWithExtInfoAdapter();
			}
			@Override
			public Adapter caseIRole(IRole object) {
				return createIRoleAdapter();
			}
			@Override
			public Adapter caseIBlob(IBlob object) {
				return createIBlobAdapter();
			}
			@Override
			public Adapter caseIEncounter(IEncounter object) {
				return createIEncounterAdapter();
			}
			@Override
			public Adapter caseIBilled(IBilled object) {
				return createIBilledAdapter();
			}
			@Override
			public Adapter caseIInvoiceBilled(IInvoiceBilled object) {
				return createIInvoiceBilledAdapter();
			}
			@Override
			public Adapter caseIInvoice(IInvoice object) {
				return createIInvoiceAdapter();
			}
			@Override
			public Adapter caseIStock(IStock object) {
				return createIStockAdapter();
			}
			@Override
			public Adapter caseIStockEntry(IStockEntry object) {
				return createIStockEntryAdapter();
			}
			@Override
			public Adapter caseIOrderEntry(IOrderEntry object) {
				return createIOrderEntryAdapter();
			}
			@Override
			public Adapter caseIOutputLog(IOutputLog object) {
				return createIOutputLogAdapter();
			}
			@Override
			public Adapter caseIOrder(IOrder object) {
				return createIOrderAdapter();
			}
			@Override
			public Adapter caseIAddress(IAddress object) {
				return createIAddressAdapter();
			}
			@Override
			public Adapter caseIImage(IImage object) {
				return createIImageAdapter();
			}
			@Override
			public Adapter caseWithAssignableId(WithAssignableId object) {
				return createWithAssignableIdAdapter();
			}
			@Override
			public Adapter caseIPrescription(IPrescription object) {
				return createIPrescriptionAdapter();
			}
			@Override
			public Adapter caseIRight(IRight object) {
				return createIRightAdapter();
			}
			@Override
			public Adapter caseIBillingSystem(IBillingSystem object) {
				return createIBillingSystemAdapter();
			}
			@Override
			public Adapter caseIRecipe(IRecipe object) {
				return createIRecipeAdapter();
			}
			@Override
			public Adapter caseIBlobSecondary(IBlobSecondary object) {
				return createIBlobSecondaryAdapter();
			}
			@Override
			public Adapter caseIRelatedContact(IRelatedContact object) {
				return createIRelatedContactAdapter();
			}
			@Override
			public Adapter caseIPayment(IPayment object) {
				return createIPaymentAdapter();
			}
			@Override
			public Adapter caseIAccountTransaction(IAccountTransaction object) {
				return createIAccountTransactionAdapter();
			}
			@Override
			public Adapter caseIAccount(IAccount object) {
				return createIAccountAdapter();
			}
			@Override
			public Adapter caseIMessage(IMessage object) {
				return createIMessageAdapter();
			}
			@Override
			public Adapter caseITextTemplate(ITextTemplate object) {
				return createITextTemplateAdapter();
			}
			@Override
			public Adapter caseIAppointment(IAppointment object) {
				return createIAppointmentAdapter();
			}
			@Override
			public Adapter caseIAppointmentSeries(IAppointmentSeries object) {
				return createIAppointmentSeriesAdapter();
			}
			@Override
			public Adapter caseISickCertificate(ISickCertificate object) {
				return createISickCertificateAdapter();
			}
			@Override
			public Adapter caseIDayMessage(IDayMessage object) {
				return createIDayMessageAdapter();
			}
			@Override
			public Adapter caseIReminder(IReminder object) {
				return createIReminderAdapter();
			}
			@Override
			public Adapter caseIReminderResponsibleLink(IReminderResponsibleLink object) {
				return createIReminderResponsibleLinkAdapter();
			}
			@Override
			public Adapter caseIVaccination(IVaccination object) {
				return createIVaccinationAdapter();
			}
			@Override
			public <T> Adapter caseComparable(Comparable<T> object) {
				return createComparableAdapter();
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
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.Identifiable <em>Identifiable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.Identifiable
	 * @generated
	 */
	public Adapter createIdentifiableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.Deleteable <em>Deleteable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.Deleteable
	 * @generated
	 */
	public Adapter createDeleteableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IXid <em>IXid</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IXid
	 * @generated
	 */
	public Adapter createIXidAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IContact <em>IContact</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IContact
	 * @generated
	 */
	public Adapter createIContactAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IPerson <em>IPerson</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IPerson
	 * @generated
	 */
	public Adapter createIPersonAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IOrganization <em>IOrganization</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IOrganization
	 * @generated
	 */
	public Adapter createIOrganizationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ILaboratory <em>ILaboratory</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ILaboratory
	 * @generated
	 */
	public Adapter createILaboratoryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IPatient <em>IPatient</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IPatient
	 * @generated
	 */
	public Adapter createIPatientAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IMandator <em>IMandator</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IMandator
	 * @generated
	 */
	public Adapter createIMandatorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IUser <em>IUser</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IUser
	 * @generated
	 */
	public Adapter createIUserAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IUserGroup <em>IUser Group</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IUserGroup
	 * @generated
	 */
	public Adapter createIUserGroupAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ILabItem <em>ILab Item</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ILabItem
	 * @generated
	 */
	public Adapter createILabItemAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ILabResult <em>ILab Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ILabResult
	 * @generated
	 */
	public Adapter createILabResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ILabOrder <em>ILab Order</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ILabOrder
	 * @generated
	 */
	public Adapter createILabOrderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ILabMapping <em>ILab Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ILabMapping
	 * @generated
	 */
	public Adapter createILabMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IDocument <em>IDocument</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IDocument
	 * @generated
	 */
	public Adapter createIDocumentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IHistory <em>IHistory</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IHistory
	 * @generated
	 */
	public Adapter createIHistoryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ICategory <em>ICategory</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ICategory
	 * @generated
	 */
	public Adapter createICategoryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IDocumentLetter <em>IDocument Letter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IDocumentLetter
	 * @generated
	 */
	public Adapter createIDocumentLetterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IDocumentTemplate <em>IDocument Template</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IDocumentTemplate
	 * @generated
	 */
	public Adapter createIDocumentTemplateAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ISticker <em>ISticker</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ISticker
	 * @generated
	 */
	public Adapter createIStickerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ICodeElement <em>ICode Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ICodeElement
	 * @generated
	 */
	public Adapter createICodeElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ICodeElementBlock <em>ICode Element Block</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ICodeElementBlock
	 * @generated
	 */
	public Adapter createICodeElementBlockAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IBillable <em>IBillable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IBillable
	 * @generated
	 */
	public Adapter createIBillableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IBillableVerifier <em>IBillable Verifier</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IBillableVerifier
	 * @generated
	 */
	public Adapter createIBillableVerifierAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IBillableOptifier <em>IBillable Optifier</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IBillableOptifier
	 * @generated
	 */
	public Adapter createIBillableOptifierAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IService <em>IService</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IService
	 * @generated
	 */
	public Adapter createIServiceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ICustomService <em>ICustom Service</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ICustomService
	 * @generated
	 */
	public Adapter createICustomServiceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IArticle <em>IArticle</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IArticle
	 * @generated
	 */
	public Adapter createIArticleAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IArticleDefaultSignature <em>IArticle Default Signature</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IArticleDefaultSignature
	 * @generated
	 */
	public Adapter createIArticleDefaultSignatureAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IDiagnosis <em>IDiagnosis</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IDiagnosis
	 * @generated
	 */
	public Adapter createIDiagnosisAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IFreeTextDiagnosis <em>IFree Text Diagnosis</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IFreeTextDiagnosis
	 * @generated
	 */
	public Adapter createIFreeTextDiagnosisAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IDiagnosisReference <em>IDiagnosis Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IDiagnosisReference
	 * @generated
	 */
	public Adapter createIDiagnosisReferenceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IDiagnosisTree <em>IDiagnosis Tree</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IDiagnosisTree
	 * @generated
	 */
	public Adapter createIDiagnosisTreeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ICustomDiagnosis <em>ICustom Diagnosis</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ICustomDiagnosis
	 * @generated
	 */
	public Adapter createICustomDiagnosisAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ICoverage <em>ICoverage</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ICoverage
	 * @generated
	 */
	public Adapter createICoverageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IBillingSystemFactor <em>IBilling System Factor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IBillingSystemFactor
	 * @generated
	 */
	public Adapter createIBillingSystemFactorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IConfig <em>IConfig</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IConfig
	 * @generated
	 */
	public Adapter createIConfigAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IUserConfig <em>IUser Config</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IUserConfig
	 * @generated
	 */
	public Adapter createIUserConfigAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IPeriod <em>IPeriod</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IPeriod
	 * @generated
	 */
	public Adapter createIPeriodAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.WithExtInfo <em>With Ext Info</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.WithExtInfo
	 * @generated
	 */
	public Adapter createWithExtInfoAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IRole <em>IRole</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IRole
	 * @generated
	 */
	public Adapter createIRoleAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IBlob <em>IBlob</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IBlob
	 * @generated
	 */
	public Adapter createIBlobAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IEncounter <em>IEncounter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IEncounter
	 * @generated
	 */
	public Adapter createIEncounterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IBilled <em>IBilled</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IBilled
	 * @generated
	 */
	public Adapter createIBilledAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IInvoiceBilled <em>IInvoice Billed</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IInvoiceBilled
	 * @generated
	 */
	public Adapter createIInvoiceBilledAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IInvoice <em>IInvoice</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IInvoice
	 * @generated
	 */
	public Adapter createIInvoiceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IStock <em>IStock</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IStock
	 * @generated
	 */
	public Adapter createIStockAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IStockEntry <em>IStock Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IStockEntry
	 * @generated
	 */
	public Adapter createIStockEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IOrderEntry <em>IOrder Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IOrderEntry
	 * @generated
	 */
	public Adapter createIOrderEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IOutputLog <em>IOutput Log</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IOutputLog
	 * @generated
	 */
	public Adapter createIOutputLogAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IOrder <em>IOrder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IOrder
	 * @generated
	 */
	public Adapter createIOrderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IAddress <em>IAddress</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IAddress
	 * @generated
	 */
	public Adapter createIAddressAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IImage <em>IImage</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IImage
	 * @generated
	 */
	public Adapter createIImageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.WithAssignableId <em>With Assignable Id</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.WithAssignableId
	 * @generated
	 */
	public Adapter createWithAssignableIdAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IPrescription <em>IPrescription</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IPrescription
	 * @generated
	 */
	public Adapter createIPrescriptionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IRight <em>IRight</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IRight
	 * @generated
	 */
	public Adapter createIRightAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IBillingSystem <em>IBilling System</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IBillingSystem
	 * @generated
	 */
	public Adapter createIBillingSystemAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IRecipe <em>IRecipe</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IRecipe
	 * @generated
	 */
	public Adapter createIRecipeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IBlobSecondary <em>IBlob Secondary</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IBlobSecondary
	 * @generated
	 */
	public Adapter createIBlobSecondaryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IRelatedContact <em>IRelated Contact</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IRelatedContact
	 * @generated
	 */
	public Adapter createIRelatedContactAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IPayment <em>IPayment</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IPayment
	 * @generated
	 */
	public Adapter createIPaymentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IAccountTransaction <em>IAccount Transaction</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IAccountTransaction
	 * @generated
	 */
	public Adapter createIAccountTransactionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IAccount <em>IAccount</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IAccount
	 * @generated
	 */
	public Adapter createIAccountAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IMessage <em>IMessage</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IMessage
	 * @generated
	 */
	public Adapter createIMessageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ITextTemplate <em>IText Template</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ITextTemplate
	 * @generated
	 */
	public Adapter createITextTemplateAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IAppointmentSeries <em>IAppointment Series</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IAppointmentSeries
	 * @generated
	 */
	public Adapter createIAppointmentSeriesAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.ISickCertificate <em>ISick Certificate</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.ISickCertificate
	 * @generated
	 */
	public Adapter createISickCertificateAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IDayMessage <em>IDay Message</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IDayMessage
	 * @generated
	 */
	public Adapter createIDayMessageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IReminder <em>IReminder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IReminder
	 * @generated
	 */
	public Adapter createIReminderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IReminderResponsibleLink <em>IReminder Responsible Link</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IReminderResponsibleLink
	 * @generated
	 */
	public Adapter createIReminderResponsibleLinkAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IVaccination <em>IVaccination</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IVaccination
	 * @generated
	 */
	public Adapter createIVaccinationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link ch.elexis.core.model.IAppointment <em>IAppointment</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see ch.elexis.core.model.IAppointment
	 * @generated
	 */
	public Adapter createIAppointmentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.lang.Comparable <em>Comparable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.lang.Comparable
	 * @generated
	 */
	public Adapter createComparableAdapter() {
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
