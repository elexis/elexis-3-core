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
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IChangeListener;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IHistory;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPeriod;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.ITag;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelFactory;
import ch.elexis.core.model.ModelPackage;
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
	private EClass iPersistentObjectEClass = null;

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
	private EClass iChangeListenerEClass = null;

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
	private EDataType stringArrayEDataType = null;

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
		ModelPackageImpl theModelPackage = (ModelPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof ModelPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new ModelPackageImpl());

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
	public EAttribute getIContact_ContactType() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Mandator() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_User() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Patient() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Description1() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Description2() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Description3() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Code() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Country() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Zip() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_City() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Street() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Phone1() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Phone2() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Fax() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Email() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Website() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Mobile() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIContact_Comment() {
		return (EAttribute)iContactEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIPersistentObject() {
		return iPersistentObjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIPersistentObject_Xid() {
		return (EReference)iPersistentObjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIPersistentObject_Xids() {
		return (EReference)iPersistentObjectEClass.getEStructuralFeatures().get(1);
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
	public EReference getIXid_Object() {
		return (EReference)iXidEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIXid_Quality() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIXid_GUID() {
		return (EAttribute)iXidEClass.getEStructuralFeatures().get(4);
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
	public EClass getIChangeListener() {
		return iChangeListenerEClass;
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
	public EAttribute getIUser_Password() {
		return (EAttribute)iUserEClass.getEStructuralFeatures().get(1);
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
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Priority() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Unit() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabItem_Kuerzel() {
		return (EAttribute)iLabItemEClass.getEStructuralFeatures().get(6);
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
	public EClass getILabResult() {
		return iLabResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_RefMale() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_RefFemale() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Unit() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_AnalyseTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_ObservationTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_TransmissionTime() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Result() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Flags() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Comment() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabResult_OriginContact() {
		return (EReference)iLabResultEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_Date() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabResult_Item() {
		return (EReference)iLabResultEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getILabResult_PathologicDescription() {
		return (EAttribute)iLabResultEClass.getEStructuralFeatures().get(12);
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
	public EReference getILabOrder_LabResult() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabOrder_LabItem() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getILabOrder_PatientContact() {
		return (EReference)iLabOrderEClass.getEStructuralFeatures().get(2);
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
	public EAttribute getIDocument_PatientId() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_AuthorId() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Title() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Description() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Status() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Created() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Lastchanged() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_MimeType() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDocument_Category() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDocument_History() {
		return (EReference)iDocumentEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_StoreId() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Extension() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIDocument_Keywords() {
		return (EAttribute)iDocumentEClass.getEStructuralFeatures().get(12);
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
	public EDataType getStringArray() {
		return stringArrayEDataType;
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
		iContactEClass = createEClass(ICONTACT);
		createEAttribute(iContactEClass, ICONTACT__CONTACT_TYPE);
		createEAttribute(iContactEClass, ICONTACT__MANDATOR);
		createEAttribute(iContactEClass, ICONTACT__USER);
		createEAttribute(iContactEClass, ICONTACT__PATIENT);
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

		iPersistentObjectEClass = createEClass(IPERSISTENT_OBJECT);
		createEReference(iPersistentObjectEClass, IPERSISTENT_OBJECT__XID);
		createEReference(iPersistentObjectEClass, IPERSISTENT_OBJECT__XIDS);

		iXidEClass = createEClass(IXID);
		createEAttribute(iXidEClass, IXID__DOMAIN);
		createEAttribute(iXidEClass, IXID__DOMAIN_ID);
		createEReference(iXidEClass, IXID__OBJECT);
		createEAttribute(iXidEClass, IXID__QUALITY);
		createEAttribute(iXidEClass, IXID__GUID);

		iCodeElementEClass = createEClass(ICODE_ELEMENT);

		iChangeListenerEClass = createEClass(ICHANGE_LISTENER);

		iStickerEClass = createEClass(ISTICKER);
		createEAttribute(iStickerEClass, ISTICKER__BACKGROUND);
		createEAttribute(iStickerEClass, ISTICKER__FOREGROUND);
		createEAttribute(iStickerEClass, ISTICKER__VISIBLE);

		iPersonEClass = createEClass(IPERSON);
		createEAttribute(iPersonEClass, IPERSON__DATE_OF_BIRTH);
		createEAttribute(iPersonEClass, IPERSON__GENDER);
		createEAttribute(iPersonEClass, IPERSON__TITEL);
		createEAttribute(iPersonEClass, IPERSON__TITEL_SUFFIX);

		iPatientEClass = createEClass(IPATIENT);
		createEAttribute(iPatientEClass, IPATIENT__DIAGNOSEN);
		createEAttribute(iPatientEClass, IPATIENT__RISK);
		createEAttribute(iPatientEClass, IPATIENT__FAMILY_ANAMNESE);
		createEAttribute(iPatientEClass, IPATIENT__PERSONAL_ANAMNESE);
		createEAttribute(iPatientEClass, IPATIENT__ALLERGIES);

		iUserEClass = createEClass(IUSER);
		createEAttribute(iUserEClass, IUSER__USERNAME);
		createEAttribute(iUserEClass, IUSER__PASSWORD);

		identifiableEClass = createEClass(IDENTIFIABLE);

		deleteableEClass = createEClass(DELETEABLE);
		createEAttribute(deleteableEClass, DELETEABLE__DELETED);

		iLabItemEClass = createEClass(ILAB_ITEM);
		createEAttribute(iLabItemEClass, ILAB_ITEM__TYP);
		createEAttribute(iLabItemEClass, ILAB_ITEM__REFERENCE_MALE);
		createEAttribute(iLabItemEClass, ILAB_ITEM__REFERENCE_FEMALE);
		createEAttribute(iLabItemEClass, ILAB_ITEM__GROUP);
		createEAttribute(iLabItemEClass, ILAB_ITEM__PRIORITY);
		createEAttribute(iLabItemEClass, ILAB_ITEM__UNIT);
		createEAttribute(iLabItemEClass, ILAB_ITEM__KUERZEL);
		createEAttribute(iLabItemEClass, ILAB_ITEM__NAME);
		createEAttribute(iLabItemEClass, ILAB_ITEM__DIGITS);
		createEAttribute(iLabItemEClass, ILAB_ITEM__VISIBLE);

		iLabResultEClass = createEClass(ILAB_RESULT);
		createEAttribute(iLabResultEClass, ILAB_RESULT__REF_MALE);
		createEAttribute(iLabResultEClass, ILAB_RESULT__REF_FEMALE);
		createEAttribute(iLabResultEClass, ILAB_RESULT__UNIT);
		createEAttribute(iLabResultEClass, ILAB_RESULT__ANALYSE_TIME);
		createEAttribute(iLabResultEClass, ILAB_RESULT__OBSERVATION_TIME);
		createEAttribute(iLabResultEClass, ILAB_RESULT__TRANSMISSION_TIME);
		createEAttribute(iLabResultEClass, ILAB_RESULT__RESULT);
		createEAttribute(iLabResultEClass, ILAB_RESULT__FLAGS);
		createEAttribute(iLabResultEClass, ILAB_RESULT__COMMENT);
		createEReference(iLabResultEClass, ILAB_RESULT__ORIGIN_CONTACT);
		createEAttribute(iLabResultEClass, ILAB_RESULT__DATE);
		createEReference(iLabResultEClass, ILAB_RESULT__ITEM);
		createEAttribute(iLabResultEClass, ILAB_RESULT__PATHOLOGIC_DESCRIPTION);

		iLabOrderEClass = createEClass(ILAB_ORDER);
		createEReference(iLabOrderEClass, ILAB_ORDER__LAB_RESULT);
		createEReference(iLabOrderEClass, ILAB_ORDER__LAB_ITEM);
		createEReference(iLabOrderEClass, ILAB_ORDER__PATIENT_CONTACT);

		iPeriodEClass = createEClass(IPERIOD);
		createEAttribute(iPeriodEClass, IPERIOD__START_TIME);
		createEAttribute(iPeriodEClass, IPERIOD__END_TIME);

		iDocumentEClass = createEClass(IDOCUMENT);
		createEAttribute(iDocumentEClass, IDOCUMENT__PATIENT_ID);
		createEAttribute(iDocumentEClass, IDOCUMENT__AUTHOR_ID);
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

		iCategoryEClass = createEClass(ICATEGORY);
		createEAttribute(iCategoryEClass, ICATEGORY__NAME);

		iHistoryEClass = createEClass(IHISTORY);
		createEAttribute(iHistoryEClass, IHISTORY__DATE);
		createEAttribute(iHistoryEClass, IHISTORY__STATUS);
		createEAttribute(iHistoryEClass, IHISTORY__DESCRIPTION);

		// Create data types
		stringArrayEDataType = createEDataType(STRING_ARRAY);
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
		iContactEClass.getESuperTypes().add(this.getIdentifiable());
		iContactEClass.getESuperTypes().add(this.getDeleteable());
		iPersistentObjectEClass.getESuperTypes().add(this.getIdentifiable());
		iXidEClass.getESuperTypes().add(this.getIPersistentObject());
		EGenericType g1 = createEGenericType(theTypesPackage.getComparable());
		EGenericType g2 = createEGenericType(this.getISticker());
		g1.getETypeArguments().add(g2);
		iStickerEClass.getEGenericSuperTypes().add(g1);
		iPersonEClass.getESuperTypes().add(this.getIContact());
		iPatientEClass.getESuperTypes().add(this.getIPerson());
		iUserEClass.getESuperTypes().add(this.getIContact());
		iLabItemEClass.getESuperTypes().add(this.getIdentifiable());
		iLabResultEClass.getESuperTypes().add(this.getIdentifiable());
		iLabOrderEClass.getESuperTypes().add(this.getIdentifiable());
		iPeriodEClass.getESuperTypes().add(this.getIdentifiable());
		iDocumentEClass.getESuperTypes().add(this.getIdentifiable());

		// Initialize classes and features; add operations and parameters
		initEClass(iContactEClass, IContact.class, "IContact", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIContact_ContactType(), theTypesPackage.getContactType(), "contactType", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Mandator(), ecorePackage.getEBoolean(), "mandator", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_User(), ecorePackage.getEBoolean(), "user", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIContact_Patient(), ecorePackage.getEBoolean(), "patient", null, 0, 1, IContact.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
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

		initEClass(iPersistentObjectEClass, IPersistentObject.class, "IPersistentObject", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIPersistentObject_Xid(), this.getIXid(), null, "xid", null, 0, 1, IPersistentObject.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIPersistentObject_Xids(), this.getIXid(), null, "xids", null, 0, -1, IPersistentObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iPersistentObjectEClass, ecorePackage.getELong(), "getLastUpdate", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iPersistentObjectEClass, ecorePackage.getEBoolean(), "isValid", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iPersistentObjectEClass, ecorePackage.getEString(), "storeToString", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iPersistentObjectEClass, ecorePackage.getEInt(), "state", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iPersistentObjectEClass, ecorePackage.getEBoolean(), "exists", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iPersistentObjectEClass, ecorePackage.getEBoolean(), "isAvailable", 0, 1, IS_UNIQUE, IS_ORDERED);

		EOperation op = addEOperation(iPersistentObjectEClass, ecorePackage.getEString(), "getXid", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "domain", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPersistentObjectEClass, ecorePackage.getEBoolean(), "addXid", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "domain", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "domain_id", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "updateIfExists", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPersistentObjectEClass, ecorePackage.getEBoolean(), "get", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getStringArray(), "fields", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getStringArray(), "values", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPersistentObjectEClass, ecorePackage.getEString(), "get", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "field", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPersistentObjectEClass, ecorePackage.getEBoolean(), "set", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "field", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iXidEClass, IXid.class, "IXid", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIXid_Domain(), ecorePackage.getEString(), "domain", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIXid_DomainId(), ecorePackage.getEString(), "domainId", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIXid_Object(), this.getIPersistentObject(), null, "object", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIXid_Quality(), ecorePackage.getEInt(), "quality", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIXid_GUID(), ecorePackage.getEBoolean(), "gUID", null, 0, 1, IXid.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iCodeElementEClass, ICodeElement.class, "ICodeElement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getCodeSystemName", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getCodeSystemCode", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getId", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getCode", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iCodeElementEClass, ecorePackage.getEString(), "getText", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iCodeElementEClass, ecorePackage.getEJavaObject(), "getActions", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "context", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iChangeListenerEClass, IChangeListener.class, "IChangeListener", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = addEOperation(iChangeListenerEClass, null, "valueChanged", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIPersistentObject(), "object", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "field", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "oldValue", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEJavaObject(), "newValue", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iChangeListenerEClass, null, "objectDisposing", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getIPersistentObject(), "object", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iStickerEClass, ISticker.class, "ISticker", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getISticker_Background(), ecorePackage.getEString(), "background", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Foreground(), ecorePackage.getEString(), "foreground", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISticker_Visible(), ecorePackage.getEBoolean(), "visible", null, 0, 1, ISticker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iStickerEClass, ecorePackage.getEString(), "getId", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iStickerEClass, ecorePackage.getEString(), "getLabel", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iStickerEClass, ecorePackage.getEInt(), "getWert", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iStickerEClass, null, "setWert", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEInt(), "w", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iStickerEClass, ecorePackage.getEBoolean(), "delete", 0, 1, IS_UNIQUE, IS_ORDERED);

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

		initEClass(iPersonEClass, IPerson.class, "IPerson", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIPerson_DateOfBirth(), theTypesPackage.getTimeTool(), "dateOfBirth", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_Gender(), theTypesPackage.getGender(), "gender", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_Titel(), ecorePackage.getEString(), "titel", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPerson_TitelSuffix(), ecorePackage.getEString(), "titelSuffix", null, 0, 1, IPerson.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iPersonEClass, ecorePackage.getEString(), "getFirstName", 1, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iPersonEClass, ecorePackage.getEString(), "getFamilyName", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iPatientEClass, IPatient.class, "IPatient", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIPatient_Diagnosen(), ecorePackage.getEString(), "diagnosen", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_Risk(), ecorePackage.getEString(), "risk", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_FamilyAnamnese(), ecorePackage.getEString(), "familyAnamnese", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_PersonalAnamnese(), ecorePackage.getEString(), "personalAnamnese", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPatient_Allergies(), ecorePackage.getEString(), "allergies", null, 0, 1, IPatient.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(iPatientEClass, ecorePackage.getEString(), "getPatientLabel", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(iPatientEClass, ecorePackage.getEString(), "getPatientNr", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(iPatientEClass, null, "setPatientNr", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "patientNr", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(iUserEClass, IUser.class, "IUser", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIUser_Username(), ecorePackage.getEString(), "username", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIUser_Password(), ecorePackage.getEString(), "password", null, 0, 1, IUser.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(identifiableEClass, Identifiable.class, "Identifiable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		addEOperation(identifiableEClass, ecorePackage.getEString(), "getId", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(identifiableEClass, ecorePackage.getEString(), "getLabel", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(deleteableEClass, Deleteable.class, "Deleteable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDeleteable_Deleted(), ecorePackage.getEBoolean(), "deleted", null, 0, 1, Deleteable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iLabItemEClass, ILabItem.class, "ILabItem", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getILabItem_Typ(), theTypesPackage.getLabItemTyp(), "typ", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_ReferenceMale(), ecorePackage.getEString(), "referenceMale", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_ReferenceFemale(), ecorePackage.getEString(), "referenceFemale", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Group(), ecorePackage.getEString(), "group", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Priority(), ecorePackage.getEString(), "priority", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Unit(), ecorePackage.getEString(), "unit", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Kuerzel(), ecorePackage.getEString(), "kuerzel", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Name(), ecorePackage.getEString(), "name", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Digits(), ecorePackage.getEInt(), "digits", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabItem_Visible(), ecorePackage.getEBoolean(), "visible", null, 0, 1, ILabItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iLabResultEClass, ILabResult.class, "ILabResult", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getILabResult_RefMale(), ecorePackage.getEString(), "refMale", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_RefFemale(), ecorePackage.getEString(), "refFemale", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Unit(), ecorePackage.getEString(), "unit", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_AnalyseTime(), theTypesPackage.getTimeTool(), "analyseTime", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_ObservationTime(), theTypesPackage.getTimeTool(), "observationTime", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_TransmissionTime(), theTypesPackage.getTimeTool(), "transmissionTime", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Result(), ecorePackage.getEString(), "result", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Flags(), ecorePackage.getEInt(), "flags", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Comment(), ecorePackage.getEString(), "comment", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabResult_OriginContact(), this.getIContact(), null, "originContact", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_Date(), ecorePackage.getEString(), "date", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabResult_Item(), this.getILabItem(), null, "item", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getILabResult_PathologicDescription(), theTypesPackage.getPathologicDescription(), "pathologicDescription", null, 0, 1, ILabResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iLabOrderEClass, ILabOrder.class, "ILabOrder", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getILabOrder_LabResult(), this.getILabResult(), null, "labResult", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabOrder_LabItem(), this.getILabItem(), null, "labItem", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getILabOrder_PatientContact(), this.getIPatient(), null, "patientContact", null, 0, 1, ILabOrder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iPeriodEClass, IPeriod.class, "IPeriod", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIPeriod_StartTime(), theTypesPackage.getTimeTool(), "startTime", null, 0, 1, IPeriod.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIPeriod_EndTime(), theTypesPackage.getTimeTool(), "endTime", null, 0, 1, IPeriod.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iDocumentEClass, IDocument.class, "IDocument", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIDocument_PatientId(), ecorePackage.getEString(), "patientId", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIDocument_AuthorId(), ecorePackage.getEString(), "authorId", null, 0, 1, IDocument.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
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

		initEClass(iCategoryEClass, ICategory.class, "ICategory", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getICategory_Name(), ecorePackage.getEString(), "name", null, 0, 1, ICategory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(iHistoryEClass, IHistory.class, "IHistory", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIHistory_Date(), ecorePackage.getEDate(), "date", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIHistory_Status(), theTypesPackage.getDocumentStatus(), "status", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIHistory_Description(), ecorePackage.getEString(), "description", null, 0, 1, IHistory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize data types
		initEDataType(stringArrayEDataType, String[].class, "StringArray", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //ModelPackageImpl
