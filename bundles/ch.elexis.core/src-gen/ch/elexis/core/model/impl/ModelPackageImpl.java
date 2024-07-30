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
package ch.elexis.core.model.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IAccount;
import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IAppointmentSeries;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBillableVerifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.IBlob;
import ch.elexis.core.model.IBlobSecondary;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.ICustomDiagnosis;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IDayMessage;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IFreeTextDiagnosis;
import ch.elexis.core.model.IHistory;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IInvoiceBilled;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.model.IPeriod;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IReminderResponsibleLink;
import ch.elexis.core.model.IRight;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IService;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelFactory;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.WithAssignableId;
import ch.elexis.core.model.WithExtInfo;
import ch.elexis.core.types.TypesPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelPackageImpl extends EPackageImpl implements ModelPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass identifiableEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass deleteableEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iXidEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iContactEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iPersonEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iOrganizationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iLaboratoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iPatientEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iMandatorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iUserEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iUserGroupEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iLabItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iLabResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iLabOrderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iLabMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iDocumentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iHistoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iCategoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iDocumentLetterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iDocumentTemplateEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iStickerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iCodeElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iCodeElementBlockEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iBillableEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iBillableVerifierEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iBillableOptifierEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iCustomServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iArticleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iArticleDefaultSignatureEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iDiagnosisEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iFreeTextDiagnosisEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iDiagnosisReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iDiagnosisTreeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iCustomDiagnosisEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iCoverageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iBillingSystemFactorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iUserConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iPeriodEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass withExtInfoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iRoleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iBlobEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iEncounterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iBilledEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iInvoiceBilledEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iInvoiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iStockEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iStockEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iOrderEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iOrderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iAddressEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iAppointmentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iImageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass withAssignableIdEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iPrescriptionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iRightEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iBillingSystemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iRecipeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iBlobSecondaryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iRelatedContactEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iPaymentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iAccountTransactionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iAccountEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iMessageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iTextTemplateEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iAppointmentSeriesEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iSickCertificateEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iDayMessageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iReminderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iReminderResponsibleLinkEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iVaccinationEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see ch.elexis.core.model.ModelPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ModelPackageImpl() {
		super(eNS_URI, ModelFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link ModelPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ModelPackage init() {
		if (isInited) return (ModelPackage)EPackage.Registry.INSTANCE.getEPackage(ModelPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredModelPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ModelPackageImpl theModelPackage = registeredModelPackage instanceof ModelPackageImpl ? (ModelPackageImpl)registeredModelPackage : new ModelPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		TypesPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theModelPackage.createPackageContents();

		// Initialize created meta-data
		theModelPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theModelPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ModelPackage.eNS_URI, theModelPackage);
		return theModelPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIdentifiable() {
		return identifiableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIdentifiable_Lastupdate() {
		return (EAttribute)identifiableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDeleteable() {
		return deleteableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDeleteable_Deleted() {
		return (EAttribute)deleteableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIXid() {
		return iXidEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIXid_Domain() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIXid_DomainId() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIXid_Quality() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIContact() {
		return iContactEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Mandator() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_User() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Patient() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Person() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Organization() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Laboratory() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Description1() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Description2() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Description3() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Code() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Country() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Zip() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_City() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Street() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Phone1() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Phone2() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Fax() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Email() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Website() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Mobile() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Comment() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIContact_Address() {
		return (EReference)iContactEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Group() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(22);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_PostalAddress() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(23);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIContact_Image() {
		return (EReference)iContactEClass.getEStructuralFeatures().get(24);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIContact_RelatedContacts() {
		return (EReference)iContactEClass.getEStructuralFeatures().get(25);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Deceased() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(26);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIContact_Email2() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(27);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIPerson() {
		return iPersonEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPerson_DateOfBirth() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPerson_Gender() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPerson_Titel() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPerson_TitelSuffix() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPerson_FirstName() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPerson_LastName() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPerson_MaritalStatus() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPerson_LegalGuardian() {
		return (EReference)iPersonEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPerson_DateOfDeath() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIOrganization() {
		return iOrganizationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIOrganization_InsuranceXmlName() {
		return (EAttribute)iOrganizationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIOrganization_InsuranceLawCode() {
		return (EAttribute)iOrganizationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getILaboratory() {
		return iLaboratoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIPatient() {
		return iPatientEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPatient_FamilyDoctor() {
		return (EReference)iPatientEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPatient_Diagnosen() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPatient_Risk() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPatient_FamilyAnamnese() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPatient_PersonalAnamnese() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPatient_Allergies() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPatient_Coverages() {
		return (EReference)iPatientEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIMandator() {
		return iMandatorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIMandator_Biller() {
		return (EReference)iMandatorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMandator_Active() {
		return (EAttribute)iMandatorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIUser() {
		return iUserEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIUser_Username() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIUser_HashedPassword() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIUser_Salt() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIUser_AssignedContact() {
		return (EReference)iUserEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIUser_Roles() {
		return (EReference)iUserEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIUser_Active() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIUser_AllowExternal() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIUser_Administrator() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIUserGroup() {
		return iUserGroupEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIUserGroup_Users() {
		return (EReference)iUserGroupEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIUserGroup_Roles() {
		return (EReference)iUserGroupEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIUserGroup_Groupname() {
		return (EAttribute)iUserGroupEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getILabItem() {
		return iLabItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Typ() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_ReferenceMale() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_ReferenceFemale() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Unit() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Group() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Priority() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Code() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Name() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Digits() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Visible() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Formula() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_LoincCode() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_BillingCode() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabItem_Export() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabItem_Mappings() {
		return (EReference)iLabItemEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getILabResult() {
		return iLabResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_Result() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_Comment() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_ReferenceMale() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_ReferenceFemale() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_Unit() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_Date() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_ObservationTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_AnalyseTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_TransmissionTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_Pathologic() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabResult_PathologicDescription() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabResult_Origin() {
		return (EReference)iLabResultEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabResult_Item() {
		return (EReference)iLabResultEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabResult_Patient() {
		return (EReference)iLabResultEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getILabOrder() {
		return iLabOrderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabOrder_Result() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabOrder_Item() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabOrder_Patient() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabOrder_TimeStamp() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabOrder_ObservationTime() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabOrder_User() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabOrder_Mandator() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabOrder_OrderId() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabOrder_State() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabOrder_GroupName() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabOrder_UserResolved() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getILabMapping() {
		return iLabMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabMapping_ItemName() {
		return (EAttribute)iLabMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabMapping_Item() {
		return (EReference)iLabMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getILabMapping_Origin() {
		return (EReference)iLabMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getILabMapping_Charge() {
		return (EAttribute)iLabMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIDocument() {
		return iDocumentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_Title() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_Description() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_Status() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_Created() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_Lastchanged() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_MimeType() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDocument_Category() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDocument_History() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_StoreId() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_Extension() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocument_Keywords() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDocument_Patient() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDocument_Author() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIHistory() {
		return iHistoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIHistory_Date() {
		return (EAttribute)iHistoryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIHistory_Status() {
		return (EAttribute)iHistoryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIHistory_Description() {
		return (EAttribute)iHistoryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getICategory() {
		return iCategoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICategory_Name() {
		return (EAttribute)iCategoryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIDocumentLetter() {
		return iDocumentLetterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDocumentLetter_Encounter() {
		return (EReference)iDocumentLetterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDocumentLetter_Recipient() {
		return (EReference)iDocumentLetterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIDocumentTemplate() {
		return iDocumentTemplateEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocumentTemplate_TemplateTyp() {
		return (EAttribute)iDocumentTemplateEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDocumentTemplate_Mandator() {
		return (EReference)iDocumentTemplateEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDocumentTemplate_AskForAddressee() {
		return (EAttribute)iDocumentTemplateEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getISticker() {
		return iStickerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISticker_Background() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISticker_Foreground() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISticker_Visible() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISticker_Name() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISticker_Importance() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getISticker_Image() {
		return (EReference)iStickerEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getISticker_AttachedTo() {
		return (EReference)iStickerEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISticker_AttachedToData() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getICodeElement() {
		return iCodeElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICodeElement_Code() {
		return (EAttribute)iCodeElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICodeElement_Text() {
		return (EAttribute)iCodeElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getICodeElementBlock() {
		return iCodeElementBlockEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getICodeElementBlock_Elements() {
		return (EReference)iCodeElementBlockEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getICodeElementBlock_ElementReferences() {
		return (EReference)iCodeElementBlockEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getICodeElementBlock_Mandator() {
		return (EReference)iCodeElementBlockEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICodeElementBlock_Macro() {
		return (EAttribute)iCodeElementBlockEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIBillable() {
		return iBillableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIBillableVerifier() {
		return iBillableVerifierEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIBillableOptifier() {
		return iBillableOptifierEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIService() {
		return iServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIService_Price() {
		return (EAttribute)iServiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIService_NetPrice() {
		return (EAttribute)iServiceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIService_Minutes() {
		return (EAttribute)iServiceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getICustomService() {
		return iCustomServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIArticle() {
		return iArticleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_Gtin() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_AtcCode() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_Name() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_SellingSize() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_PackageSize() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_PackageUnit() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIArticle_Product() {
		return (EReference)iArticleEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_PurchasePrice() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_SellingPrice() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_Obligation() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_Typ() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_SubTyp() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticle_PackageSizeString() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIArticleDefaultSignature() {
		return iArticleDefaultSignatureEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_AtcCode() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_Morning() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_Noon() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_Evening() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_Night() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_Comment() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_FreeText() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_MedicationType() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_DisposalType() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_EndDate() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIArticleDefaultSignature_StartDate() {
		return (EAttribute)iArticleDefaultSignatureEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIDiagnosis() {
		return iDiagnosisEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDiagnosis_Description() {
		return (EAttribute)iDiagnosisEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIFreeTextDiagnosis() {
		return iFreeTextDiagnosisEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIDiagnosisReference() {
		return iDiagnosisReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDiagnosisReference_ReferredClass() {
		return (EAttribute)iDiagnosisReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIDiagnosisTree() {
		return iDiagnosisTreeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDiagnosisTree_Parent() {
		return (EReference)iDiagnosisTreeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIDiagnosisTree_Children() {
		return (EReference)iDiagnosisTreeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getICustomDiagnosis() {
		return iCustomDiagnosisEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getICoverage() {
		return iCoverageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getICoverage_Patient() {
		return (EReference)iCoverageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICoverage_Description() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICoverage_Reason() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICoverage_DateFrom() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getICoverage_BillingSystem() {
		return (EReference)iCoverageEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getICoverage_Guarantor() {
		return (EReference)iCoverageEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getICoverage_CostBearer() {
		return (EReference)iCoverageEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICoverage_InsuranceNumber() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICoverage_DateTo() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getICoverage_BillingProposalDate() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getICoverage_Encounters() {
		return (EReference)iCoverageEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIBillingSystemFactor() {
		return iBillingSystemFactorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBillingSystemFactor_System() {
		return (EAttribute)iBillingSystemFactorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBillingSystemFactor_Factor() {
		return (EAttribute)iBillingSystemFactorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBillingSystemFactor_ValidFrom() {
		return (EAttribute)iBillingSystemFactorEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBillingSystemFactor_ValidTo() {
		return (EAttribute)iBillingSystemFactorEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIConfig() {
		return iConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIConfig_Key() {
		return (EAttribute)iConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIConfig_Value() {
		return (EAttribute)iConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIUserConfig() {
		return iUserConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIUserConfig_Owner() {
		return (EReference)iUserConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIPeriod() {
		return iPeriodEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPeriod_StartTime() {
		return (EAttribute)iPeriodEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPeriod_EndTime() {
		return (EAttribute)iPeriodEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWithExtInfo() {
		return withExtInfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIRole() {
		return iRoleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIRole_SystemRole() {
		return (EAttribute)iRoleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIRole_AssignedRights() {
		return (EReference)iRoleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIBlob() {
		return iBlobEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBlob_Content() {
		return (EAttribute)iBlobEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBlob_Date() {
		return (EAttribute)iBlobEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIEncounter() {
		return iEncounterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIEncounter_TimeStamp() {
		return (EAttribute)iEncounterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIEncounter_Date() {
		return (EAttribute)iEncounterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIEncounter_Billable() {
		return (EAttribute)iEncounterEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIEncounter_Patient() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIEncounter_Mandator() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIEncounter_Billed() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIEncounter_Diagnoses() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIEncounter_Coverage() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIEncounter_VersionedEntry() {
		return (EAttribute)iEncounterEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIEncounter_Invoice() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIBilled() {
		return iBilledEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIBilled_Billable() {
		return (EReference)iBilledEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIBilled_Encounter() {
		return (EReference)iBilledEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_Amount() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_Price() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_ScaledPrice() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_NetPrice() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_Text() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_Points() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_Factor() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_PrimaryScale() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_SecondaryScale() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_Code() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBilled_Total() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIBilled_Biller() {
		return (EReference)iBilledEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIInvoiceBilled() {
		return iInvoiceBilledEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIInvoiceBilled_Invoice() {
		return (EReference)iInvoiceBilledEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIInvoice() {
		return iInvoiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_State() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_Number() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIInvoice_Mandator() {
		return (EReference)iInvoiceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIInvoice_Coverage() {
		return (EReference)iInvoiceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIInvoice_Encounters() {
		return (EReference)iInvoiceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIInvoice_Billed() {
		return (EReference)iInvoiceEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_Date() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_DateFrom() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_DateTo() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_TotalAmount() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_OpenAmount() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_PayedAmount() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_DemandAmount() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_Remark() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIInvoice_Payments() {
		return (EReference)iInvoiceEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIInvoice_Transactions() {
		return (EReference)iInvoiceEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIInvoice_Attachments() {
		return (EReference)iInvoiceEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIInvoice_StateDate() {
		return (EAttribute)iInvoiceEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIStock() {
		return iStockEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStock_Code() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStock_DriverUuid() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStock_DriverConfig() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStock_Priority() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIStock_Owner() {
		return (EReference)iStockEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStock_Location() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIStock_Responsible() {
		return (EReference)iStockEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIStock_StockEntries() {
		return (EReference)iStockEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIStockEntry() {
		return iStockEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStockEntry_MinimumStock() {
		return (EAttribute)iStockEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStockEntry_CurrentStock() {
		return (EAttribute)iStockEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStockEntry_MaximumStock() {
		return (EAttribute)iStockEntryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIStockEntry_FractionUnits() {
		return (EAttribute)iStockEntryEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIStockEntry_Stock() {
		return (EReference)iStockEntryEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIStockEntry_Article() {
		return (EReference)iStockEntryEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIStockEntry_Provider() {
		return (EReference)iStockEntryEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIOrderEntry() {
		return iOrderEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIOrderEntry_Order() {
		return (EReference)iOrderEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIOrderEntry_Stock() {
		return (EReference)iOrderEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIOrderEntry_Amount() {
		return (EAttribute)iOrderEntryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIOrderEntry_Article() {
		return (EReference)iOrderEntryEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIOrderEntry_Provider() {
		return (EReference)iOrderEntryEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIOrderEntry_State() {
		return (EAttribute)iOrderEntryEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIOrder() {
		return iOrderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIOrder_Entries() {
		return (EReference)iOrderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIOrder_Timestamp() {
		return (EAttribute)iOrderEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIOrder_Name() {
		return (EAttribute)iOrderEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIAddress() {
		return iAddressEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAddress_Street1() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAddress_Street2() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAddress_Zip() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAddress_City() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAddress_Country() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAddress_WrittenAddress() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAddress_Type() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIAddress_Contact() {
		return (EReference)iAddressEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIAppointment() {
		return iAppointmentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_Reason() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_State() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_Type() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_DurationMinutes() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_Schedule() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_CreatedBy() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_SubjectOrPatient() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_Priority() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_TreatmentReason() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_CaseType() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_InsuranceType() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_Linkgroup() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_Extension() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_Created() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_LastEdit() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_StateHistory() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointment_Recurring() {
		return (EAttribute)iAppointmentEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIImage() {
		return iImageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIImage_Date() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIImage_Prefix() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIImage_Title() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIImage_Image() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIImage_MimeType() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWithAssignableId() {
		return withAssignableIdEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIPrescription() {
		return iPrescriptionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPrescription_Patient() {
		return (EReference)iPrescriptionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPrescription_Article() {
		return (EReference)iPrescriptionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_DateFrom() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_DateTo() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_DosageInstruction() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_Remark() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_StopReason() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_EntryType() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_Applied() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_SortOrder() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPrescription_DisposalComment() {
		return (EAttribute)iPrescriptionEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPrescription_Prescriptor() {
		return (EReference)iPrescriptionEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPrescription_Recipe() {
		return (EReference)iPrescriptionEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPrescription_Billed() {
		return (EReference)iPrescriptionEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIRight() {
		return iRightEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIRight_Name() {
		return (EAttribute)iRightEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIRight_LocalizedName() {
		return (EAttribute)iRightEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIRight_Parent() {
		return (EReference)iRightEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIBillingSystem() {
		return iBillingSystemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBillingSystem_Name() {
		return (EAttribute)iBillingSystemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIBillingSystem_Law() {
		return (EAttribute)iBillingSystemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIRecipe() {
		return iRecipeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIRecipe_Patient() {
		return (EReference)iRecipeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIRecipe_Mandator() {
		return (EReference)iRecipeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIRecipe_Date() {
		return (EAttribute)iRecipeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIRecipe_Prescriptions() {
		return (EReference)iRecipeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIRecipe_Document() {
		return (EReference)iRecipeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIBlobSecondary() {
		return iBlobSecondaryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIRelatedContact() {
		return iRelatedContactEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIRelatedContact_MyContact() {
		return (EReference)iRelatedContactEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIRelatedContact_OtherContact() {
		return (EReference)iRelatedContactEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIRelatedContact_RelationshipDescription() {
		return (EAttribute)iRelatedContactEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIRelatedContact_MyType() {
		return (EAttribute)iRelatedContactEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIRelatedContact_OtherType() {
		return (EAttribute)iRelatedContactEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIPayment() {
		return iPaymentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIPayment_Invoice() {
		return (EReference)iPaymentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPayment_Amount() {
		return (EAttribute)iPaymentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPayment_Remark() {
		return (EAttribute)iPaymentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIPayment_Date() {
		return (EAttribute)iPaymentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIAccountTransaction() {
		return iAccountTransactionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIAccountTransaction_Invoice() {
		return (EReference)iAccountTransactionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIAccountTransaction_Payment() {
		return (EReference)iAccountTransactionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIAccountTransaction_Patient() {
		return (EReference)iAccountTransactionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAccountTransaction_Amount() {
		return (EAttribute)iAccountTransactionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAccountTransaction_Remark() {
		return (EAttribute)iAccountTransactionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIAccountTransaction_Account() {
		return (EReference)iAccountTransactionEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIAccount() {
		return iAccountEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAccount_Name() {
		return (EAttribute)iAccountEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAccount_Numeric() {
		return (EAttribute)iAccountEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAccountTransaction_Date() {
		return (EAttribute)iAccountTransactionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIMessage() {
		return iMessageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMessage_Sender() {
		return (EAttribute)iMessageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMessage_Receiver() {
		return (EAttribute)iMessageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMessage_SenderAcceptsAnswer() {
		return (EAttribute)iMessageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMessage_CreateDateTime() {
		return (EAttribute)iMessageEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMessage_MessageText() {
		return (EAttribute)iMessageEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMessage_MessageCodes() {
		return (EAttribute)iMessageEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMessage_MessagePriority() {
		return (EAttribute)iMessageEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIMessage_PreferredTransporters() {
		return (EAttribute)iMessageEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getITextTemplate() {
		return iTextTemplateEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITextTemplate_Category() {
		return (EAttribute)iTextTemplateEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getITextTemplate_Mandator() {
		return (EReference)iTextTemplateEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITextTemplate_Name() {
		return (EAttribute)iTextTemplateEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getITextTemplate_Template() {
		return (EAttribute)iTextTemplateEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIAppointmentSeries() {
		return iAppointmentSeriesEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_SeriesType() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_EndingType() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_SeriesStartDate() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_SeriesStartTime() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_SeriesEndDate() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_SeriesEndTime() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_SeriesPatternString() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_EndingPatternString() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIAppointmentSeries_Persistent() {
		return (EAttribute)iAppointmentSeriesEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIAppointmentSeries_RootAppointment() {
		return (EReference)iAppointmentSeriesEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getISickCertificate() {
		return iSickCertificateEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getISickCertificate_Patient() {
		return (EReference)iSickCertificateEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getISickCertificate_Coverage() {
		return (EReference)iSickCertificateEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getISickCertificate_Letter() {
		return (EReference)iSickCertificateEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISickCertificate_Percent() {
		return (EAttribute)iSickCertificateEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISickCertificate_Date() {
		return (EAttribute)iSickCertificateEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISickCertificate_Start() {
		return (EAttribute)iSickCertificateEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISickCertificate_End() {
		return (EAttribute)iSickCertificateEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISickCertificate_Reason() {
		return (EAttribute)iSickCertificateEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISickCertificate_Note() {
		return (EAttribute)iSickCertificateEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIDayMessage() {
		return iDayMessageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDayMessage_Title() {
		return (EAttribute)iDayMessageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDayMessage_Message() {
		return (EAttribute)iDayMessageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIDayMessage_Date() {
		return (EAttribute)iDayMessageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIReminder() {
		return iReminderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIReminder_Creator() {
		return (EReference)iReminderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIReminder_Responsible() {
		return (EReference)iReminderEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIReminder_Contact() {
		return (EReference)iReminderEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIReminder_Due() {
		return (EAttribute)iReminderEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIReminder_Status() {
		return (EAttribute)iReminderEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIReminder_Visibility() {
		return (EAttribute)iReminderEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIReminder_Subject() {
		return (EAttribute)iReminderEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIReminder_Message() {
		return (EAttribute)iReminderEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIReminder_Priority() {
		return (EAttribute)iReminderEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIReminder_Type() {
		return (EAttribute)iReminderEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIReminder_ResponsibleAll() {
		return (EAttribute)iReminderEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIReminderResponsibleLink() {
		return iReminderResponsibleLinkEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIReminderResponsibleLink_Reminder() {
		return (EReference)iReminderResponsibleLinkEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIReminderResponsibleLink_Responsible() {
		return (EReference)iReminderResponsibleLinkEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIVaccination() {
		return iVaccinationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIVaccination_Patient() {
		return (EReference)iVaccinationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIVaccination_Article() {
		return (EReference)iVaccinationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIVaccination_ArticleName() {
		return (EAttribute)iVaccinationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIVaccination_ArticleGtin() {
		return (EAttribute)iVaccinationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIVaccination_ArticleAtc() {
		return (EAttribute)iVaccinationEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIVaccination_LotNumber() {
		return (EAttribute)iVaccinationEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIVaccination_DateOfAdministration() {
		return (EAttribute)iVaccinationEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIVaccination_IngredientsAtc() {
		return (EAttribute)iVaccinationEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIVaccination_Performer() {
		return (EReference)iVaccinationEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIVaccination_Side() {
		return (EAttribute)iVaccinationEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ModelFactory getModelFactory() {
		return (ModelFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		identifiableEClass = createEClass(IDENTIFIABLE);
		createEAttribute(identifiableEClass, IDENTIFIABLE__LASTUPDATE);

		deleteableEClass = createEClass(DELETEABLE);
		createEAttribute(deleteableEClass, DELETEABLE__DELETED);

		iXidEClass = createEClass(IXID);
		createEAttribute(iXidEClass, IXID__DOMAIN);
		createEAttribute(iXidEClass, IXID__DOMAIN_ID);
		createEAttribute(iXidEClass, IXID__QUALITY);

		iContactEClass = createEClass(ICONTACT);
		createEAttribute(iContactEClass, ICONTACT__MANDATOR);
		createEAttribute(iContactEClass, ICONTACT__USER);
		createEAttribute(iContactEClass, ICONTACT__PATIENT);
		createEAttribute(iContactEClass, ICONTACT__PERSON);
		createEAttribute(iContactEClass, ICONTACT__ORGANIZATION);
		createEAttribute(iContactEClass, ICONTACT__LABORATORY);
		createEAttribute(iContactEClass, ICONTACT__DESCRIPTION1);
		createEAttribute(iContactEClass, ICONTACT__DESCRIPTION2);
		createEAttribute(iContactEClass, ICONTACT__DESCRIPTION3);
		createEAttribute(iContactEClass, ICONTACT__CODE);
		createEAttribute(iContactEClass, ICONTACT__COUNTRY);
		createEAttribute(iContactEClass, ICONTACT__ZIP);
		createEAttribute(iContactEClass, ICONTACT__CITY);
		createEAttribute(iContactEClass, ICONTACT__STREET);
		createEAttribute(iContactEClass, ICONTACT__PHONE1);
		createEAttribute(iContactEClass, ICONTACT__PHONE2);
		createEAttribute(iContactEClass, ICONTACT__FAX);
		createEAttribute(iContactEClass, ICONTACT__EMAIL);
		createEAttribute(iContactEClass, ICONTACT__WEBSITE);
		createEAttribute(iContactEClass, ICONTACT__MOBILE);
		createEAttribute(iContactEClass, ICONTACT__COMMENT);
		createEReference(iContactEClass, ICONTACT__ADDRESS);
		createEAttribute(iContactEClass, ICONTACT__GROUP);
		createEAttribute(iContactEClass, ICONTACT__POSTAL_ADDRESS);
		createEReference(iContactEClass, ICONTACT__IMAGE);
		createEReference(iContactEClass, ICONTACT__RELATED_CONTACTS);
		createEAttribute(iContactEClass, ICONTACT__DECEASED);
		createEAttribute(iContactEClass, ICONTACT__EMAIL2);

		iPersonEClass = createEClass(IPERSON);
		createEAttribute(iPersonEClass, IPERSON__DATE_OF_BIRTH);
		createEAttribute(iPersonEClass, IPERSON__GENDER);
		createEAttribute(iPersonEClass, IPERSON__TITEL);
		createEAttribute(iPersonEClass, IPERSON__TITEL_SUFFIX);
		createEAttribute(iPersonEClass, IPERSON__FIRST_NAME);
		createEAttribute(iPersonEClass, IPERSON__LAST_NAME);
		createEAttribute(iPersonEClass, IPERSON__MARITAL_STATUS);
		createEReference(iPersonEClass, IPERSON__LEGAL_GUARDIAN);
		createEAttribute(iPersonEClass, IPERSON__DATE_OF_DEATH);

		iOrganizationEClass = createEClass(IORGANIZATION);
		createEAttribute(iOrganizationEClass, IORGANIZATION__INSURANCE_XML_NAME);
		createEAttribute(iOrganizationEClass, IORGANIZATION__INSURANCE_LAW_CODE);

		iLaboratoryEClass = createEClass(ILABORATORY);

		iPatientEClass = createEClass(IPATIENT);
		createEReference(iPatientEClass, IPATIENT__FAMILY_DOCTOR);
		createEAttribute(iPatientEClass, IPATIENT__DIAGNOSEN);
		createEAttribute(iPatientEClass, IPATIENT__RISK);
		createEAttribute(iPatientEClass, IPATIENT__FAMILY_ANAMNESE);
		createEAttribute(iPatientEClass, IPATIENT__PERSONAL_ANAMNESE);
		createEAttribute(iPatientEClass, IPATIENT__ALLERGIES);
		createEReference(iPatientEClass, IPATIENT__COVERAGES);

		iMandatorEClass = createEClass(IMANDATOR);
		createEReference(iMandatorEClass, IMANDATOR__BILLER);
		createEAttribute(iMandatorEClass, IMANDATOR__ACTIVE);

		iUserEClass = createEClass(IUSER);
		createEAttribute(iUserEClass, IUSER__USERNAME);
		createEAttribute(iUserEClass, IUSER__HASHED_PASSWORD);
		createEAttribute(iUserEClass, IUSER__SALT);
		createEReference(iUserEClass, IUSER__ASSIGNED_CONTACT);
		createEReference(iUserEClass, IUSER__ROLES);
		createEAttribute(iUserEClass, IUSER__ACTIVE);
		createEAttribute(iUserEClass, IUSER__ALLOW_EXTERNAL);
		createEAttribute(iUserEClass, IUSER__ADMINISTRATOR);

		iUserGroupEClass = createEClass(IUSER_GROUP);
		createEReference(iUserGroupEClass, IUSER_GROUP__USERS);
		createEReference(iUserGroupEClass, IUSER_GROUP__ROLES);
		createEAttribute(iUserGroupEClass, IUSER_GROUP__GROUPNAME);

		iLabItemEClass = createEClass(ILAB_ITEM);
		createEAttribute(iLabItemEClass, ILAB_ITEM__TYP);
		createEAttribute(iLabItemEClass, ILAB_ITEM__REFERENCE_MALE);
		createEAttribute(iLabItemEClass, ILAB_ITEM__REFERENCE_FEMALE);
		createEAttribute(iLabItemEClass, ILAB_ITEM__UNIT);
		createEAttribute(iLabItemEClass, ILAB_ITEM__GROUP);
		createEAttribute(iLabItemEClass, ILAB_ITEM__PRIORITY);
		createEAttribute(iLabItemEClass, ILAB_ITEM__CODE);
		createEAttribute(iLabItemEClass, ILAB_ITEM__NAME);
		createEAttribute(iLabItemEClass, ILAB_ITEM__DIGITS);
		createEAttribute(iLabItemEClass, ILAB_ITEM__VISIBLE);
		createEAttribute(iLabItemEClass, ILAB_ITEM__FORMULA);
		createEAttribute(iLabItemEClass, ILAB_ITEM__LOINC_CODE);
		createEAttribute(iLabItemEClass, ILAB_ITEM__BILLING_CODE);
		createEAttribute(iLabItemEClass, ILAB_ITEM__EXPORT);
		createEReference(iLabItemEClass, ILAB_ITEM__MAPPINGS);

		iLabResultEClass = createEClass(ILAB_RESULT);
		createEAttribute(iLabResultEClass, ILAB_RESULT__RESULT);
		createEAttribute(iLabResultEClass, ILAB_RESULT__COMMENT);
		createEAttribute(iLabResultEClass, ILAB_RESULT__REFERENCE_MALE);
		createEAttribute(iLabResultEClass, ILAB_RESULT__REFERENCE_FEMALE);
		createEAttribute(iLabResultEClass, ILAB_RESULT__UNIT);
		createEAttribute(iLabResultEClass, ILAB_RESULT__DATE);
		createEAttribute(iLabResultEClass, ILAB_RESULT__OBSERVATION_TIME);
		createEAttribute(iLabResultEClass, ILAB_RESULT__ANALYSE_TIME);
		createEAttribute(iLabResultEClass, ILAB_RESULT__TRANSMISSION_TIME);
		createEAttribute(iLabResultEClass, ILAB_RESULT__PATHOLOGIC);
		createEAttribute(iLabResultEClass, ILAB_RESULT__PATHOLOGIC_DESCRIPTION);
		createEReference(iLabResultEClass, ILAB_RESULT__ORIGIN);
		createEReference(iLabResultEClass, ILAB_RESULT__ITEM);
		createEReference(iLabResultEClass, ILAB_RESULT__PATIENT);

		iLabOrderEClass = createEClass(ILAB_ORDER);
		createEReference(iLabOrderEClass, ILAB_ORDER__RESULT);
		createEReference(iLabOrderEClass, ILAB_ORDER__ITEM);
		createEReference(iLabOrderEClass, ILAB_ORDER__PATIENT);
		createEAttribute(iLabOrderEClass, ILAB_ORDER__TIME_STAMP);
		createEAttribute(iLabOrderEClass, ILAB_ORDER__OBSERVATION_TIME);
		createEReference(iLabOrderEClass, ILAB_ORDER__USER);
		createEReference(iLabOrderEClass, ILAB_ORDER__MANDATOR);
		createEAttribute(iLabOrderEClass, ILAB_ORDER__ORDER_ID);
		createEAttribute(iLabOrderEClass, ILAB_ORDER__STATE);
		createEAttribute(iLabOrderEClass, ILAB_ORDER__GROUP_NAME);
		createEAttribute(iLabOrderEClass, ILAB_ORDER__USER_RESOLVED);

		iLabMappingEClass = createEClass(ILAB_MAPPING);
		createEAttribute(iLabMappingEClass, ILAB_MAPPING__ITEM_NAME);
		createEReference(iLabMappingEClass, ILAB_MAPPING__ITEM);
		createEReference(iLabMappingEClass, ILAB_MAPPING__ORIGIN);
		createEAttribute(iLabMappingEClass, ILAB_MAPPING__CHARGE);

		iDocumentEClass = createEClass(IDOCUMENT);
		createEAttribute(iDocumentEClass, IDOCUMENT__TITLE);
		createEAttribute(iDocumentEClass, IDOCUMENT__DESCRIPTION);
		createEAttribute(iDocumentEClass, IDOCUMENT__STATUS);
		createEAttribute(iDocumentEClass, IDOCUMENT__CREATED);
		createEAttribute(iDocumentEClass, IDOCUMENT__LASTCHANGED);
		createEAttribute(iDocumentEClass, IDOCUMENT__MIME_TYPE);
		createEReference(iDocumentEClass, IDOCUMENT__CATEGORY);
		createEReference(iDocumentEClass, IDOCUMENT__HISTORY);
		createEAttribute(iDocumentEClass, IDOCUMENT__STORE_ID);
		createEAttribute(iDocumentEClass, IDOCUMENT__EXTENSION);
		createEAttribute(iDocumentEClass, IDOCUMENT__KEYWORDS);
		createEReference(iDocumentEClass, IDOCUMENT__PATIENT);
		createEReference(iDocumentEClass, IDOCUMENT__AUTHOR);

		iHistoryEClass = createEClass(IHISTORY);
		createEAttribute(iHistoryEClass, IHISTORY__DATE);
		createEAttribute(iHistoryEClass, IHISTORY__STATUS);
		createEAttribute(iHistoryEClass, IHISTORY__DESCRIPTION);

		iCategoryEClass = createEClass(ICATEGORY);
		createEAttribute(iCategoryEClass, ICATEGORY__NAME);

		iDocumentLetterEClass = createEClass(IDOCUMENT_LETTER);
		createEReference(iDocumentLetterEClass, IDOCUMENT_LETTER__ENCOUNTER);
		createEReference(iDocumentLetterEClass, IDOCUMENT_LETTER__RECIPIENT);

		iDocumentTemplateEClass = createEClass(IDOCUMENT_TEMPLATE);
		createEAttribute(iDocumentTemplateEClass, IDOCUMENT_TEMPLATE__TEMPLATE_TYP);
		createEReference(iDocumentTemplateEClass, IDOCUMENT_TEMPLATE__MANDATOR);
		createEAttribute(iDocumentTemplateEClass, IDOCUMENT_TEMPLATE__ASK_FOR_ADDRESSEE);

		iStickerEClass = createEClass(ISTICKER);
		createEAttribute(iStickerEClass, ISTICKER__BACKGROUND);
		createEAttribute(iStickerEClass, ISTICKER__FOREGROUND);
		createEAttribute(iStickerEClass, ISTICKER__VISIBLE);
		createEAttribute(iStickerEClass, ISTICKER__NAME);
		createEAttribute(iStickerEClass, ISTICKER__IMPORTANCE);
		createEReference(iStickerEClass, ISTICKER__IMAGE);
		createEReference(iStickerEClass, ISTICKER__ATTACHED_TO);
		createEAttribute(iStickerEClass, ISTICKER__ATTACHED_TO_DATA);

		iCodeElementEClass = createEClass(ICODE_ELEMENT);
		createEAttribute(iCodeElementEClass, ICODE_ELEMENT__CODE);
		createEAttribute(iCodeElementEClass, ICODE_ELEMENT__TEXT);

		iCodeElementBlockEClass = createEClass(ICODE_ELEMENT_BLOCK);
		createEReference(iCodeElementBlockEClass, ICODE_ELEMENT_BLOCK__ELEMENTS);
		createEReference(iCodeElementBlockEClass, ICODE_ELEMENT_BLOCK__ELEMENT_REFERENCES);
		createEReference(iCodeElementBlockEClass, ICODE_ELEMENT_BLOCK__MANDATOR);
		createEAttribute(iCodeElementBlockEClass, ICODE_ELEMENT_BLOCK__MACRO);

		iBillableEClass = createEClass(IBILLABLE);

		iBillableVerifierEClass = createEClass(IBILLABLE_VERIFIER);

		iBillableOptifierEClass = createEClass(IBILLABLE_OPTIFIER);

		iServiceEClass = createEClass(ISERVICE);
		createEAttribute(iServiceEClass, ISERVICE__PRICE);
		createEAttribute(iServiceEClass, ISERVICE__NET_PRICE);
		createEAttribute(iServiceEClass, ISERVICE__MINUTES);

		iCustomServiceEClass = createEClass(ICUSTOM_SERVICE);

		iArticleEClass = createEClass(IARTICLE);
		createEAttribute(iArticleEClass, IARTICLE__GTIN);
		createEAttribute(iArticleEClass, IARTICLE__ATC_CODE);
		createEAttribute(iArticleEClass, IARTICLE__NAME);
		createEAttribute(iArticleEClass, IARTICLE__SELLING_SIZE);
		createEAttribute(iArticleEClass, IARTICLE__PACKAGE_SIZE);
		createEAttribute(iArticleEClass, IARTICLE__PACKAGE_UNIT);
		createEReference(iArticleEClass, IARTICLE__PRODUCT);
		createEAttribute(iArticleEClass, IARTICLE__PURCHASE_PRICE);
		createEAttribute(iArticleEClass, IARTICLE__SELLING_PRICE);
		createEAttribute(iArticleEClass, IARTICLE__OBLIGATION);
		createEAttribute(iArticleEClass, IARTICLE__TYP);
		createEAttribute(iArticleEClass, IARTICLE__SUB_TYP);
		createEAttribute(iArticleEClass, IARTICLE__PACKAGE_SIZE_STRING);

		iArticleDefaultSignatureEClass = createEClass(IARTICLE_DEFAULT_SIGNATURE);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__ATC_CODE);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__MORNING);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__NOON);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__EVENING);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__NIGHT);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__COMMENT);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__FREE_TEXT);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__MEDICATION_TYPE);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__DISPOSAL_TYPE);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__END_DATE);
		createEAttribute(iArticleDefaultSignatureEClass, IARTICLE_DEFAULT_SIGNATURE__START_DATE);

		iDiagnosisEClass = createEClass(IDIAGNOSIS);
		createEAttribute(iDiagnosisEClass, IDIAGNOSIS__DESCRIPTION);

		iFreeTextDiagnosisEClass = createEClass(IFREE_TEXT_DIAGNOSIS);

		iDiagnosisReferenceEClass = createEClass(IDIAGNOSIS_REFERENCE);
		createEAttribute(iDiagnosisReferenceEClass, IDIAGNOSIS_REFERENCE__REFERRED_CLASS);

		iDiagnosisTreeEClass = createEClass(IDIAGNOSIS_TREE);
		createEReference(iDiagnosisTreeEClass, IDIAGNOSIS_TREE__PARENT);
		createEReference(iDiagnosisTreeEClass, IDIAGNOSIS_TREE__CHILDREN);

		iCustomDiagnosisEClass = createEClass(ICUSTOM_DIAGNOSIS);

		iCoverageEClass = createEClass(ICOVERAGE);
		createEReference(iCoverageEClass, ICOVERAGE__PATIENT);
		createEAttribute(iCoverageEClass, ICOVERAGE__DESCRIPTION);
		createEAttribute(iCoverageEClass, ICOVERAGE__REASON);
		createEAttribute(iCoverageEClass, ICOVERAGE__DATE_FROM);
		createEReference(iCoverageEClass, ICOVERAGE__COST_BEARER);
		createEAttribute(iCoverageEClass, ICOVERAGE__INSURANCE_NUMBER);
		createEAttribute(iCoverageEClass, ICOVERAGE__DATE_TO);
		createEAttribute(iCoverageEClass, ICOVERAGE__BILLING_PROPOSAL_DATE);
		createEReference(iCoverageEClass, ICOVERAGE__ENCOUNTERS);
		createEReference(iCoverageEClass, ICOVERAGE__BILLING_SYSTEM);
		createEReference(iCoverageEClass, ICOVERAGE__GUARANTOR);

		iBillingSystemFactorEClass = createEClass(IBILLING_SYSTEM_FACTOR);
		createEAttribute(iBillingSystemFactorEClass, IBILLING_SYSTEM_FACTOR__SYSTEM);
		createEAttribute(iBillingSystemFactorEClass, IBILLING_SYSTEM_FACTOR__FACTOR);
		createEAttribute(iBillingSystemFactorEClass, IBILLING_SYSTEM_FACTOR__VALID_FROM);
		createEAttribute(iBillingSystemFactorEClass, IBILLING_SYSTEM_FACTOR__VALID_TO);

		iConfigEClass = createEClass(ICONFIG);
		createEAttribute(iConfigEClass, ICONFIG__KEY);
		createEAttribute(iConfigEClass, ICONFIG__VALUE);

		iUserConfigEClass = createEClass(IUSER_CONFIG);
		createEReference(iUserConfigEClass, IUSER_CONFIG__OWNER);

		iPeriodEClass = createEClass(IPERIOD);
		createEAttribute(iPeriodEClass, IPERIOD__START_TIME);
		createEAttribute(iPeriodEClass, IPERIOD__END_TIME);

		withExtInfoEClass = createEClass(WITH_EXT_INFO);

		iRoleEClass = createEClass(IROLE);
		createEAttribute(iRoleEClass, IROLE__SYSTEM_ROLE);
		createEReference(iRoleEClass, IROLE__ASSIGNED_RIGHTS);

		iBlobEClass = createEClass(IBLOB);
		createEAttribute(iBlobEClass, IBLOB__CONTENT);
		createEAttribute(iBlobEClass, IBLOB__DATE);

		iEncounterEClass = createEClass(IENCOUNTER);
		createEAttribute(iEncounterEClass, IENCOUNTER__TIME_STAMP);
		createEAttribute(iEncounterEClass, IENCOUNTER__DATE);
		createEAttribute(iEncounterEClass, IENCOUNTER__BILLABLE);
		createEReference(iEncounterEClass, IENCOUNTER__PATIENT);
		createEReference(iEncounterEClass, IENCOUNTER__MANDATOR);
		createEReference(iEncounterEClass, IENCOUNTER__BILLED);
		createEReference(iEncounterEClass, IENCOUNTER__DIAGNOSES);
		createEReference(iEncounterEClass, IENCOUNTER__COVERAGE);
		createEAttribute(iEncounterEClass, IENCOUNTER__VERSIONED_ENTRY);
		createEReference(iEncounterEClass, IENCOUNTER__INVOICE);

		iBilledEClass = createEClass(IBILLED);
		createEReference(iBilledEClass, IBILLED__BILLABLE);
		createEReference(iBilledEClass, IBILLED__ENCOUNTER);
		createEAttribute(iBilledEClass, IBILLED__AMOUNT);
		createEAttribute(iBilledEClass, IBILLED__PRICE);
		createEAttribute(iBilledEClass, IBILLED__SCALED_PRICE);
		createEAttribute(iBilledEClass, IBILLED__NET_PRICE);
		createEAttribute(iBilledEClass, IBILLED__TEXT);
		createEAttribute(iBilledEClass, IBILLED__POINTS);
		createEAttribute(iBilledEClass, IBILLED__FACTOR);
		createEAttribute(iBilledEClass, IBILLED__PRIMARY_SCALE);
		createEAttribute(iBilledEClass, IBILLED__SECONDARY_SCALE);
		createEAttribute(iBilledEClass, IBILLED__CODE);
		createEAttribute(iBilledEClass, IBILLED__TOTAL);
		createEReference(iBilledEClass, IBILLED__BILLER);

		iInvoiceBilledEClass = createEClass(IINVOICE_BILLED);
		createEReference(iInvoiceBilledEClass, IINVOICE_BILLED__INVOICE);

		iInvoiceEClass = createEClass(IINVOICE);
		createEAttribute(iInvoiceEClass, IINVOICE__STATE);
		createEAttribute(iInvoiceEClass, IINVOICE__NUMBER);
		createEReference(iInvoiceEClass, IINVOICE__MANDATOR);
		createEReference(iInvoiceEClass, IINVOICE__COVERAGE);
		createEReference(iInvoiceEClass, IINVOICE__ENCOUNTERS);
		createEReference(iInvoiceEClass, IINVOICE__BILLED);
		createEAttribute(iInvoiceEClass, IINVOICE__DATE);
		createEAttribute(iInvoiceEClass, IINVOICE__DATE_FROM);
		createEAttribute(iInvoiceEClass, IINVOICE__DATE_TO);
		createEAttribute(iInvoiceEClass, IINVOICE__TOTAL_AMOUNT);
		createEAttribute(iInvoiceEClass, IINVOICE__OPEN_AMOUNT);
		createEAttribute(iInvoiceEClass, IINVOICE__PAYED_AMOUNT);
		createEAttribute(iInvoiceEClass, IINVOICE__DEMAND_AMOUNT);
		createEAttribute(iInvoiceEClass, IINVOICE__REMARK);
		createEAttribute(iInvoiceEClass, IINVOICE__STATE_DATE);
		createEReference(iInvoiceEClass, IINVOICE__PAYMENTS);
		createEReference(iInvoiceEClass, IINVOICE__TRANSACTIONS);
		createEReference(iInvoiceEClass, IINVOICE__ATTACHMENTS);

		iStockEClass = createEClass(ISTOCK);
		createEAttribute(iStockEClass, ISTOCK__CODE);
		createEAttribute(iStockEClass, ISTOCK__DRIVER_UUID);
		createEAttribute(iStockEClass, ISTOCK__DRIVER_CONFIG);
		createEAttribute(iStockEClass, ISTOCK__PRIORITY);
		createEReference(iStockEClass, ISTOCK__OWNER);
		createEAttribute(iStockEClass, ISTOCK__LOCATION);
		createEReference(iStockEClass, ISTOCK__RESPONSIBLE);
		createEReference(iStockEClass, ISTOCK__STOCK_ENTRIES);

		iStockEntryEClass = createEClass(ISTOCK_ENTRY);
		createEAttribute(iStockEntryEClass, ISTOCK_ENTRY__MINIMUM_STOCK);
		createEAttribute(iStockEntryEClass, ISTOCK_ENTRY__CURRENT_STOCK);
		createEAttribute(iStockEntryEClass, ISTOCK_ENTRY__MAXIMUM_STOCK);
		createEAttribute(iStockEntryEClass, ISTOCK_ENTRY__FRACTION_UNITS);
		createEReference(iStockEntryEClass, ISTOCK_ENTRY__STOCK);
		createEReference(iStockEntryEClass, ISTOCK_ENTRY__ARTICLE);
		createEReference(iStockEntryEClass, ISTOCK_ENTRY__PROVIDER);

		iOrderEntryEClass = createEClass(IORDER_ENTRY);
		createEReference(iOrderEntryEClass, IORDER_ENTRY__ORDER);
		createEReference(iOrderEntryEClass, IORDER_ENTRY__STOCK);
		createEAttribute(iOrderEntryEClass, IORDER_ENTRY__AMOUNT);
		createEReference(iOrderEntryEClass, IORDER_ENTRY__ARTICLE);
		createEReference(iOrderEntryEClass, IORDER_ENTRY__PROVIDER);
		createEAttribute(iOrderEntryEClass, IORDER_ENTRY__STATE);

		iOrderEClass = createEClass(IORDER);
		createEReference(iOrderEClass, IORDER__ENTRIES);
		createEAttribute(iOrderEClass, IORDER__TIMESTAMP);
		createEAttribute(iOrderEClass, IORDER__NAME);

		iAddressEClass = createEClass(IADDRESS);
		createEAttribute(iAddressEClass, IADDRESS__STREET1);
		createEAttribute(iAddressEClass, IADDRESS__STREET2);
		createEAttribute(iAddressEClass, IADDRESS__ZIP);
		createEAttribute(iAddressEClass, IADDRESS__CITY);
		createEAttribute(iAddressEClass, IADDRESS__COUNTRY);
		createEAttribute(iAddressEClass, IADDRESS__WRITTEN_ADDRESS);
		createEAttribute(iAddressEClass, IADDRESS__TYPE);
		createEReference(iAddressEClass, IADDRESS__CONTACT);

		iImageEClass = createEClass(IIMAGE);
		createEAttribute(iImageEClass, IIMAGE__DATE);
		createEAttribute(iImageEClass, IIMAGE__PREFIX);
		createEAttribute(iImageEClass, IIMAGE__TITLE);
		createEAttribute(iImageEClass, IIMAGE__IMAGE);
		createEAttribute(iImageEClass, IIMAGE__MIME_TYPE);

		withAssignableIdEClass = createEClass(WITH_ASSIGNABLE_ID);

		iPrescriptionEClass = createEClass(IPRESCRIPTION);
		createEReference(iPrescriptionEClass, IPRESCRIPTION__PATIENT);
		createEReference(iPrescriptionEClass, IPRESCRIPTION__ARTICLE);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__DATE_FROM);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__DATE_TO);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__STOP_REASON);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__DOSAGE_INSTRUCTION);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__DISPOSAL_COMMENT);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__REMARK);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__ENTRY_TYPE);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__APPLIED);
		createEAttribute(iPrescriptionEClass, IPRESCRIPTION__SORT_ORDER);
		createEReference(iPrescriptionEClass, IPRESCRIPTION__PRESCRIPTOR);
		createEReference(iPrescriptionEClass, IPRESCRIPTION__RECIPE);
		createEReference(iPrescriptionEClass, IPRESCRIPTION__BILLED);

		iRightEClass = createEClass(IRIGHT);
		createEAttribute(iRightEClass, IRIGHT__NAME);
		createEAttribute(iRightEClass, IRIGHT__LOCALIZED_NAME);
		createEReference(iRightEClass, IRIGHT__PARENT);

		iBillingSystemEClass = createEClass(IBILLING_SYSTEM);
		createEAttribute(iBillingSystemEClass, IBILLING_SYSTEM__NAME);
		createEAttribute(iBillingSystemEClass, IBILLING_SYSTEM__LAW);

		iRecipeEClass = createEClass(IRECIPE);
		createEReference(iRecipeEClass, IRECIPE__PATIENT);
		createEReference(iRecipeEClass, IRECIPE__MANDATOR);
		createEAttribute(iRecipeEClass, IRECIPE__DATE);
		createEReference(iRecipeEClass, IRECIPE__PRESCRIPTIONS);
		createEReference(iRecipeEClass, IRECIPE__DOCUMENT);

		iBlobSecondaryEClass = createEClass(IBLOB_SECONDARY);

		iRelatedContactEClass = createEClass(IRELATED_CONTACT);
		createEReference(iRelatedContactEClass, IRELATED_CONTACT__MY_CONTACT);
		createEReference(iRelatedContactEClass, IRELATED_CONTACT__OTHER_CONTACT);
		createEAttribute(iRelatedContactEClass, IRELATED_CONTACT__RELATIONSHIP_DESCRIPTION);
		createEAttribute(iRelatedContactEClass, IRELATED_CONTACT__MY_TYPE);
		createEAttribute(iRelatedContactEClass, IRELATED_CONTACT__OTHER_TYPE);

		iPaymentEClass = createEClass(IPAYMENT);
		createEReference(iPaymentEClass, IPAYMENT__INVOICE);
		createEAttribute(iPaymentEClass, IPAYMENT__AMOUNT);
		createEAttribute(iPaymentEClass, IPAYMENT__REMARK);
		createEAttribute(iPaymentEClass, IPAYMENT__DATE);

		iAccountTransactionEClass = createEClass(IACCOUNT_TRANSACTION);
		createEReference(iAccountTransactionEClass, IACCOUNT_TRANSACTION__INVOICE);
		createEReference(iAccountTransactionEClass, IACCOUNT_TRANSACTION__PAYMENT);
		createEReference(iAccountTransactionEClass, IACCOUNT_TRANSACTION__PATIENT);
		createEAttribute(iAccountTransactionEClass, IACCOUNT_TRANSACTION__AMOUNT);
		createEAttribute(iAccountTransactionEClass, IACCOUNT_TRANSACTION__REMARK);
		createEAttribute(iAccountTransactionEClass, IACCOUNT_TRANSACTION__DATE);
		createEReference(iAccountTransactionEClass, IACCOUNT_TRANSACTION__ACCOUNT);

		iAccountEClass = createEClass(IACCOUNT);
		createEAttribute(iAccountEClass, IACCOUNT__NAME);
		createEAttribute(iAccountEClass, IACCOUNT__NUMERIC);

		iMessageEClass = createEClass(IMESSAGE);
		createEAttribute(iMessageEClass, IMESSAGE__SENDER);
		createEAttribute(iMessageEClass, IMESSAGE__RECEIVER);
		createEAttribute(iMessageEClass, IMESSAGE__SENDER_ACCEPTS_ANSWER);
		createEAttribute(iMessageEClass, IMESSAGE__CREATE_DATE_TIME);
		createEAttribute(iMessageEClass, IMESSAGE__MESSAGE_TEXT);
		createEAttribute(iMessageEClass, IMESSAGE__MESSAGE_CODES);
		createEAttribute(iMessageEClass, IMESSAGE__MESSAGE_PRIORITY);
		createEAttribute(iMessageEClass, IMESSAGE__PREFERRED_TRANSPORTERS);

		iTextTemplateEClass = createEClass(ITEXT_TEMPLATE);
		createEAttribute(iTextTemplateEClass, ITEXT_TEMPLATE__CATEGORY);
		createEReference(iTextTemplateEClass, ITEXT_TEMPLATE__MANDATOR);
		createEAttribute(iTextTemplateEClass, ITEXT_TEMPLATE__NAME);
		createEAttribute(iTextTemplateEClass, ITEXT_TEMPLATE__TEMPLATE);

		iAppointmentEClass = createEClass(IAPPOINTMENT);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__REASON);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__STATE);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__TYPE);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__DURATION_MINUTES);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__SCHEDULE);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__SUBJECT_OR_PATIENT);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__PRIORITY);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__TREATMENT_REASON);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__CASE_TYPE);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__INSURANCE_TYPE);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__LINKGROUP);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__EXTENSION);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__CREATED);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__LAST_EDIT);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__STATE_HISTORY);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__RECURRING);
		createEAttribute(iAppointmentEClass, IAPPOINTMENT__CREATED_BY);

		iAppointmentSeriesEClass = createEClass(IAPPOINTMENT_SERIES);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__SERIES_TYPE);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__ENDING_TYPE);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__SERIES_START_DATE);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__SERIES_START_TIME);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__SERIES_END_DATE);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__SERIES_END_TIME);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__SERIES_PATTERN_STRING);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__ENDING_PATTERN_STRING);
		createEAttribute(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__PERSISTENT);
		createEReference(iAppointmentSeriesEClass, IAPPOINTMENT_SERIES__ROOT_APPOINTMENT);

		iSickCertificateEClass = createEClass(ISICK_CERTIFICATE);
		createEReference(iSickCertificateEClass, ISICK_CERTIFICATE__PATIENT);
		createEReference(iSickCertificateEClass, ISICK_CERTIFICATE__COVERAGE);
		createEReference(iSickCertificateEClass, ISICK_CERTIFICATE__LETTER);
		createEAttribute(iSickCertificateEClass, ISICK_CERTIFICATE__PERCENT);
		createEAttribute(iSickCertificateEClass, ISICK_CERTIFICATE__DATE);
		createEAttribute(iSickCertificateEClass, ISICK_CERTIFICATE__START);
		createEAttribute(iSickCertificateEClass, ISICK_CERTIFICATE__END);
		createEAttribute(iSickCertificateEClass, ISICK_CERTIFICATE__REASON);
		createEAttribute(iSickCertificateEClass, ISICK_CERTIFICATE__NOTE);

		iDayMessageEClass = createEClass(IDAY_MESSAGE);
		createEAttribute(iDayMessageEClass, IDAY_MESSAGE__TITLE);
		createEAttribute(iDayMessageEClass, IDAY_MESSAGE__MESSAGE);
		createEAttribute(iDayMessageEClass, IDAY_MESSAGE__DATE);

		iReminderEClass = createEClass(IREMINDER);
		createEReference(iReminderEClass, IREMINDER__CREATOR);
		createEReference(iReminderEClass, IREMINDER__RESPONSIBLE);
		createEReference(iReminderEClass, IREMINDER__CONTACT);
		createEAttribute(iReminderEClass, IREMINDER__DUE);
		createEAttribute(iReminderEClass, IREMINDER__STATUS);
		createEAttribute(iReminderEClass, IREMINDER__VISIBILITY);
		createEAttribute(iReminderEClass, IREMINDER__SUBJECT);
		createEAttribute(iReminderEClass, IREMINDER__MESSAGE);
		createEAttribute(iReminderEClass, IREMINDER__PRIORITY);
		createEAttribute(iReminderEClass, IREMINDER__TYPE);
		createEAttribute(iReminderEClass, IREMINDER__RESPONSIBLE_ALL);

		iReminderResponsibleLinkEClass = createEClass(IREMINDER_RESPONSIBLE_LINK);
		createEReference(iReminderResponsibleLinkEClass, IREMINDER_RESPONSIBLE_LINK__REMINDER);
		createEReference(iReminderResponsibleLinkEClass, IREMINDER_RESPONSIBLE_LINK__RESPONSIBLE);

		iVaccinationEClass = createEClass(IVACCINATION);
		createEReference(iVaccinationEClass, IVACCINATION__PATIENT);
		createEReference(iVaccinationEClass, IVACCINATION__ARTICLE);
		createEAttribute(iVaccinationEClass, IVACCINATION__ARTICLE_NAME);
		createEAttribute(iVaccinationEClass, IVACCINATION__ARTICLE_GTIN);
		createEAttribute(iVaccinationEClass, IVACCINATION__ARTICLE_ATC);
		createEAttribute(iVaccinationEClass, IVACCINATION__LOT_NUMBER);
		createEAttribute(iVaccinationEClass, IVACCINATION__DATE_OF_ADMINISTRATION);
		createEAttribute(iVaccinationEClass, IVACCINATION__INGREDIENTS_ATC);
		createEReference(iVaccinationEClass, IVACCINATION__PERFORMER);
		createEAttribute(iVaccinationEClass, IVACCINATION__SIDE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		TypesPackage theTypesPackage = (TypesPackage)EPackage.Registry.INSTANCE.getEPackage(TypesPackage.eNS_URI);

		// Create type parameters
		ETypeParameter iBillableOptifierEClass_T = addETypeParameter(iBillableOptifierEClass, "T");

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(this.getIBillable());
		iBillableOptifierEClass_T.getEBounds().add(g1);

		// Add supertypes to classes
		iXidEClass.getESuperTypes().add(this.getDeleteable());
		iXidEClass.getESuperTypes().add(this.getIdentifiable());
		iContactEClass.getESuperTypes().add(this.getIdentifiable());
		iContactEClass.getESuperTypes().add(this.getDeleteable());
		iContactEClass.getESuperTypes().add(this.getWithExtInfo());
		iPersonEClass.getESuperTypes().add(this.getIContact());
		iOrganizationEClass.getESuperTypes().add(this.getIContact());
		iLaboratoryEClass.getESuperTypes().add(this.getIOrganization());
		iPatientEClass.getESuperTypes().add(this.getIPerson());
		iMandatorEClass.getESuperTypes().add(this.getIContact());
		iUserEClass.getESuperTypes().add(this.getDeleteable());
		iUserEClass.getESuperTypes().add(this.getIdentifiable());
		iUserGroupEClass.getESuperTypes().add(this.getDeleteable());
		iUserGroupEClass.getESuperTypes().add(this.getIdentifiable());
		iUserGroupEClass.getESuperTypes().add(this.getWithExtInfo());
		iLabItemEClass.getESuperTypes().add(this.getIdentifiable());
		iLabItemEClass.getESuperTypes().add(this.getDeleteable());
		iLabResultEClass.getESuperTypes().add(this.getDeleteable());
		iLabResultEClass.getESuperTypes().add(this.getIdentifiable());
		iLabResultEClass.getESuperTypes().add(this.getWithExtInfo());
		iLabOrderEClass.getESuperTypes().add(this.getIdentifiable());
		iLabOrderEClass.getESuperTypes().add(this.getDeleteable());
		iLabMappingEClass.getESuperTypes().add(this.getDeleteable());
		iLabMappingEClass.getESuperTypes().add(this.getIdentifiable());
		iDocumentEClass.getESuperTypes().add(this.getIdentifiable());
		iDocumentEClass.getESuperTypes().add(this.getDeleteable());
		iDocumentLetterEClass.getESuperTypes().add(this.getIDocument());
		iDocumentTemplateEClass.getESuperTypes().add(this.getIDocument());
		g1 = createEGenericType(theTypesPackage.getComparable());
		EGenericType g2 = createEGenericType(this.getISticker());
		g1.getETypeArguments().add(g2);
		iStickerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getDeleteable());
		iStickerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getIdentifiable());
		iStickerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getWithAssignableId());
		iStickerEClass.getEGenericSuperTypes().add(g1);
		iCodeElementBlockEClass.getESuperTypes().add(this.getIdentifiable());
		iCodeElementBlockEClass.getESuperTypes().add(this.getDeleteable());
		iCodeElementBlockEClass.getESuperTypes().add(this.getICodeElement());
		iBillableEClass.getESuperTypes().add(this.getICodeElement());
		iBillableEClass.getESuperTypes().add(this.getIdentifiable());
		iServiceEClass.getESuperTypes().add(this.getIBillable());
		iServiceEClass.getESuperTypes().add(this.getIdentifiable());
		iServiceEClass.getESuperTypes().add(this.getDeleteable());
		iCustomServiceEClass.getESuperTypes().add(this.getIService());
		iArticleEClass.getESuperTypes().add(this.getIdentifiable());
		iArticleEClass.getESuperTypes().add(this.getIBillable());
		iArticleEClass.getESuperTypes().add(this.getDeleteable());
		iArticleEClass.getESuperTypes().add(this.getWithExtInfo());
		iArticleDefaultSignatureEClass.getESuperTypes().add(this.getDeleteable());
		iArticleDefaultSignatureEClass.getESuperTypes().add(this.getIdentifiable());
		iArticleDefaultSignatureEClass.getESuperTypes().add(this.getWithExtInfo());
		iDiagnosisEClass.getESuperTypes().add(this.getICodeElement());
		iDiagnosisEClass.getESuperTypes().add(this.getIdentifiable());
		iFreeTextDiagnosisEClass.getESuperTypes().add(this.getIDiagnosis());
		iDiagnosisReferenceEClass.getESuperTypes().add(this.getIDiagnosis());
		iDiagnosisTreeEClass.getESuperTypes().add(this.getIDiagnosis());
		iCustomDiagnosisEClass.getESuperTypes().add(this.getIDiagnosisTree());
		iCoverageEClass.getESuperTypes().add(this.getDeleteable());
		iCoverageEClass.getESuperTypes().add(this.getIdentifiable());
		iCoverageEClass.getESuperTypes().add(this.getWithExtInfo());
		iBillingSystemFactorEClass.getESuperTypes().add(this.getIdentifiable());
		iConfigEClass.getESuperTypes().add(this.getIdentifiable());
		iUserConfigEClass.getESuperTypes().add(this.getIConfig());
		iPeriodEClass.getESuperTypes().add(this.getIdentifiable());
		iPeriodEClass.getESuperTypes().add(this.getDeleteable());
		iRoleEClass.getESuperTypes().add(this.getIdentifiable());
		iRoleEClass.getESuperTypes().add(this.getWithAssignableId());
		iRoleEClass.getESuperTypes().add(this.getWithExtInfo());
		iBlobEClass.getESuperTypes().add(this.getIdentifiable());
		iBlobEClass.getESuperTypes().add(this.getDeleteable());
		iBlobEClass.getESuperTypes().add(this.getWithAssignableId());
		iEncounterEClass.getESuperTypes().add(this.getIdentifiable());
		iEncounterEClass.getESuperTypes().add(this.getDeleteable());
		iBilledEClass.getESuperTypes().add(this.getIdentifiable());
		iBilledEClass.getESuperTypes().add(this.getDeleteable());
		iBilledEClass.getESuperTypes().add(this.getWithExtInfo());
		iInvoiceBilledEClass.getESuperTypes().add(this.getIBilled());
		iInvoiceEClass.getESuperTypes().add(this.getIdentifiable());
		iInvoiceEClass.getESuperTypes().add(this.getDeleteable());
		iInvoiceEClass.getESuperTypes().add(this.getWithExtInfo());
		iStockEClass.getESuperTypes().add(this.getIdentifiable());
		iStockEClass.getESuperTypes().add(this.getDeleteable());
		iStockEClass.getESuperTypes().add(this.getWithAssignableId());
		iStockEntryEClass.getESuperTypes().add(this.getIdentifiable());
		iStockEntryEClass.getESuperTypes().add(this.getDeleteable());
		iOrderEntryEClass.getESuperTypes().add(this.getIdentifiable());
		iOrderEntryEClass.getESuperTypes().add(this.getDeleteable());
		iOrderEClass.getESuperTypes().add(this.getIdentifiable());
		iOrderEClass.getESuperTypes().add(this.getDeleteable());
		iAddressEClass.getESuperTypes().add(this.getIdentifiable());
		iAddressEClass.getESuperTypes().add(this.getDeleteable());
		iImageEClass.getESuperTypes().add(this.getIdentifiable());
		iImageEClass.getESuperTypes().add(this.getDeleteable());
		iImageEClass.getESuperTypes().add(this.getWithAssignableId());
		iPrescriptionEClass.getESuperTypes().add(this.getIdentifiable());
		iPrescriptionEClass.getESuperTypes().add(this.getDeleteable());
		iPrescriptionEClass.getESuperTypes().add(this.getWithExtInfo());
		iRightEClass.getESuperTypes().add(this.getIdentifiable());
		iRightEClass.getESuperTypes().add(this.getDeleteable());
		iRightEClass.getESuperTypes().add(this.getWithAssignableId());
		iRecipeEClass.getESuperTypes().add(this.getIdentifiable());
		iRecipeEClass.getESuperTypes().add(this.getDeleteable());
		iBlobSecondaryEClass.getESuperTypes().add(this.getIBlob());
		iRelatedContactEClass.getESuperTypes().add(this.getIdentifiable());
		iRelatedContactEClass.getESuperTypes().add(this.getDeleteable());
		iPaymentEClass.getESuperTypes().add(this.getIdentifiable());
		iPaymentEClass.getESuperTypes().add(this.getDeleteable());
		iAccountTransactionEClass.getESuperTypes().add(this.getIdentifiable());
		iAccountTransactionEClass.getESuperTypes().add(this.getDeleteable());
		iMessageEClass.getESuperTypes().add(this.getIdentifiable());
		iMessageEClass.getESuperTypes().add(this.getDeleteable());
		iTextTemplateEClass.getESuperTypes().add(this.getIdentifiable());
		iTextTemplateEClass.getESuperTypes().add(this.getDeleteable());
		iTextTemplateEClass.getESuperTypes().add(this.getWithExtInfo());
		iAppointmentEClass.getESuperTypes().add(this.getIPeriod());
		iAppointmentSeriesEClass.getESuperTypes().add(this.getIAppointment());
		iSickCertificateEClass.getESuperTypes().add(this.getIdentifiable());
		iSickCertificateEClass.getESuperTypes().add(this.getDeleteable());
		iDayMessageEClass.getESuperTypes().add(this.getDeleteable());
		iDayMessageEClass.getESuperTypes().add(this.getIdentifiable());
		iDayMessageEClass.getESuperTypes().add(this.getWithAssignableId());
		iReminderEClass.getESuperTypes().add(this.getDeleteable());
		iReminderEClass.getESuperTypes().add(this.getIdentifiable());
		iReminderEClass.getESuperTypes().add(this.getWithExtInfo());
		iReminderResponsibleLinkEClass.getESuperTypes().add(this.getIdentifiable());
		iReminderResponsibleLinkEClass.getESuperTypes().add(this.getDeleteable());
		iVaccinationEClass.getESuperTypes().add(this.getIdentifiable());
		iVaccinationEClass.getESuperTypes().add(this.getDeleteable());
		iVaccinationEClass.getESuperTypes().add(this.getWithExtInfo());

		// Initialize classes and features; add operations and parameters
		initEClass(identifiableEClass, Identifiable.class, "Identifiable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIdentifiable_Lastupdate(), ecorePackage.getELongObject(), "lastupdate", null, 1, 1, Identifiable.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(identifiableEClass, ecorePackage.getEString(), "getId", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(identifiableEClass, ecorePackage.getEString(), "getLabel", 0, 1, IS_UNIQUE, IS_ORDERED);

		EOperation op = addEOperation(identifiableEClass, ecorePackage.getEBoolean(), "addXid", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "domain", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "id", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "updateIfExists", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(identifiableEClass, this.getIXid(), "getXid", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "domain", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(identifiableEClass, null, "getChanged", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getIdentifiable());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(identifiableEClass, null, "addChanged", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIdentifiable(), "changed", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(identifiableEClass, null, "clearChanged", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(identifiableEClass, null, "getRefresh", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getIdentifiable());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(identifiableEClass, null, "addRefresh", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIdentifiable(), "changed", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(identifiableEClass, null, "clearRefresh", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(identifiableEClass, null, "addUpdated", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEStructuralFeature(), "feature", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(identifiableEClass, null, "getUpdated", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(ecorePackage.getEStructuralFeature());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		addEOperation(identifiableEClass, null, "clearUpdated", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(deleteableEClass, Deleteable.class, "Deleteable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDeleteable_Deleted(), ecorePackage.getEBoolean(), "deleted", null, 0, 1, Deleteable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iXidEClass, IXid.class, "IXid", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIXid_Domain(), ecorePackage.getEString(), "domain", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIXid_DomainId(), ecorePackage.getEString(), "domainId", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIXid_Quality(), theTypesPackage.getXidQuality(), "quality", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iXidEClass, null, "getObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		ETypeParameter t1 = addETypeParameter(op, "T");
		g1 = createEGenericType(ecorePackage.getEJavaClass());
		g2 = createEGenericType(t1);
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "clazz", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(t1);
		initEOperation(op, g1);

		op = addEOperation(iXidEClass, null, "setObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "object", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iContactEClass, IContact.class, "IContact", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIContact_Mandator(), ecorePackage.getEBoolean(), "mandator", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_User(), ecorePackage.getEBoolean(), "user", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Patient(), ecorePackage.getEBoolean(), "patient", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Person(), ecorePackage.getEBoolean(), "person", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Organization(), ecorePackage.getEBoolean(), "organization", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Laboratory(), ecorePackage.getEBoolean(), "laboratory", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Description1(), ecorePackage.getEString(), "description1", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Description2(), ecorePackage.getEString(), "description2", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Description3(), ecorePackage.getEString(), "description3", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Code(), ecorePackage.getEString(), "code", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Country(), theTypesPackage.getCountry(), "country", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Zip(), ecorePackage.getEString(), "zip", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_City(), ecorePackage.getEString(), "city", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Street(), ecorePackage.getEString(), "street", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Phone1(), ecorePackage.getEString(), "phone1", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Phone2(), ecorePackage.getEString(), "phone2", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Fax(), ecorePackage.getEString(), "fax", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Email(), ecorePackage.getEString(), "email", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Website(), ecorePackage.getEString(), "website", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Mobile(), ecorePackage.getEString(), "mobile", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Comment(), ecorePackage.getEString(), "comment", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIContact_Address(), this.getIAddress(), null, "address", null, 0, -1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Group(), ecorePackage.getEString(), "group", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_PostalAddress(), ecorePackage.getEString(), "postalAddress", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIContact_Image(), this.getIImage(), null, "image", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIContact_RelatedContacts(), this.getIRelatedContact(), null, "relatedContacts", null, 0, -1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Deceased(), ecorePackage.getEBoolean(), "deceased", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Email2(), ecorePackage.getEString(), "email2", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iContactEClass, this.getIPerson(), "asIPerson", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iContactEClass, this.getIPatient(), "asIPatient", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iContactEClass, this.getIOrganization(), "asIOrganization", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iPersonEClass, IPerson.class, "IPerson", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIPerson_DateOfBirth(), theTypesPackage.getLocalDateTime(), "dateOfBirth", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_Gender(), theTypesPackage.getGender(), "gender", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_Titel(), ecorePackage.getEString(), "titel", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_TitelSuffix(), ecorePackage.getEString(), "titelSuffix", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_FirstName(), ecorePackage.getEString(), "firstName", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_LastName(), ecorePackage.getEString(), "lastName", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_MaritalStatus(), theTypesPackage.getMaritalStatus(), "maritalStatus", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIPerson_LegalGuardian(), this.getIContact(), null, "legalGuardian", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_DateOfDeath(), theTypesPackage.getLocalDateTime(), "dateOfDeath", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iPersonEClass, ecorePackage.getEInt(), "getAgeInYears", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPersonEClass, ecorePackage.getELong(), "getAgeAtIn", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theTypesPackage.getLocalDateTime(), "reference", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theTypesPackage.getChronoUnit(), "chronoUnit", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iOrganizationEClass, IOrganization.class, "IOrganization", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIOrganization_InsuranceXmlName(), ecorePackage.getEString(), "insuranceXmlName", null, 0, 1, IOrganization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIOrganization_InsuranceLawCode(), ecorePackage.getEString(), "insuranceLawCode", null, 0, 1, IOrganization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iLaboratoryEClass, ILaboratory.class, "ILaboratory", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(iPatientEClass, IPatient.class, "IPatient", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIPatient_FamilyDoctor(), this.getIContact(), null, "familyDoctor", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_Diagnosen(), ecorePackage.getEString(), "diagnosen", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_Risk(), ecorePackage.getEString(), "risk", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_FamilyAnamnese(), ecorePackage.getEString(), "familyAnamnese", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_PersonalAnamnese(), ecorePackage.getEString(), "personalAnamnese", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_Allergies(), ecorePackage.getEString(), "allergies", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIPatient_Coverages(), this.getICoverage(), null, "coverages", null, 0, -1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iPatientEClass, ecorePackage.getEString(), "getPatientNr", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPatientEClass, null, "setPatientNr", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "patientNr", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPatientEClass, null, "getMedication", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theTypesPackage.getEntryType(), "filterType", 0, -1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getIPrescription());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(iMandatorEClass, IMandator.class, "IMandator", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIMandator_Biller(), this.getIContact(), null, "biller", null, 0, 1, IMandator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIMandator_Active(), ecorePackage.getEBoolean(), "active", null, 0, 1, IMandator.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iUserEClass, IUser.class, "IUser", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIUser_Username(), ecorePackage.getEString(), "username", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_HashedPassword(), ecorePackage.getEString(), "hashedPassword", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_Salt(), ecorePackage.getEString(), "salt", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIUser_AssignedContact(), this.getIContact(), null, "assignedContact", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIUser_Roles(), this.getIRole(), null, "roles", null, 0, -1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_Active(), ecorePackage.getEBoolean(), "active", null, 1, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_AllowExternal(), ecorePackage.getEBoolean(), "allowExternal", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_Administrator(), ecorePackage.getEBoolean(), "administrator", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iUserEClass, this.getIRole(), "addRole", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIRole(), "role", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iUserEClass, null, "removeRole", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIRole(), "role", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iUserEClass, this.getIUser(), "login", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "username", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theTypesPackage.getcharArray(), "password", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iUserEClass, ecorePackage.getEBoolean(), "isInternal", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iUserEClass, null, "setRoles", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getIRole());
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "roles", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iUserGroupEClass, IUserGroup.class, "IUserGroup", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIUserGroup_Users(), this.getIUser(), null, "users", null, 0, -1, IUserGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIUserGroup_Roles(), this.getIRole(), null, "roles", null, 0, -1, IUserGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUserGroup_Groupname(), ecorePackage.getEString(), "groupname", null, 0, 1, IUserGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iUserGroupEClass, this.getIRole(), "addRole", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIRole(), "role", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iUserGroupEClass, null, "removeRole", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIRole(), "role", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iUserGroupEClass, this.getIUser(), "addUser", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIUser(), "user", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iUserGroupEClass, null, "removeUser", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIUser(), "user", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iLabItemEClass, ILabItem.class, "ILabItem", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getILabItem_Typ(), theTypesPackage.getLabItemTyp(), "typ", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_ReferenceMale(), ecorePackage.getEString(), "referenceMale", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_ReferenceFemale(), ecorePackage.getEString(), "referenceFemale", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Unit(), ecorePackage.getEString(), "unit", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Group(), ecorePackage.getEString(), "group", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Priority(), ecorePackage.getEString(), "priority", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Code(), ecorePackage.getEString(), "code", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Name(), ecorePackage.getEString(), "name", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Digits(), ecorePackage.getEInt(), "digits", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Visible(), ecorePackage.getEBoolean(), "visible", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Formula(), ecorePackage.getEString(), "formula", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_LoincCode(), ecorePackage.getEString(), "loincCode", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_BillingCode(), ecorePackage.getEString(), "billingCode", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Export(), ecorePackage.getEString(), "export", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabItem_Mappings(), this.getILabMapping(), null, "mappings", null, 0, -1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iLabItemEClass, ecorePackage.getEString(), "getVariableName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iLabResultEClass, ILabResult.class, "ILabResult", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getILabResult_Result(), ecorePackage.getEString(), "result", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Comment(), ecorePackage.getEString(), "comment", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_ReferenceMale(), ecorePackage.getEString(), "referenceMale", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_ReferenceFemale(), ecorePackage.getEString(), "referenceFemale", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Unit(), ecorePackage.getEString(), "unit", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_ObservationTime(), theTypesPackage.getLocalDateTime(), "observationTime", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_AnalyseTime(), theTypesPackage.getLocalDateTime(), "analyseTime", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_TransmissionTime(), theTypesPackage.getLocalDateTime(), "transmissionTime", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Pathologic(), ecorePackage.getEBoolean(), "pathologic", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_PathologicDescription(), theTypesPackage.getPathologicDescription(), "pathologicDescription", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabResult_Origin(), this.getIContact(), null, "origin", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabResult_Item(), this.getILabItem(), null, "item", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabResult_Patient(), this.getIPatient(), null, "patient", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iLabResultEClass, this.getILabOrder(), "getLabOrder", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iLabResultEClass, this.getILabOrder(), "getLabOrder", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "includeDeleted", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iLabOrderEClass, ILabOrder.class, "ILabOrder", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getILabOrder_Result(), this.getILabResult(), null, "result", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabOrder_Item(), this.getILabItem(), null, "item", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabOrder_Patient(), this.getIPatient(), null, "patient", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabOrder_TimeStamp(), theTypesPackage.getLocalDateTime(), "timeStamp", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabOrder_ObservationTime(), theTypesPackage.getLocalDateTime(), "observationTime", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabOrder_User(), this.getIContact(), null, "user", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabOrder_Mandator(), this.getIMandator(), null, "mandator", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabOrder_OrderId(), ecorePackage.getEString(), "orderId", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabOrder_State(), theTypesPackage.getLabOrderState(), "state", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabOrder_GroupName(), ecorePackage.getEString(), "groupName", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabOrder_UserResolved(), ecorePackage.getEBoolean(), "userResolved", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iLabMappingEClass, ILabMapping.class, "ILabMapping", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getILabMapping_ItemName(), ecorePackage.getEString(), "itemName", null, 0, 1, ILabMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabMapping_Item(), this.getILabItem(), null, "item", null, 0, 1, ILabMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabMapping_Origin(), this.getIContact(), null, "origin", null, 0, 1, ILabMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabMapping_Charge(), ecorePackage.getEBoolean(), "charge", null, 0, 1, ILabMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iDocumentEClass, IDocument.class, "IDocument", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIDocument_Title(), ecorePackage.getEString(), "title", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_Description(), ecorePackage.getEString(), "description", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_Status(), theTypesPackage.getDocumentStatus(), "status", null, 1, -1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_Created(), ecorePackage.getEDate(), "created", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_Lastchanged(), ecorePackage.getEDate(), "lastchanged", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_MimeType(), ecorePackage.getEString(), "mimeType", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIDocument_Category(), this.getICategory(), null, "category", null, 1, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIDocument_History(), this.getIHistory(), null, "history", null, 0, -1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_StoreId(), ecorePackage.getEString(), "storeId", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_Extension(), ecorePackage.getEString(), "extension", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_Keywords(), ecorePackage.getEString(), "keywords", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIDocument_Patient(), this.getIPatient(), null, "patient", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIDocument_Author(), this.getIContact(), null, "author", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iDocumentEClass, theTypesPackage.getInputStream(), "getContent", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iDocumentEClass, null, "setContent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theTypesPackage.getInputStream(), "content", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iDocumentEClass, null, "setStatus", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theTypesPackage.getDocumentStatus(), "status", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "active", 1, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iDocumentEClass, ecorePackage.getELong(), "getContentLength", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iHistoryEClass, IHistory.class, "IHistory", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIHistory_Date(), ecorePackage.getEDate(), "date", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIHistory_Status(), theTypesPackage.getDocumentStatus(), "status", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIHistory_Description(), ecorePackage.getEString(), "description", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iCategoryEClass, ICategory.class, "ICategory", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getICategory_Name(), ecorePackage.getEString(), "name", null, 0, 1, ICategory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iDocumentLetterEClass, IDocumentLetter.class, "IDocumentLetter", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIDocumentLetter_Encounter(), this.getIEncounter(), null, "encounter", null, 0, 1, IDocumentLetter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIDocumentLetter_Recipient(), this.getIContact(), null, "recipient", null, 0, 1, IDocumentLetter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iDocumentLetterEClass, ecorePackage.getEBoolean(), "isTemplate", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iDocumentTemplateEClass, IDocumentTemplate.class, "IDocumentTemplate", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIDocumentTemplate_TemplateTyp(), ecorePackage.getEString(), "templateTyp", null, 0, 1, IDocumentTemplate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIDocumentTemplate_Mandator(), this.getIMandator(), null, "mandator", null, 0, 1, IDocumentTemplate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocumentTemplate_AskForAddressee(), ecorePackage.getEBoolean(), "askForAddressee", null, 0, 1, IDocumentTemplate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iStickerEClass, ISticker.class, "ISticker", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getISticker_Background(), ecorePackage.getEString(), "background", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Foreground(), ecorePackage.getEString(), "foreground", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Visible(), ecorePackage.getEBoolean(), "visible", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Name(), ecorePackage.getEString(), "name", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Importance(), ecorePackage.getEInt(), "importance", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getISticker_Image(), this.getIImage(), null, "image", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getISticker_AttachedTo(), this.getIdentifiable(), null, "attachedTo", null, 0, 1, ISticker.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_AttachedToData(), ecorePackage.getEString(), "attachedToData", null, 0, 1, ISticker.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iStickerEClass, ecorePackage.getEString(), "getLabel", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iCodeElementEClass, ICodeElement.class, "ICodeElement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getICodeElement_Code(), ecorePackage.getEString(), "code", null, 0, 1, ICodeElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICodeElement_Text(), ecorePackage.getEString(), "text", null, 0, 1, ICodeElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getCodeSystemName", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getCodeSystemCode", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iCodeElementBlockEClass, ICodeElementBlock.class, "ICodeElementBlock", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getICodeElementBlock_Elements(), this.getICodeElement(), null, "elements", null, 0, -1, ICodeElementBlock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getICodeElementBlock_ElementReferences(), this.getICodeElement(), null, "elementReferences", null, 0, -1, ICodeElementBlock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getICodeElementBlock_Mandator(), this.getIMandator(), null, "mandator", null, 0, 1, ICodeElementBlock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICodeElementBlock_Macro(), ecorePackage.getEString(), "macro", null, 0, 1, ICodeElementBlock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iCodeElementBlockEClass, null, "getDiffToReferences", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getICodeElement());
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "elements", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getICodeElement());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iCodeElementBlockEClass, null, "addElement", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getICodeElement(), "element", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iCodeElementBlockEClass, null, "removeElement", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getICodeElement(), "element", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iCodeElementBlockEClass, null, "moveElement", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getICodeElement(), "element", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "up", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iCodeElementBlockEClass, null, "getElements", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIEncounter(), "encounter", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getICodeElement());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(iBillableEClass, IBillable.class, "IBillable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		addEOperation(iBillableEClass, theTypesPackage.getVatInfo(), "getVatInfo", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iBillableEClass, this.getIBillableOptifier(), "getOptifier", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iBillableEClass, this.getIBillableVerifier(), "getVerifier", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iBillableVerifierEClass, IBillableVerifier.class, "IBillableVerifier", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(iBillableVerifierEClass, null, "verifyAdd", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIBillable(), "billable", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIEncounter(), "encounter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEDouble(), "amount", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getResult());
		g2 = createEGenericType(this.getIBillable());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(iBillableOptifierEClass, IBillableOptifier.class, "IBillableOptifier", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(iBillableOptifierEClass, null, "add", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(iBillableOptifierEClass_T);
		addEParameter(op, g1, "billable", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIEncounter(), "encounter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEDouble(), "amount", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "save", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getResult());
		g2 = createEGenericType(this.getIBilled());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iBillableOptifierEClass, null, "putContext", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "key", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEObject(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iBillableOptifierEClass, null, "clearContext", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iBillableOptifierEClass, null, "remove", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIBilled(), "billed", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIEncounter(), "encounter", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getResult());
		g2 = createEGenericType(this.getIBilled());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iBillableOptifierEClass, null, "getFactor", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIEncounter(), "encounter", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getOptional());
		g2 = createEGenericType(this.getIBillingSystemFactor());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(iServiceEClass, IService.class, "IService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIService_Price(), theTypesPackage.getMoney(), "price", null, 0, 1, IService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIService_NetPrice(), theTypesPackage.getMoney(), "netPrice", null, 0, 1, IService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIService_Minutes(), ecorePackage.getEInt(), "minutes", null, 0, 1, IService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iCustomServiceEClass, ICustomService.class, "ICustomService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(iCustomServiceEClass, null, "setCodeSystemCode", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iArticleEClass, IArticle.class, "IArticle", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIArticle_Gtin(), ecorePackage.getEString(), "gtin", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_AtcCode(), ecorePackage.getEString(), "atcCode", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_Name(), ecorePackage.getEString(), "name", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_SellingSize(), ecorePackage.getEInt(), "sellingSize", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_PackageSize(), ecorePackage.getEInt(), "packageSize", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_PackageUnit(), ecorePackage.getEString(), "packageUnit", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIArticle_Product(), this.getIArticle(), null, "product", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_PurchasePrice(), theTypesPackage.getMoney(), "purchasePrice", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_SellingPrice(), theTypesPackage.getMoney(), "sellingPrice", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_Obligation(), ecorePackage.getEBoolean(), "obligation", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_Typ(), theTypesPackage.getArticleTyp(), "typ", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_SubTyp(), theTypesPackage.getArticleSubTyp(), "subTyp", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_PackageSizeString(), ecorePackage.getEString(), "packageSizeString", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iArticleEClass, ecorePackage.getEBoolean(), "isProduct", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iArticleEClass, null, "getPackages", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getIArticle());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iArticleEClass, null, "setCode", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "code", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iArticleEClass, ecorePackage.getEBoolean(), "isVaccination", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iArticleDefaultSignatureEClass, IArticleDefaultSignature.class, "IArticleDefaultSignature", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIArticleDefaultSignature_AtcCode(), ecorePackage.getEString(), "atcCode", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_Morning(), ecorePackage.getEString(), "morning", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_Noon(), ecorePackage.getEString(), "noon", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_Evening(), ecorePackage.getEString(), "evening", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_Night(), ecorePackage.getEString(), "night", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_Comment(), ecorePackage.getEString(), "comment", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_FreeText(), ecorePackage.getEString(), "freeText", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_MedicationType(), theTypesPackage.getEntryType(), "medicationType", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_DisposalType(), theTypesPackage.getEntryType(), "disposalType", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_EndDate(), theTypesPackage.getLocalDate(), "endDate", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticleDefaultSignature_StartDate(), theTypesPackage.getLocalDate(), "startDate", null, 0, 1, IArticleDefaultSignature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iArticleDefaultSignatureEClass, null, "setArticle", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIArticle(), "article", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iArticleDefaultSignatureEClass, ecorePackage.getEBoolean(), "isAtc", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iArticleDefaultSignatureEClass, ecorePackage.getEString(), "getSignatureAsDosisString", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iDiagnosisEClass, IDiagnosis.class, "IDiagnosis", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIDiagnosis_Description(), ecorePackage.getEString(), "description", null, 0, 1, IDiagnosis.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iFreeTextDiagnosisEClass, IFreeTextDiagnosis.class, "IFreeTextDiagnosis", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(iDiagnosisReferenceEClass, IDiagnosisReference.class, "IDiagnosisReference", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIDiagnosisReference_ReferredClass(), ecorePackage.getEString(), "referredClass", null, 0, 1, IDiagnosisReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iDiagnosisTreeEClass, IDiagnosisTree.class, "IDiagnosisTree", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIDiagnosisTree_Parent(), this.getIDiagnosisTree(), null, "parent", null, 0, 1, IDiagnosisTree.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIDiagnosisTree_Children(), this.getIDiagnosisTree(), null, "children", null, 0, -1, IDiagnosisTree.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iCustomDiagnosisEClass, ICustomDiagnosis.class, "ICustomDiagnosis", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(iCoverageEClass, ICoverage.class, "ICoverage", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getICoverage_Patient(), this.getIPatient(), null, "patient", null, 1, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_Description(), ecorePackage.getEString(), "description", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_Reason(), ecorePackage.getEString(), "reason", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_DateFrom(), theTypesPackage.getLocalDate(), "dateFrom", null, 1, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getICoverage_CostBearer(), this.getIContact(), null, "costBearer", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_InsuranceNumber(), ecorePackage.getEString(), "insuranceNumber", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_DateTo(), theTypesPackage.getLocalDate(), "dateTo", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_BillingProposalDate(), theTypesPackage.getLocalDate(), "billingProposalDate", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getICoverage_Encounters(), this.getIEncounter(), null, "encounters", null, 0, -1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getICoverage_BillingSystem(), this.getIBillingSystem(), null, "billingSystem", null, 1, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getICoverage_Guarantor(), this.getIContact(), null, "guarantor", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iCoverageEClass, ecorePackage.getEBoolean(), "isOpen", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iBillingSystemFactorEClass, IBillingSystemFactor.class, "IBillingSystemFactor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIBillingSystemFactor_System(), ecorePackage.getEString(), "system", null, 0, 1, IBillingSystemFactor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBillingSystemFactor_Factor(), ecorePackage.getEDouble(), "factor", null, 0, 1, IBillingSystemFactor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBillingSystemFactor_ValidFrom(), theTypesPackage.getLocalDate(), "validFrom", null, 0, 1, IBillingSystemFactor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBillingSystemFactor_ValidTo(), theTypesPackage.getLocalDate(), "validTo", null, 0, 1, IBillingSystemFactor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iConfigEClass, IConfig.class, "IConfig", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIConfig_Key(), ecorePackage.getEString(), "key", null, 0, 1, IConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIConfig_Value(), ecorePackage.getEString(), "value", null, 0, 1, IConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iUserConfigEClass, IUserConfig.class, "IUserConfig", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIUserConfig_Owner(), this.getIContact(), null, "owner", null, 0, 1, IUserConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iPeriodEClass, IPeriod.class, "IPeriod", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIPeriod_StartTime(), theTypesPackage.getLocalDateTime(), "startTime", null, 0, 1, IPeriod.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPeriod_EndTime(), theTypesPackage.getLocalDateTime(), "endTime", null, 0, 1, IPeriod.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iPeriodEClass, ecorePackage.getEBoolean(), "isAllDay", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(withExtInfoEClass, WithExtInfo.class, "WithExtInfo", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(withExtInfoEClass, ecorePackage.getEJavaObject(), "getExtInfo", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "key", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(withExtInfoEClass, null, "setExtInfo", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "key", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(withExtInfoEClass, null, "getMap", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getMap());
		g2 = createEGenericType(ecorePackage.getEJavaObject());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(ecorePackage.getEJavaObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(iRoleEClass, IRole.class, "IRole", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIRole_SystemRole(), ecorePackage.getEBoolean(), "systemRole", null, 0, 1, IRole.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIRole_AssignedRights(), this.getIRight(), null, "assignedRights", null, 0, -1, IRole.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iBlobEClass, IBlob.class, "IBlob", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIBlob_Content(), ecorePackage.getEByteArray(), "content", null, 0, 1, IBlob.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBlob_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IBlob.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iBlobEClass, ecorePackage.getEString(), "getStringContent", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iBlobEClass, null, "setStringContent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iBlobEClass, null, "getMapContent", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEJavaObject());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(ecorePackage.getEJavaObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iBlobEClass, null, "setMapContent", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEJavaObject());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(ecorePackage.getEJavaObject());
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "map", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iEncounterEClass, IEncounter.class, "IEncounter", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIEncounter_TimeStamp(), theTypesPackage.getLocalDateTime(), "timeStamp", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIEncounter_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIEncounter_Billable(), ecorePackage.getEBoolean(), "billable", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Patient(), this.getIPatient(), null, "patient", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Mandator(), this.getIMandator(), null, "mandator", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Billed(), this.getIBilled(), null, "billed", null, 0, -1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Diagnoses(), this.getIDiagnosisReference(), null, "diagnoses", null, 0, -1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Coverage(), this.getICoverage(), null, "coverage", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIEncounter_VersionedEntry(), theTypesPackage.getVersionedResource(), "versionedEntry", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Invoice(), this.getIInvoice(), null, "invoice", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iEncounterEClass, null, "addDiagnosis", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIDiagnosis(), "diagnosis", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iEncounterEClass, null, "removeDiagnosis", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIDiagnosis(), "diagnosis", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iEncounterEClass, null, "removeBilled", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIBilled(), "billed", 1, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iEncounterEClass, theTypesPackage.getInvoiceState(), "getInvoiceState", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iEncounterEClass, ecorePackage.getEString(), "getHeadVersionInPlaintext", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iBilledEClass, IBilled.class, "IBilled", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIBilled_Billable(), this.getIBillable(), null, "billable", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIBilled_Encounter(), this.getIEncounter(), null, "encounter", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_Amount(), ecorePackage.getEDouble(), "amount", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_Price(), theTypesPackage.getMoney(), "price", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_ScaledPrice(), theTypesPackage.getMoney(), "scaledPrice", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_NetPrice(), theTypesPackage.getMoney(), "netPrice", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_Text(), ecorePackage.getEString(), "text", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_Points(), ecorePackage.getEInt(), "points", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_Factor(), ecorePackage.getEDouble(), "factor", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_PrimaryScale(), ecorePackage.getEInt(), "primaryScale", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_SecondaryScale(), ecorePackage.getEInt(), "secondaryScale", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_Code(), ecorePackage.getEString(), "code", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_Total(), theTypesPackage.getMoney(), "total", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIBilled_Biller(), this.getIContact(), null, "biller", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iBilledEClass, ecorePackage.getEBoolean(), "isChangedPrice", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iBilledEClass, ecorePackage.getEBoolean(), "isNonIntegerAmount", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iBilledEClass, ecorePackage.getEDouble(), "getPrimaryScaleFactor", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iBilledEClass, ecorePackage.getEDouble(), "getSecondaryScaleFactor", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iBilledEClass, null, "copy", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIBilled(), "to", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iInvoiceBilledEClass, IInvoiceBilled.class, "IInvoiceBilled", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIInvoiceBilled_Invoice(), this.getIInvoice(), null, "invoice", null, 0, 1, IInvoiceBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iInvoiceEClass, IInvoice.class, "IInvoice", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIInvoice_State(), theTypesPackage.getInvoiceState(), "state", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_Number(), ecorePackage.getEString(), "number", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIInvoice_Mandator(), this.getIMandator(), null, "mandator", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIInvoice_Coverage(), this.getICoverage(), null, "coverage", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIInvoice_Encounters(), this.getIEncounter(), null, "encounters", null, 0, -1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIInvoice_Billed(), this.getIBilled(), null, "billed", null, 0, -1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_DateFrom(), theTypesPackage.getLocalDate(), "dateFrom", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_DateTo(), theTypesPackage.getLocalDate(), "dateTo", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_TotalAmount(), theTypesPackage.getMoney(), "totalAmount", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_OpenAmount(), theTypesPackage.getMoney(), "openAmount", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_PayedAmount(), theTypesPackage.getMoney(), "payedAmount", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_DemandAmount(), theTypesPackage.getMoney(), "demandAmount", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_Remark(), ecorePackage.getEString(), "remark", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIInvoice_StateDate(), theTypesPackage.getLocalDate(), "stateDate", null, 0, 1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIInvoice_Payments(), this.getIPayment(), null, "payments", null, 0, -1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIInvoice_Transactions(), this.getIAccountTransaction(), null, "transactions", null, 0, -1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIInvoice_Attachments(), this.getIDocument(), null, "attachments", null, 0, -1, IInvoice.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iInvoiceEClass, null, "addTrace", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "name", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iInvoiceEClass, null, "getTrace", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "name", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iInvoiceEClass, ecorePackage.getEBoolean(), "adjustAmount", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theTypesPackage.getMoney(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iInvoiceEClass, null, "reject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theTypesPackage.getInvoiceRejectCode(), "rejectCode", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "message", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iInvoiceEClass, null, "addAttachment", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIDocument(), "attachment", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iInvoiceEClass, null, "removeAttachment", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIDocument(), "attachment", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iStockEClass, IStock.class, "IStock", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIStock_Code(), ecorePackage.getEString(), "code", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStock_DriverUuid(), ecorePackage.getEString(), "driverUuid", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStock_DriverConfig(), ecorePackage.getEString(), "driverConfig", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStock_Priority(), ecorePackage.getEInt(), "priority", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIStock_Owner(), this.getIPerson(), null, "owner", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStock_Location(), ecorePackage.getEString(), "location", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIStock_Responsible(), this.getIContact(), null, "responsible", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIStock_StockEntries(), this.getIStockEntry(), null, "stockEntries", null, 0, -1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iStockEClass, ecorePackage.getEBoolean(), "isCommissioningSystem", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iStockEntryEClass, IStockEntry.class, "IStockEntry", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIStockEntry_MinimumStock(), ecorePackage.getEInt(), "minimumStock", null, 0, 1, IStockEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStockEntry_CurrentStock(), ecorePackage.getEInt(), "currentStock", null, 0, 1, IStockEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStockEntry_MaximumStock(), ecorePackage.getEInt(), "maximumStock", null, 0, 1, IStockEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStockEntry_FractionUnits(), ecorePackage.getEInt(), "fractionUnits", null, 0, 1, IStockEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIStockEntry_Stock(), this.getIStock(), null, "stock", null, 0, 1, IStockEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIStockEntry_Article(), this.getIArticle(), null, "article", null, 0, 1, IStockEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIStockEntry_Provider(), this.getIContact(), null, "provider", null, 0, 1, IStockEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iOrderEntryEClass, IOrderEntry.class, "IOrderEntry", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIOrderEntry_Order(), this.getIOrder(), null, "order", null, 0, 1, IOrderEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIOrderEntry_Stock(), this.getIStock(), null, "stock", null, 0, 1, IOrderEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIOrderEntry_Amount(), ecorePackage.getEInt(), "amount", null, 0, 1, IOrderEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIOrderEntry_Article(), this.getIArticle(), null, "article", null, 0, 1, IOrderEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIOrderEntry_Provider(), this.getIContact(), null, "provider", null, 0, 1, IOrderEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIOrderEntry_State(), theTypesPackage.getOrderEntryState(), "state", null, 0, 1, IOrderEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iOrderEClass, IOrder.class, "IOrder", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIOrder_Entries(), this.getIOrderEntry(), null, "entries", null, 0, -1, IOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIOrder_Timestamp(), theTypesPackage.getLocalDateTime(), "timestamp", null, 0, 1, IOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIOrder_Name(), ecorePackage.getEString(), "name", null, 0, 1, IOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iOrderEClass, this.getIOrderEntry(), "addEntry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIArticle(), "article", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIStock(), "stock", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIContact(), "provider", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEInt(), "amount", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iOrderEClass, ecorePackage.getEBoolean(), "isDone", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iOrderEClass, this.getIOrderEntry(), "findOrderEntry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIStock(), "stock", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIArticle(), "article", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iOrderEClass, ecorePackage.getEBoolean(), "isPartialDone", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iAddressEClass, IAddress.class, "IAddress", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIAddress_Street1(), ecorePackage.getEString(), "street1", null, 0, 1, IAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAddress_Street2(), ecorePackage.getEString(), "street2", null, 0, 1, IAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAddress_Zip(), ecorePackage.getEString(), "zip", null, 0, 1, IAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAddress_City(), ecorePackage.getEString(), "city", null, 0, 1, IAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAddress_Country(), theTypesPackage.getCountry(), "country", null, 0, 1, IAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAddress_WrittenAddress(), ecorePackage.getEString(), "writtenAddress", null, 0, 1, IAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAddress_Type(), theTypesPackage.getAddressType(), "type", null, 1, 1, IAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIAddress_Contact(), this.getIContact(), null, "contact", null, 1, 1, IAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iImageEClass, IImage.class, "IImage", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIImage_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIImage_Prefix(), ecorePackage.getEString(), "prefix", null, 0, 1, IImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIImage_Title(), ecorePackage.getEString(), "title", null, 0, 1, IImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIImage_Image(), ecorePackage.getEByteArray(), "image", null, 0, 1, IImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIImage_MimeType(), theTypesPackage.getMimeType(), "mimeType", null, 0, 1, IImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(withAssignableIdEClass, WithAssignableId.class, "WithAssignableId", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(withAssignableIdEClass, null, "setId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "id", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iPrescriptionEClass, IPrescription.class, "IPrescription", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIPrescription_Patient(), this.getIPatient(), null, "patient", null, 1, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIPrescription_Article(), this.getIArticle(), null, "article", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_DateFrom(), theTypesPackage.getLocalDateTime(), "dateFrom", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_DateTo(), theTypesPackage.getLocalDateTime(), "dateTo", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_StopReason(), ecorePackage.getEString(), "stopReason", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_DosageInstruction(), ecorePackage.getEString(), "dosageInstruction", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_DisposalComment(), ecorePackage.getEString(), "disposalComment", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_Remark(), ecorePackage.getEString(), "remark", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_EntryType(), theTypesPackage.getEntryType(), "entryType", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_Applied(), ecorePackage.getEBoolean(), "applied", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPrescription_SortOrder(), ecorePackage.getEInt(), "sortOrder", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIPrescription_Prescriptor(), this.getIContact(), null, "prescriptor", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIPrescription_Recipe(), this.getIRecipe(), null, "recipe", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIPrescription_Billed(), this.getIBilled(), null, "billed", null, 0, 1, IPrescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iRightEClass, IRight.class, "IRight", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIRight_Name(), ecorePackage.getEString(), "name", null, 0, 1, IRight.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIRight_LocalizedName(), ecorePackage.getEString(), "localizedName", null, 0, 1, IRight.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIRight_Parent(), this.getIRight(), null, "parent", null, 0, 1, IRight.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iBillingSystemEClass, IBillingSystem.class, "IBillingSystem", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIBillingSystem_Name(), ecorePackage.getEString(), "name", null, 0, 1, IBillingSystem.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBillingSystem_Law(), theTypesPackage.getBillingLaw(), "law", null, 0, 1, IBillingSystem.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iRecipeEClass, IRecipe.class, "IRecipe", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIRecipe_Patient(), this.getIPatient(), null, "patient", null, 1, 1, IRecipe.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIRecipe_Mandator(), this.getIMandator(), null, "mandator", null, 0, 1, IRecipe.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIRecipe_Date(), theTypesPackage.getLocalDateTime(), "date", null, 0, 1, IRecipe.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIRecipe_Prescriptions(), this.getIPrescription(), null, "prescriptions", null, 0, -1, IRecipe.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIRecipe_Document(), this.getIDocumentLetter(), null, "document", null, 0, 1, IRecipe.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iRecipeEClass, null, "removePrescription", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIPrescription(), "prescription", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iBlobSecondaryEClass, IBlobSecondary.class, "IBlobSecondary", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(iRelatedContactEClass, IRelatedContact.class, "IRelatedContact", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIRelatedContact_MyContact(), this.getIContact(), null, "myContact", null, 1, 1, IRelatedContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIRelatedContact_OtherContact(), this.getIContact(), null, "otherContact", null, 0, 1, IRelatedContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIRelatedContact_RelationshipDescription(), ecorePackage.getEString(), "relationshipDescription", null, 0, 1, IRelatedContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIRelatedContact_MyType(), theTypesPackage.getRelationshipType(), "myType", null, 0, 1, IRelatedContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIRelatedContact_OtherType(), theTypesPackage.getRelationshipType(), "otherType", null, 0, 1, IRelatedContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iPaymentEClass, IPayment.class, "IPayment", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIPayment_Invoice(), this.getIInvoice(), null, "invoice", null, 0, 1, IPayment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPayment_Amount(), theTypesPackage.getMoney(), "amount", null, 0, 1, IPayment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPayment_Remark(), ecorePackage.getEString(), "remark", null, 0, 1, IPayment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPayment_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IPayment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iAccountTransactionEClass, IAccountTransaction.class, "IAccountTransaction", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIAccountTransaction_Invoice(), this.getIInvoice(), null, "invoice", null, 0, 1, IAccountTransaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIAccountTransaction_Payment(), this.getIPayment(), null, "payment", null, 0, 1, IAccountTransaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIAccountTransaction_Patient(), this.getIPatient(), null, "patient", null, 0, 1, IAccountTransaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAccountTransaction_Amount(), theTypesPackage.getMoney(), "amount", null, 0, 1, IAccountTransaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAccountTransaction_Remark(), ecorePackage.getEString(), "remark", null, 0, 1, IAccountTransaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAccountTransaction_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IAccountTransaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIAccountTransaction_Account(), this.getIAccount(), null, "account", null, 0, 1, IAccountTransaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iAccountEClass, IAccount.class, "IAccount", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIAccount_Name(), ecorePackage.getEString(), "name", null, 0, 1, IAccount.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAccount_Numeric(), ecorePackage.getEInt(), "numeric", null, 0, 1, IAccount.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iMessageEClass, IMessage.class, "IMessage", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIMessage_Sender(), ecorePackage.getEString(), "sender", null, 1, 1, IMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIMessage_Receiver(), ecorePackage.getEString(), "receiver", null, 0, -1, IMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIMessage_SenderAcceptsAnswer(), ecorePackage.getEBoolean(), "senderAcceptsAnswer", null, 0, 1, IMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIMessage_CreateDateTime(), theTypesPackage.getLocalDateTime(), "createDateTime", null, 0, 1, IMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIMessage_MessageText(), ecorePackage.getEString(), "messageText", null, 0, 1, IMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEAttribute(getIMessage_MessageCodes(), g1, "messageCodes", null, 0, 1, IMessage.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIMessage_MessagePriority(), ecorePackage.getEInt(), "messagePriority", "0", 0, 1, IMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIMessage_PreferredTransporters(), ecorePackage.getEString(), "preferredTransporters", null, 0, -1, IMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iMessageEClass, null, "setSender", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIUser(), "user", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iMessageEClass, null, "addMessageCode", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "key", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iMessageEClass, null, "addReceiver", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "receiver", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iMessageEClass, null, "addReceiver", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIUser(), "receiver", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iTextTemplateEClass, ITextTemplate.class, "ITextTemplate", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getITextTemplate_Category(), theTypesPackage.getTextTemplateCategory(), "category", null, 0, 1, ITextTemplate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getITextTemplate_Mandator(), this.getIMandator(), null, "mandator", null, 0, 1, ITextTemplate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITextTemplate_Name(), ecorePackage.getEString(), "name", null, 0, 1, ITextTemplate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getITextTemplate_Template(), ecorePackage.getEString(), "template", null, 0, 1, ITextTemplate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iAppointmentEClass, IAppointment.class, "IAppointment", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIAppointment_Reason(), ecorePackage.getEString(), "reason", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_State(), ecorePackage.getEString(), "state", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_Type(), ecorePackage.getEString(), "type", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_DurationMinutes(), ecorePackage.getEIntegerObject(), "durationMinutes", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_Schedule(), ecorePackage.getEString(), "schedule", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_SubjectOrPatient(), ecorePackage.getEString(), "subjectOrPatient", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_Priority(), ecorePackage.getEInt(), "priority", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_TreatmentReason(), ecorePackage.getEInt(), "treatmentReason", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_CaseType(), ecorePackage.getEInt(), "caseType", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_InsuranceType(), ecorePackage.getEInt(), "insuranceType", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_Linkgroup(), ecorePackage.getEString(), "linkgroup", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_Extension(), ecorePackage.getEString(), "extension", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_Created(), ecorePackage.getEString(), "created", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_LastEdit(), ecorePackage.getEString(), "lastEdit", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_StateHistory(), ecorePackage.getEString(), "stateHistory", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_Recurring(), ecorePackage.getEBoolean(), "recurring", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointment_CreatedBy(), ecorePackage.getEString(), "createdBy", null, 0, 1, IAppointment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iAppointmentEClass, this.getIContact(), "getContact", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iAppointmentEClass, ecorePackage.getEString(), "getStateHistoryFormatted", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "formatPattern", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iAppointmentSeriesEClass, IAppointmentSeries.class, "IAppointmentSeries", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIAppointmentSeries_SeriesType(), theTypesPackage.getSeriesType(), "seriesType", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointmentSeries_EndingType(), theTypesPackage.getEndingType(), "endingType", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointmentSeries_SeriesStartDate(), theTypesPackage.getLocalDate(), "seriesStartDate", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointmentSeries_SeriesStartTime(), theTypesPackage.getLocalTime(), "seriesStartTime", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointmentSeries_SeriesEndDate(), theTypesPackage.getLocalDate(), "seriesEndDate", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointmentSeries_SeriesEndTime(), theTypesPackage.getLocalTime(), "seriesEndTime", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointmentSeries_SeriesPatternString(), ecorePackage.getEString(), "seriesPatternString", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointmentSeries_EndingPatternString(), ecorePackage.getEString(), "endingPatternString", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIAppointmentSeries_Persistent(), ecorePackage.getEBoolean(), "persistent", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIAppointmentSeries_RootAppointment(), this.getIAppointment(), null, "rootAppointment", null, 0, 1, IAppointmentSeries.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iAppointmentSeriesEClass, ecorePackage.getEString(), "getAsSeriesExtension", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iAppointmentSeriesEClass, null, "getAppointments", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getIAppointment());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(iSickCertificateEClass, ISickCertificate.class, "ISickCertificate", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getISickCertificate_Patient(), this.getIPatient(), null, "patient", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getISickCertificate_Coverage(), this.getICoverage(), null, "coverage", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getISickCertificate_Letter(), this.getIDocumentLetter(), null, "letter", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISickCertificate_Percent(), ecorePackage.getEInt(), "percent", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISickCertificate_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISickCertificate_Start(), theTypesPackage.getLocalDate(), "start", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISickCertificate_End(), theTypesPackage.getLocalDate(), "end", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISickCertificate_Reason(), ecorePackage.getEString(), "reason", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISickCertificate_Note(), ecorePackage.getEString(), "note", null, 0, 1, ISickCertificate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iDayMessageEClass, IDayMessage.class, "IDayMessage", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIDayMessage_Title(), ecorePackage.getEString(), "title", null, 0, 1, IDayMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDayMessage_Message(), ecorePackage.getEString(), "message", null, 0, 1, IDayMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDayMessage_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IDayMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iReminderEClass, IReminder.class, "IReminder", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIReminder_Creator(), this.getIContact(), null, "creator", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIReminder_Responsible(), this.getIContact(), null, "responsible", null, 0, -1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIReminder_Contact(), this.getIContact(), null, "contact", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIReminder_Due(), theTypesPackage.getLocalDate(), "due", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIReminder_Status(), theTypesPackage.getProcessStatus(), "status", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIReminder_Visibility(), theTypesPackage.getVisibility(), "visibility", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIReminder_Subject(), ecorePackage.getEString(), "subject", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIReminder_Message(), ecorePackage.getEString(), "message", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIReminder_Priority(), theTypesPackage.getPriority(), "priority", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIReminder_Type(), theTypesPackage.getType(), "type", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIReminder_ResponsibleAll(), ecorePackage.getEBoolean(), "responsibleAll", null, 0, 1, IReminder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(iReminderEClass, null, "addResponsible", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIContact(), "responsible", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iReminderEClass, null, "removeResponsible", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIContact(), "responsible", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iReminderResponsibleLinkEClass, IReminderResponsibleLink.class, "IReminderResponsibleLink", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIReminderResponsibleLink_Reminder(), this.getIReminder(), null, "reminder", null, 0, 1, IReminderResponsibleLink.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIReminderResponsibleLink_Responsible(), this.getIContact(), null, "responsible", null, 0, 1, IReminderResponsibleLink.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iVaccinationEClass, IVaccination.class, "IVaccination", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIVaccination_Patient(), this.getIPatient(), null, "patient", null, 1, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIVaccination_Article(), this.getIArticle(), null, "article", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIVaccination_ArticleName(), ecorePackage.getEString(), "articleName", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIVaccination_ArticleGtin(), ecorePackage.getEString(), "articleGtin", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIVaccination_ArticleAtc(), ecorePackage.getEString(), "articleAtc", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIVaccination_LotNumber(), ecorePackage.getEString(), "lotNumber", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIVaccination_DateOfAdministration(), theTypesPackage.getLocalDate(), "dateOfAdministration", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIVaccination_IngredientsAtc(), ecorePackage.getEString(), "ingredientsAtc", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIVaccination_Performer(), this.getIContact(), null, "performer", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIVaccination_Side(), ecorePackage.getEString(), "side", null, 0, 1, IVaccination.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://elexis.info/jpa/entity/attribute/mapping
		createMappingAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://elexis.info/jpa/entity/attribute/mapping</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createMappingAnnotations() {
		String source = "http://elexis.info/jpa/entity/attribute/mapping";
		addAnnotation
		  (getIContact_Organization(),
		   source,
		   new String[] {
			   "attributeName", "organisation"
		   });
		addAnnotation
		  (getIPerson_DateOfBirth(),
		   source,
		   new String[] {
			   "attributeName", "dob"
		   });
		addAnnotation
		  (getIPerson_FirstName(),
		   source,
		   new String[] {
			   "attributeName", "description2"
		   });
		addAnnotation
		  (getIPerson_LastName(),
		   source,
		   new String[] {
			   "attributeName", "description1"
		   });
		addAnnotation
		  (getIUser_AssignedContact(),
		   source,
		   new String[] {
			   "attributeName", "kontakt"
		   });
		addAnnotation
		  (getILabMapping_Item(),
		   source,
		   new String[] {
			   "attributeName", "labitem"
		   });
		addAnnotation
		  (getIDocument_Category(),
		   source,
		   new String[] {
			   "Brief#attributeName", "typ",
			   "DocHandle#attributeName", "category",
			   "BriefVorlage#attributeName", "typ"
		   });
		addAnnotation
		  (getIDocument_Patient(),
		   source,
		   new String[] {
			   "DocHandle#attributeName", "kontakt",
			   "Brief#attributeName", "patient"
		   });
		addAnnotation
		  (getIArticle_Gtin(),
		   source,
		   new String[] {
			   "attributeName", "ean"
		   });
		addAnnotation
		  (getIArticle_PurchasePrice(),
		   source,
		   new String[] {
			   "attributeName", "ekPreis"
		   });
		addAnnotation
		  (getIArticle_SellingPrice(),
		   source,
		   new String[] {
			   "attributeName", "vkPreis"
		   });
		addAnnotation
		  (getIArticle_SubTyp(),
		   source,
		   new String[] {
			   "attributeName", "codeclass"
		   });
		addAnnotation
		  (getICoverage_Patient(),
		   source,
		   new String[] {
			   "attributeName", "patientKontakt"
		   });
		addAnnotation
		  (getICoverage_DateFrom(),
		   source,
		   new String[] {
			   "attributeName", "datumvon"
		   });
		addAnnotation
		  (getICoverage_CostBearer(),
		   source,
		   new String[] {
			   "attributeName", "kostentrkontakt"
		   });
		addAnnotation
		  (getICoverage_InsuranceNumber(),
		   source,
		   new String[] {
			   "attributeName", "versnummer"
		   });
		addAnnotation
		  (getICoverage_DateTo(),
		   source,
		   new String[] {
			   "attributeName", "datumbis"
		   });
		addAnnotation
		  (getIBillingSystemFactor_System(),
		   source,
		   new String[] {
			   "attributeName", "typ"
		   });
		addAnnotation
		  (getIBillingSystemFactor_ValidFrom(),
		   source,
		   new String[] {
			   "attributeName", "datum_von"
		   });
		addAnnotation
		  (getIBillingSystemFactor_ValidTo(),
		   source,
		   new String[] {
			   "attributeName", "datum_bis"
		   });
		addAnnotation
		  (getIConfig_Key(),
		   source,
		   new String[] {
			   "attributeName", "param"
		   });
		addAnnotation
		  (getIUserConfig_Owner(),
		   source,
		   new String[] {
			   "attributeName", "ownerId"
		   });
		addAnnotation
		  (getIBlob_Date(),
		   source,
		   new String[] {
			   "attributeName", "datum"
		   });
		addAnnotation
		  (getIEncounter_Date(),
		   source,
		   new String[] {
			   "attributeName", "datum"
		   });
		addAnnotation
		  (getIEncounter_Mandator(),
		   source,
		   new String[] {
			   "attributeName", "mandant"
		   });
		addAnnotation
		  (getIEncounter_Coverage(),
		   source,
		   new String[] {
			   "attributeName", "fall"
		   });
		addAnnotation
		  (getIEncounter_VersionedEntry(),
		   source,
		   new String[] {
			   "attributeName", "eintrag"
		   });
		addAnnotation
		  (getIPrescription_DateTo(),
		   source,
		   new String[] {
			   "attributeName", "dateUntil"
		   });
		addAnnotation
		  (getIAccountTransaction_Payment(),
		   source,
		   new String[] {
			   "attributeName", "zahlung"
		   });
		addAnnotation
		  (getIAppointment_Reason(),
		   source,
		   new String[] {
			   "attributeName", "grund"
		   });
		addAnnotation
		  (getIAppointment_Type(),
		   source,
		   new String[] {
			   "attributeName", "termintyp"
		   });
		addAnnotation
		  (getIAppointment_DurationMinutes(),
		   source,
		   new String[] {
			   "attributeName", "dauer"
		   });
		addAnnotation
		  (getIAppointment_Schedule(),
		   source,
		   new String[] {
			   "attributeName", "bereich"
		   });
		addAnnotation
		  (getIAppointment_SubjectOrPatient(),
		   source,
		   new String[] {
			   "attributeName", "patid"
		   });
		addAnnotation
		  (getIAppointment_Created(),
		   source,
		   new String[] {
			   "attributeName", "angelegt"
		   });
		addAnnotation
		  (getIAppointment_LastEdit(),
		   source,
		   new String[] {
			   "attributeName", "lastedit"
		   });
		addAnnotation
		  (getIAppointment_StateHistory(),
		   source,
		   new String[] {
			   "attributeName", "statusHistory"
		   });
		addAnnotation
		  (getIAppointment_CreatedBy(),
		   source,
		   new String[] {
			   "attributeName", "erstelltvon"
		   });
		addAnnotation
		  (getIReminder_Contact(),
		   source,
		   new String[] {
			   "attributeName", "kontakt"
		   });
		addAnnotation
		  (getIReminder_Due(),
		   source,
		   new String[] {
			   "attributeName", "dateDue"
		   });
	}

} //ModelPackageImpl
