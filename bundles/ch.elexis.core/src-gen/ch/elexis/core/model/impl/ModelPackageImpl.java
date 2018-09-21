/**
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
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
import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBillableVerifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.IBlob;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IHistory;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPeriod;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserConfig;
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
	private EClass iContactEClass = null;

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
	private EClass iCodeElementEClass = null;

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
	private EClass iPersonEClass = null;

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
	private EClass iUserEClass = null;

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
	private EClass iPeriodEClass = null;

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
	private EClass iCategoryEClass = null;

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
	private EClass iDiagnosisEClass = null;

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
	private EClass iBillableEClass = null;

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
	private EClass iDocumentLetterEClass = null;

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
	private EClass iMandatorEClass = null;

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
	private EClass iImageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass withAssignableIdEClass = null;

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
	public EClass getIContact() {
		return iContactEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Mandator() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_User() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Patient() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Person() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Laboratory() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Organization() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Description1() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Description2() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Description3() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Code() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Country() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Zip() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_City() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Street() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Phone1() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Phone2() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Fax() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Email() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Website() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Mobile() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Comment() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIContact_Address() {
		return (EReference)iContactEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIXid() {
		return iXidEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIXid_Domain() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIXid_DomainId() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIXid_Quality() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIXid_ObjectId() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getICodeElement() {
		return iCodeElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getICodeElement_Code() {
		return (EAttribute)iCodeElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getICodeElement_Text() {
		return (EAttribute)iCodeElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getISticker() {
		return iStickerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getISticker_Background() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getISticker_Foreground() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getISticker_Visible() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getISticker_Value() {
		return (EAttribute)iStickerEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIPerson() {
		return iPersonEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPerson_DateOfBirth() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPerson_Gender() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPerson_Titel() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPerson_TitelSuffix() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPerson_FirstName() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPerson_LastName() {
		return (EAttribute)iPersonEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIPatient() {
		return iPatientEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPatient_Diagnosen() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPatient_Risk() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPatient_FamilyAnamnese() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPatient_PersonalAnamnese() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPatient_Allergies() {
		return (EAttribute)iPatientEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIPatient_Coverages() {
		return (EReference)iPatientEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIUser() {
		return iUserEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIUser_Username() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIUser_HashedPassword() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIUser_Salt() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIUser_AssignedContact() {
		return (EReference)iUserEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIUser_Roles() {
		return (EReference)iUserEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIUser_Active() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIdentifiable() {
		return identifiableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDeleteable() {
		return deleteableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDeleteable_Deleted() {
		return (EAttribute)deleteableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getILabItem() {
		return iLabItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Typ() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_ReferenceMale() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_ReferenceFemale() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Group() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Priority() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Code() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Unit() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Name() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Digits() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Visible() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Formula() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_LoincCode() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_BillingCode() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getILabResult() {
		return iLabResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Unit() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_AnalyseTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_ObservationTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_TransmissionTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Pathologic() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Result() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Comment() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_ReferenceMale() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_ReferenceFemale() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Date() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabResult_Item() {
		return (EReference)iLabResultEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabResult_Patient() {
		return (EReference)iLabResultEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_PathologicDescription() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabResult_Origin() {
		return (EReference)iLabResultEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getILabOrder() {
		return iLabOrderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabOrder_Result() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabOrder_Item() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabOrder_Patient() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabOrder_TimeStamp() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabOrder_ObservationTime() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabOrder_User() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabOrder_Mandator() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabOrder_OrderId() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabOrder_State() {
		return (EAttribute)iLabOrderEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getILabMapping() {
		return iLabMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabMapping_ItemName() {
		return (EAttribute)iLabMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabMapping_Item() {
		return (EReference)iLabMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabMapping_Origin() {
		return (EReference)iLabMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabMapping_Charge() {
		return (EAttribute)iLabMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIPeriod() {
		return iPeriodEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPeriod_StartTime() {
		return (EAttribute)iPeriodEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIPeriod_EndTime() {
		return (EAttribute)iPeriodEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIDocument() {
		return iDocumentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Title() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Description() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Status() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Created() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Lastchanged() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_MimeType() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDocument_Category() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDocument_History() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_StoreId() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Extension() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Keywords() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDocument_Patient() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDocument_Author() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getICategory() {
		return iCategoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getICategory_Name() {
		return (EAttribute)iCategoryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIHistory() {
		return iHistoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIHistory_Date() {
		return (EAttribute)iHistoryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIHistory_Status() {
		return (EAttribute)iHistoryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIHistory_Description() {
		return (EAttribute)iHistoryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIDiagnosis() {
		return iDiagnosisEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDiagnosis_Description() {
		return (EAttribute)iDiagnosisEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIDiagnosisTree() {
		return iDiagnosisTreeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDiagnosisTree_Parent() {
		return (EReference)iDiagnosisTreeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDiagnosisTree_Children() {
		return (EReference)iDiagnosisTreeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIBillable() {
		return iBillableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getICoverage() {
		return iCoverageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getICoverage_Patient() {
		return (EReference)iCoverageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getICoverage_Description() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getICoverage_Reason() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getICoverage_DateFrom() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getICoverage_BillingSystem() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getICoverage_CostBearer() {
		return (EReference)iCoverageEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getICoverage_InsuranceNumber() {
		return (EAttribute)iCoverageEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIBillingSystemFactor() {
		return iBillingSystemFactorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIBillingSystemFactor_System() {
		return (EAttribute)iBillingSystemFactorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIBillingSystemFactor_Factor() {
		return (EAttribute)iBillingSystemFactorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIBillingSystemFactor_ValidFrom() {
		return (EAttribute)iBillingSystemFactorEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIBillingSystemFactor_ValidTo() {
		return (EAttribute)iBillingSystemFactorEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIOrganization() {
		return iOrganizationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getILaboratory() {
		return iLaboratoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIDocumentLetter() {
		return iDocumentLetterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIConfig() {
		return iConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIConfig_Key() {
		return (EAttribute)iConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIConfig_Value() {
		return (EAttribute)iConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIUserConfig() {
		return iUserConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIUserConfig_Owner() {
		return (EReference)iUserConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIMandator() {
		return iMandatorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIArticle() {
		return iArticleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_Gtin() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_AtcCode() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_Name() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_SellingSize() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_PackageSize() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_PackageUnit() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIArticle_Product() {
		return (EReference)iArticleEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_PurchasePrice() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_SellingPrice() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_Obligation() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_Typ() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIArticle_SubTyp() {
		return (EAttribute)iArticleEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getWithExtInfo() {
		return withExtInfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIRole() {
		return iRoleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIRole_SystemRole() {
		return (EAttribute)iRoleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIBlob() {
		return iBlobEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIBlob_Content() {
		return (EAttribute)iBlobEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIBlob_Date() {
		return (EAttribute)iBlobEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIBillableVerifier() {
		return iBillableVerifierEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIBillableOptifier() {
		return iBillableOptifierEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIEncounter() {
		return iEncounterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIEncounter_TimeStamp() {
		return (EAttribute)iEncounterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIEncounter_Patient() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIEncounter_Coverage() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIEncounter_Mandator() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIEncounter_Billed() {
		return (EReference)iEncounterEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIEncounter_Date() {
		return (EAttribute)iEncounterEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIEncounter_VersionedEntry() {
		return (EAttribute)iEncounterEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIBilled() {
		return iBilledEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIBilled_Billable() {
		return (EReference)iBilledEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIBilled_Amount() {
		return (EAttribute)iBilledEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIStock() {
		return iStockEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIStock_Code() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIStock_DriverUuid() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIStock_DriverConfig() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIStock_Priority() {
		return (EAttribute)iStockEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIStock_Owner() {
		return (EReference)iStockEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIStockEntry() {
		return iStockEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIStockEntry_MinimumStock() {
		return (EAttribute)iStockEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIStockEntry_CurrentStock() {
		return (EAttribute)iStockEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIStockEntry_MaximumStock() {
		return (EAttribute)iStockEntryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIStockEntry_FractionUnits() {
		return (EAttribute)iStockEntryEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIStockEntry_Stock() {
		return (EReference)iStockEntryEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIStockEntry_Article() {
		return (EReference)iStockEntryEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIStockEntry_Provider() {
		return (EReference)iStockEntryEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIOrderEntry() {
		return iOrderEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIOrderEntry_Order() {
		return (EReference)iOrderEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIOrderEntry_Stock() {
		return (EReference)iOrderEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIOrderEntry_Amount() {
		return (EAttribute)iOrderEntryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIOrderEntry_Article() {
		return (EReference)iOrderEntryEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIOrderEntry_Provider() {
		return (EReference)iOrderEntryEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIOrderEntry_State() {
		return (EAttribute)iOrderEntryEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIOrder() {
		return iOrderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIOrder_Entries() {
		return (EReference)iOrderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIOrder_Timestamp() {
		return (EAttribute)iOrderEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIOrder_Name() {
		return (EAttribute)iOrderEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIAddress() {
		return iAddressEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIAddress_Street1() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIAddress_Street2() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIAddress_Zip() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIAddress_City() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIAddress_Country() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIAddress_WrittenAddress() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIAddress_Type() {
		return (EAttribute)iAddressEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIAddress_Contact() {
		return (EReference)iAddressEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIImage() {
		return iImageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIImage_Date() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIImage_Prefix() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIImage_Title() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIImage_Image() {
		return (EAttribute)iImageEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getWithAssignableId() {
		return withAssignableIdEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
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

		deleteableEClass = createEClass(DELETEABLE);
		createEAttribute(deleteableEClass, DELETEABLE__DELETED);

		iXidEClass = createEClass(IXID);
		createEAttribute(iXidEClass, IXID__DOMAIN);
		createEAttribute(iXidEClass, IXID__DOMAIN_ID);
		createEAttribute(iXidEClass, IXID__QUALITY);
		createEAttribute(iXidEClass, IXID__OBJECT_ID);

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

		iPersonEClass = createEClass(IPERSON);
		createEAttribute(iPersonEClass, IPERSON__DATE_OF_BIRTH);
		createEAttribute(iPersonEClass, IPERSON__GENDER);
		createEAttribute(iPersonEClass, IPERSON__TITEL);
		createEAttribute(iPersonEClass, IPERSON__TITEL_SUFFIX);
		createEAttribute(iPersonEClass, IPERSON__FIRST_NAME);
		createEAttribute(iPersonEClass, IPERSON__LAST_NAME);

		iOrganizationEClass = createEClass(IORGANIZATION);

		iLaboratoryEClass = createEClass(ILABORATORY);

		iPatientEClass = createEClass(IPATIENT);
		createEAttribute(iPatientEClass, IPATIENT__DIAGNOSEN);
		createEAttribute(iPatientEClass, IPATIENT__RISK);
		createEAttribute(iPatientEClass, IPATIENT__FAMILY_ANAMNESE);
		createEAttribute(iPatientEClass, IPATIENT__PERSONAL_ANAMNESE);
		createEAttribute(iPatientEClass, IPATIENT__ALLERGIES);
		createEReference(iPatientEClass, IPATIENT__COVERAGES);

		iMandatorEClass = createEClass(IMANDATOR);

		iUserEClass = createEClass(IUSER);
		createEAttribute(iUserEClass, IUSER__USERNAME);
		createEAttribute(iUserEClass, IUSER__HASHED_PASSWORD);
		createEAttribute(iUserEClass, IUSER__SALT);
		createEReference(iUserEClass, IUSER__ASSIGNED_CONTACT);
		createEReference(iUserEClass, IUSER__ROLES);
		createEAttribute(iUserEClass, IUSER__ACTIVE);

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

		iDocumentLetterEClass = createEClass(IDOCUMENT_LETTER);

		iStickerEClass = createEClass(ISTICKER);
		createEAttribute(iStickerEClass, ISTICKER__BACKGROUND);
		createEAttribute(iStickerEClass, ISTICKER__FOREGROUND);
		createEAttribute(iStickerEClass, ISTICKER__VISIBLE);
		createEAttribute(iStickerEClass, ISTICKER__VALUE);

		iCodeElementEClass = createEClass(ICODE_ELEMENT);
		createEAttribute(iCodeElementEClass, ICODE_ELEMENT__CODE);
		createEAttribute(iCodeElementEClass, ICODE_ELEMENT__TEXT);

		iCategoryEClass = createEClass(ICATEGORY);
		createEAttribute(iCategoryEClass, ICATEGORY__NAME);

		iHistoryEClass = createEClass(IHISTORY);
		createEAttribute(iHistoryEClass, IHISTORY__DATE);
		createEAttribute(iHistoryEClass, IHISTORY__STATUS);
		createEAttribute(iHistoryEClass, IHISTORY__DESCRIPTION);

		iDiagnosisEClass = createEClass(IDIAGNOSIS);
		createEAttribute(iDiagnosisEClass, IDIAGNOSIS__DESCRIPTION);

		iDiagnosisTreeEClass = createEClass(IDIAGNOSIS_TREE);
		createEReference(iDiagnosisTreeEClass, IDIAGNOSIS_TREE__PARENT);
		createEReference(iDiagnosisTreeEClass, IDIAGNOSIS_TREE__CHILDREN);

		iBillableEClass = createEClass(IBILLABLE);

		iCoverageEClass = createEClass(ICOVERAGE);
		createEReference(iCoverageEClass, ICOVERAGE__PATIENT);
		createEAttribute(iCoverageEClass, ICOVERAGE__DESCRIPTION);
		createEAttribute(iCoverageEClass, ICOVERAGE__REASON);
		createEAttribute(iCoverageEClass, ICOVERAGE__DATE_FROM);
		createEAttribute(iCoverageEClass, ICOVERAGE__BILLING_SYSTEM);
		createEReference(iCoverageEClass, ICOVERAGE__COST_BEARER);
		createEAttribute(iCoverageEClass, ICOVERAGE__INSURANCE_NUMBER);

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

		withExtInfoEClass = createEClass(WITH_EXT_INFO);

		iRoleEClass = createEClass(IROLE);
		createEAttribute(iRoleEClass, IROLE__SYSTEM_ROLE);

		iBlobEClass = createEClass(IBLOB);
		createEAttribute(iBlobEClass, IBLOB__CONTENT);
		createEAttribute(iBlobEClass, IBLOB__DATE);

		iBillableVerifierEClass = createEClass(IBILLABLE_VERIFIER);

		iBillableOptifierEClass = createEClass(IBILLABLE_OPTIFIER);

		iEncounterEClass = createEClass(IENCOUNTER);
		createEAttribute(iEncounterEClass, IENCOUNTER__TIME_STAMP);
		createEReference(iEncounterEClass, IENCOUNTER__PATIENT);
		createEReference(iEncounterEClass, IENCOUNTER__COVERAGE);
		createEReference(iEncounterEClass, IENCOUNTER__MANDATOR);
		createEReference(iEncounterEClass, IENCOUNTER__BILLED);
		createEAttribute(iEncounterEClass, IENCOUNTER__DATE);
		createEAttribute(iEncounterEClass, IENCOUNTER__VERSIONED_ENTRY);

		iBilledEClass = createEClass(IBILLED);
		createEReference(iBilledEClass, IBILLED__BILLABLE);
		createEAttribute(iBilledEClass, IBILLED__AMOUNT);

		iStockEClass = createEClass(ISTOCK);
		createEAttribute(iStockEClass, ISTOCK__CODE);
		createEAttribute(iStockEClass, ISTOCK__DRIVER_UUID);
		createEAttribute(iStockEClass, ISTOCK__DRIVER_CONFIG);
		createEAttribute(iStockEClass, ISTOCK__PRIORITY);
		createEReference(iStockEClass, ISTOCK__OWNER);

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

		withAssignableIdEClass = createEClass(WITH_ASSIGNABLE_ID);
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

		// Set bounds for type parameters

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
		EGenericType g1 = createEGenericType(theTypesPackage.getComparable());
		EGenericType g2 = createEGenericType(this.getISticker());
		g1.getETypeArguments().add(g2);
		iStickerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getDeleteable());
		iStickerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getIdentifiable());
		iStickerEClass.getEGenericSuperTypes().add(g1);
		iDiagnosisEClass.getESuperTypes().add(this.getICodeElement());
		iDiagnosisEClass.getESuperTypes().add(this.getIdentifiable());
		iDiagnosisTreeEClass.getESuperTypes().add(this.getIDiagnosis());
		iBillableEClass.getESuperTypes().add(this.getICodeElement());
		iBillableEClass.getESuperTypes().add(this.getIdentifiable());
		iCoverageEClass.getESuperTypes().add(this.getDeleteable());
		iCoverageEClass.getESuperTypes().add(this.getIdentifiable());
		iCoverageEClass.getESuperTypes().add(this.getWithExtInfo());
		iBillingSystemFactorEClass.getESuperTypes().add(this.getIdentifiable());
		iConfigEClass.getESuperTypes().add(this.getIdentifiable());
		iUserConfigEClass.getESuperTypes().add(this.getIConfig());
		iPeriodEClass.getESuperTypes().add(this.getIdentifiable());
		iPeriodEClass.getESuperTypes().add(this.getDeleteable());
		iArticleEClass.getESuperTypes().add(this.getIdentifiable());
		iArticleEClass.getESuperTypes().add(this.getIBillable());
		iArticleEClass.getESuperTypes().add(this.getDeleteable());
		iArticleEClass.getESuperTypes().add(this.getWithExtInfo());
		iRoleEClass.getESuperTypes().add(this.getIdentifiable());
		iRoleEClass.getESuperTypes().add(this.getWithAssignableId());
		iBlobEClass.getESuperTypes().add(this.getIdentifiable());
		iBlobEClass.getESuperTypes().add(this.getDeleteable());
		iBlobEClass.getESuperTypes().add(this.getWithAssignableId());
		iEncounterEClass.getESuperTypes().add(this.getIdentifiable());
		iEncounterEClass.getESuperTypes().add(this.getDeleteable());
		iBilledEClass.getESuperTypes().add(this.getIdentifiable());
		iBilledEClass.getESuperTypes().add(this.getDeleteable());
		iStockEClass.getESuperTypes().add(this.getIdentifiable());
		iStockEClass.getESuperTypes().add(this.getDeleteable());
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

		// Initialize classes and features; add operations and parameters
		initEClass(identifiableEClass, Identifiable.class, "Identifiable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		addEOperation(identifiableEClass, ecorePackage.getEString(), "getId", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(identifiableEClass, ecorePackage.getEString(), "getLabel", 0, 1, IS_UNIQUE, IS_ORDERED);

		EOperation op = addEOperation(identifiableEClass, ecorePackage.getEBoolean(), "addXid", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "domain", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "id", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "updateIfExists", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(identifiableEClass, this.getIXid(), "getXid", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "domain", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(deleteableEClass, Deleteable.class, "Deleteable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDeleteable_Deleted(), ecorePackage.getEBoolean(), "deleted", null, 0, 1, Deleteable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iXidEClass, IXid.class, "IXid", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIXid_Domain(), ecorePackage.getEString(), "domain", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIXid_DomainId(), ecorePackage.getEString(), "domainId", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIXid_Quality(), theTypesPackage.getXidQuality(), "quality", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIXid_ObjectId(), ecorePackage.getEString(), "objectId", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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

		op = addEOperation(iContactEClass, this.getIAddress(), "addAddress", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIAddress(), "address", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iPersonEClass, IPerson.class, "IPerson", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIPerson_DateOfBirth(), theTypesPackage.getLocalDateTime(), "dateOfBirth", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_Gender(), theTypesPackage.getGender(), "gender", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_Titel(), ecorePackage.getEString(), "titel", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_TitelSuffix(), ecorePackage.getEString(), "titelSuffix", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_FirstName(), ecorePackage.getEString(), "firstName", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_LastName(), ecorePackage.getEString(), "lastName", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iPersonEClass, ecorePackage.getEInt(), "getAgeInYears", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iOrganizationEClass, IOrganization.class, "IOrganization", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(iLaboratoryEClass, ILaboratory.class, "ILaboratory", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(iPatientEClass, IPatient.class, "IPatient", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIPatient_Diagnosen(), ecorePackage.getEString(), "diagnosen", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_Risk(), ecorePackage.getEString(), "risk", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_FamilyAnamnese(), ecorePackage.getEString(), "familyAnamnese", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_PersonalAnamnese(), ecorePackage.getEString(), "personalAnamnese", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_Allergies(), ecorePackage.getEString(), "allergies", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIPatient_Coverages(), this.getICoverage(), null, "coverages", null, 0, -1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iPatientEClass, ecorePackage.getEString(), "getPatientLabel", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iPatientEClass, ecorePackage.getEString(), "getPatientNr", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPatientEClass, null, "setPatientNr", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "patientNr", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPatientEClass, this.getICoverage(), "addCoverage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getICoverage(), "coverage", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iMandatorEClass, IMandator.class, "IMandator", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(iUserEClass, IUser.class, "IUser", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIUser_Username(), ecorePackage.getEString(), "username", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_HashedPassword(), ecorePackage.getEString(), "hashedPassword", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_Salt(), ecorePackage.getEString(), "salt", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIUser_AssignedContact(), this.getIContact(), null, "assignedContact", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIUser_Roles(), this.getIRole(), null, "roles", null, 0, -1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_Active(), ecorePackage.getEBoolean(), "active", null, 1, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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

		initEClass(iLabMappingEClass, ILabMapping.class, "ILabMapping", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getILabMapping_ItemName(), ecorePackage.getEString(), "itemName", null, 0, 1, ILabMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabMapping_Item(), this.getILabItem(), null, "item", null, 0, 1, ILabMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabMapping_Origin(), this.getIContact(), null, "origin", null, 0, 1, ILabMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabMapping_Charge(), ecorePackage.getEBoolean(), "charge", null, 0, 1, ILabMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iDocumentEClass, IDocument.class, "IDocument", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIDocument_Title(), ecorePackage.getEString(), "title", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_Description(), ecorePackage.getEString(), "description", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_Status(), theTypesPackage.getDocumentStatus(), "status", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
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

		initEClass(iDocumentLetterEClass, IDocumentLetter.class, "IDocumentLetter", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(iStickerEClass, ISticker.class, "ISticker", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getISticker_Background(), ecorePackage.getEString(), "background", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Foreground(), ecorePackage.getEString(), "foreground", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Visible(), ecorePackage.getEBoolean(), "visible", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Value(), ecorePackage.getEInt(), "value", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iStickerEClass, ecorePackage.getEString(), "getLabel", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iStickerEClass, null, "setClassForSticker", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEJavaClass());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "clazz", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iStickerEClass, null, "removeClassForSticker", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEJavaClass());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		addEParameter(op, g1, "clazz", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iStickerEClass, ecorePackage.getEString(), "getClassesForSticker", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(iCodeElementEClass, ICodeElement.class, "ICodeElement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getICodeElement_Code(), ecorePackage.getEString(), "code", null, 0, 1, ICodeElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICodeElement_Text(), ecorePackage.getEString(), "text", null, 0, 1, ICodeElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getCodeSystemName", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getCodeSystemCode", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iCategoryEClass, ICategory.class, "ICategory", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getICategory_Name(), ecorePackage.getEString(), "name", null, 0, 1, ICategory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iHistoryEClass, IHistory.class, "IHistory", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIHistory_Date(), ecorePackage.getEDate(), "date", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIHistory_Status(), theTypesPackage.getDocumentStatus(), "status", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIHistory_Description(), ecorePackage.getEString(), "description", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iDiagnosisEClass, IDiagnosis.class, "IDiagnosis", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIDiagnosis_Description(), ecorePackage.getEString(), "description", null, 0, 1, IDiagnosis.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iDiagnosisTreeEClass, IDiagnosisTree.class, "IDiagnosisTree", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIDiagnosisTree_Parent(), this.getIDiagnosisTree(), null, "parent", null, 0, 1, IDiagnosisTree.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIDiagnosisTree_Children(), this.getIDiagnosisTree(), null, "children", null, 0, -1, IDiagnosisTree.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iBillableEClass, IBillable.class, "IBillable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		addEOperation(iBillableEClass, theTypesPackage.getVatInfo(), "getVatInfo", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iBillableEClass, this.getIBillableOptifier(), "getOptifier", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iBillableEClass, this.getIBillableVerifier(), "getVerifier", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iCoverageEClass, ICoverage.class, "ICoverage", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getICoverage_Patient(), this.getIPatient(), null, "patient", null, 1, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_Description(), ecorePackage.getEString(), "description", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_Reason(), ecorePackage.getEString(), "reason", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_DateFrom(), theTypesPackage.getLocalDate(), "dateFrom", null, 1, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_BillingSystem(), ecorePackage.getEString(), "billingSystem", null, 1, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getICoverage_CostBearer(), this.getIContact(), null, "costBearer", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getICoverage_InsuranceNumber(), ecorePackage.getEString(), "insuranceNumber", null, 0, 1, ICoverage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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

		initEClass(iArticleEClass, IArticle.class, "IArticle", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIArticle_Gtin(), ecorePackage.getEString(), "gtin", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_AtcCode(), ecorePackage.getEString(), "atcCode", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_Name(), ecorePackage.getEString(), "name", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_SellingSize(), ecorePackage.getEInt(), "sellingSize", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_PackageSize(), ecorePackage.getEInt(), "packageSize", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_PackageUnit(), ecorePackage.getEString(), "packageUnit", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIArticle_Product(), this.getIArticle(), null, "product", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_PurchasePrice(), ecorePackage.getEString(), "purchasePrice", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_SellingPrice(), ecorePackage.getEString(), "sellingPrice", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_Obligation(), ecorePackage.getEBoolean(), "obligation", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_Typ(), theTypesPackage.getArticleTyp(), "typ", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIArticle_SubTyp(), theTypesPackage.getArticleSubTyp(), "subTyp", null, 0, 1, IArticle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iArticleEClass, ecorePackage.getEBoolean(), "isProduct", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iArticleEClass, null, "getPackages", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getList());
		g2 = createEGenericType(this.getIArticle());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iArticleEClass, null, "setCode", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "code", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(withExtInfoEClass, WithExtInfo.class, "WithExtInfo", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(withExtInfoEClass, ecorePackage.getEJavaObject(), "getExtInfo", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "key", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(withExtInfoEClass, null, "setExtInfo", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "key", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iRoleEClass, IRole.class, "IRole", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIRole_SystemRole(), ecorePackage.getEBoolean(), "systemRole", null, 0, 1, IRole.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iBlobEClass, IBlob.class, "IBlob", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIBlob_Content(), ecorePackage.getEByteArray(), "content", null, 0, 1, IBlob.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBlob_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IBlob.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iBlobEClass, ecorePackage.getEString(), "getStringContent", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iBlobEClass, null, "setStringContent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iBillableVerifierEClass, IBillableVerifier.class, "IBillableVerifier", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(iBillableVerifierEClass, null, "verifyAdd", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIBillable(), "billable", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIEncounter(), "encounter", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getResult());
		g2 = createEGenericType(this.getIBillable());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(iBillableOptifierEClass, IBillableOptifier.class, "IBillableOptifier", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(iBillableOptifierEClass, null, "add", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIBillable(), "billable", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIEncounter(), "encounter", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getResult());
		g2 = createEGenericType(this.getIBillable());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = addEOperation(iBillableOptifierEClass, null, "remove", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIBilled(), "billed", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIEncounter(), "encounter", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theTypesPackage.getResult());
		g2 = createEGenericType(this.getIBilled());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(iEncounterEClass, IEncounter.class, "IEncounter", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIEncounter_TimeStamp(), theTypesPackage.getLocalDateTime(), "timeStamp", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Patient(), this.getIPatient(), null, "patient", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Coverage(), this.getICoverage(), null, "coverage", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Mandator(), this.getIMandator(), null, "mandator", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIEncounter_Billed(), this.getIBilled(), null, "billed", null, 0, -1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIEncounter_Date(), theTypesPackage.getLocalDate(), "date", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIEncounter_VersionedEntry(), theTypesPackage.getVersionedResource(), "versionedEntry", null, 0, 1, IEncounter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iBilledEClass, IBilled.class, "IBilled", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIBilled_Billable(), this.getIBillable(), null, "billable", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIBilled_Amount(), ecorePackage.getEInt(), "amount", null, 0, 1, IBilled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iStockEClass, IStock.class, "IStock", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIStock_Code(), ecorePackage.getEString(), "code", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStock_DriverUuid(), ecorePackage.getEString(), "driverUuid", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStock_DriverConfig(), ecorePackage.getEString(), "driverConfig", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIStock_Priority(), ecorePackage.getEInt(), "priority", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIStock_Owner(), this.getIMandator(), null, "owner", null, 0, 1, IStock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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

		op = addEOperation(iOrderEClass, null, "removeEntry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIOrderEntry(), "entry", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iOrderEClass, null, "addEntry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIOrderEntry(), "entry", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iOrderEClass, ecorePackage.getEBoolean(), "isDone", 0, 1, IS_UNIQUE, IS_ORDERED);

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

		initEClass(withAssignableIdEClass, WithAssignableId.class, "WithAssignableId", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(withAssignableIdEClass, null, "setId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "id", 0, 1, IS_UNIQUE, IS_ORDERED);

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
		  (getIXid_ObjectId(),
		   source,
		   new String[] {
			   "attributeName", "object"
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
		  (getILabMapping_Item(),
		   source,
		   new String[] {
			   "attributeName", "labitem"
		   });
		addAnnotation
		  (getIDocument_Category(),
		   source,
		   new String[] {
			   "Brief#attributeName", "typ"
		   });
		addAnnotation
		  (getICoverage_Patient(),
		   source,
		   new String[] {
			   "attributeName", "patientKontakt"
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
	}

} //ModelPackageImpl
